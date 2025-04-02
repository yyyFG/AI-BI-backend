package com.yy.springbootinit.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yy.springbootinit.bizmq.BIProducer;
import com.yy.springbootinit.common.BaseResponse;
import com.yy.springbootinit.common.ErrorCode;
import com.yy.springbootinit.common.ResultUtils;
import com.yy.springbootinit.constant.CommonConstant;
import com.yy.springbootinit.exception.BusinessException;
import com.yy.springbootinit.exception.ThrowUtils;
import com.yy.springbootinit.manager.RedisLimiterManager;
import com.yy.springbootinit.manager.TestDeepSeekAiManager;
import com.yy.springbootinit.mapper.TeamChartMapper;
import com.yy.springbootinit.model.dto.chart.ChartQueryRequest;
import com.yy.springbootinit.model.dto.chart.ChartRegenRequest;
import com.yy.springbootinit.model.dto.chart.GenChartByAIRequest;
import com.yy.springbootinit.model.dto.team_chart.ChartAddToTeamRequest;
import com.yy.springbootinit.model.entity.Chart;
import com.yy.springbootinit.model.entity.Team;
import com.yy.springbootinit.model.entity.TeamChart;
import com.yy.springbootinit.model.entity.User;
import com.yy.springbootinit.model.vo.BIResponse;
import com.yy.springbootinit.service.ChartService;
import com.yy.springbootinit.mapper.ChartMapper;
import com.yy.springbootinit.service.TeamChartService;
import com.yy.springbootinit.service.TeamService;
import com.yy.springbootinit.service.UserService;
import com.yy.springbootinit.utils.ExcelUtils;
import com.yy.springbootinit.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.shadow.org.terracotta.offheapstore.util.Retryer;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
* @author DCX
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2025-03-19 16:01:02
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

    @Resource
    private TeamChartService teamChartService;


    @Resource
    private UserService userService;

    @Resource
    private TestDeepSeekAiManager testDeepSeekAiManager;

    @Resource
    private ChartMapper chartMapper;

    @Resource
    private TeamChartMapper teamChartMapper;

    @Resource
    private TeamService teamService;

    @Resource
    private RedisLimiterManager redisLimiterManager;


    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BIProducer biProducer;


//    @Resource
//    private Retryer<Boolean> retryer;

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }

        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        // 根据查询条件查询
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<Chart> pageChart(ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        String searchParams = chartQueryRequest.getSearchParams();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Chart> queryWrapper = this.getQueryWrapper(chartQueryRequest);
        queryWrapper.like(StringUtils.isNotEmpty(searchParams), "name", searchParams).or(StringUtils.isNotEmpty(searchParams), wrapper -> wrapper.like("status", searchParams));
        queryWrapper.orderBy(true, true, "updateTime");
        Page<Chart> chartPage = this.page(new Page<>(current, size), queryWrapper);
        return chartPage;
    }


    @Override
    public Page<Chart> pageTeamChart(ChartQueryRequest chartQueryRequest) {
        Long teamId = chartQueryRequest.getTeamId();
        long current = chartQueryRequest.getCurrent();
        long pageSize = chartQueryRequest.getPageSize();
        String name = chartQueryRequest.getName();

        Page<TeamChart> teamChartPage = teamChartService.page(new Page<>(current, pageSize),
                new QueryWrapper<TeamChart>().eq("teamId", teamId));
        if (CollectionUtils.isEmpty(teamChartPage.getRecords())) {
            return new Page<>();
        }
        List<Long> chartIds = teamChartPage.getRecords().stream()
                .map(TeamChart::getChartId).collect(Collectors.toList());
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(CollectionUtils.isNotEmpty(chartIds), "id", chartIds);
        queryWrapper.like(StringUtils.isNotEmpty(name), "name", name);
        Page<Chart> chartPage = this.page(new Page<>(current, pageSize), queryWrapper);
        chartPage.setTotal(chartIds.size());
        return chartPage;
    }


    /**
     * 智能分析(同步)
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    @Override
    public BIResponse genChartByAI(MultipartFile multipartFile,
                                                 GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {
        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀， aaa.png
        String suffix = FileUtil.getSuffix(originalFilename);
//        final List<String> validFileSuffixList = Arrays.asList("png", "jpg", "svg", "webp", "jpeg");
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        // 用户登录
        User loginUser = userService.getLoginUser(request);

        // 限流判断,每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAI_" + loginUser.getId());

        // 用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ". 请使用" + chartType;
        }

        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //  压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        System.out.println(csvData.length());
        userInput.append(csvData).append("\n");

        String result = testDeepSeekAiManager.doChat(userInput.toString());
//        String result = "【【";

        // 获取结果
        String[] splits = result.split("【【【【【");
//        System.out.println(splits.length);
        System.out.println(splits[0]);
//
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成错误");
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();

        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        if((csvData.length()*2) < 63*1024) chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        chart.setStatus("succeed");
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        BIResponse biResponse = new BIResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setCharId(chart.getId());

        return biResponse;
    }


    /**
     * 智能分析（异步）
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    @Override
    public BIResponse genChartByAIAsync(MultipartFile multipartFile,
                                                      GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {
        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀， aaa.png
        String suffix = FileUtil.getSuffix(originalFilename);
//        final List<String> validFileSuffixList = Arrays.asList("png", "jpg", "svg", "webp", "jpeg");
        final List<String> validFileSuffixList = Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        // 用户登录
        User loginUser = userService.getLoginUser(request);

        // 限流判断,每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAI_" + loginUser.getId());

        // 用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal += ". 请使用" + chartType;
        }

        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //  压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");


        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        if((csvData.length()*2) < 63*1024) chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表保存失败");

        // todo 建议处理任务队列满了后，抛异常的情况
        CompletableFuture.runAsync(() -> {
            // 先修改图表任务状态为 “执行中”, 等待执行成功后，修改为“已完成”，保存执行结果；执行失败后，状态修改为：“失败”，记录任务失败信息
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = this.updateById(updateChart);
            if(!b){
//                throw new BusinessException(ErrorCode.OPERATION_ERROR,"图表状态更改失败");
                handleChartUpdateError(chart.getId(), "图表状态执行中更改失败");
                return;
            }
            // 调用AI
            String result = testDeepSeekAiManager.doChat(userInput.toString());
            // 获取结果
            String[] splits = result.split("【【【【【");
//        System.out.println(splits.length);
            System.out.println(splits[0]);
//
            if(splits.length < 3){
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成错误");
                handleChartUpdateError(chart.getId(), "生成错误");
                return;
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            // todo 建议定义状态为枚举值
            updateChartResult.setStatus("succeed");
            boolean updateResult = this.updateById(updateChartResult);
            if(!updateResult){
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新图表成功状态失败");
                handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
                return;
            }
        }, threadPoolExecutor);

        BIResponse biResponse = new BIResponse();
        biResponse.setCharId(chart.getId());

        return biResponse;
    }


    /**
     * 智能分析（异步 消息队列）
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    @Override
    public BIResponse genChartByAIAsyncMQ(MultipartFile multipartFile,
                                                        GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {
        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀， aaa.png
        String suffix = FileUtil.getSuffix(originalFilename);
//        final List<String> validFileSuffixList = Arrays.asList("png", "jpg", "svg", "webp", "jpeg");
        final List<String> validFileSuffixList = Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        // 用户登录
        User loginUser = userService.getLoginUser(request);

        // 限流判断,每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAI_" + loginUser.getId());


        // 用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal += ". 请使用" + chartType;
        }

        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //  压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");


        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        if((csvData.length()*2) < 63*1024) chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表保存失败");
        long newChartId = chart.getId();

        biProducer.sendMessage(String.valueOf(newChartId));

        BIResponse biResponse = new BIResponse();
        biResponse.setCharId(newChartId);

        return biResponse;
    }

    // 处理图表更新失败
    private void handleChartUpdateError(long chartId, String execMessage){
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("failed");
        updateChart.setExecMessage("execMessage");
        boolean b = this.updateById(updateChart);
        if(!b){
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }



    /**
     * 再次生成图表
     *
     * @param id
     * @param request
     * @return
     */
    @Override
    public BIResponse RegenChartByAI(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入错误");
        }

        // 参数校验
        Long chartId = id;

//        System.out.println(chartId + "DDDD");
        Chart chart = this.getById(chartId);
        ThrowUtils.throwIf(chart.getId() == 0, ErrorCode.PARAMS_ERROR, "图表不存在");
        String name = chart.getName();
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String chartData = chart.getChartData();
        ThrowUtils.throwIf(StringUtils.isBlank(name) || name.length() > 100, ErrorCode.PARAMS_ERROR, "图表名称不合法");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(chartData), ErrorCode.PARAMS_ERROR, "原始数据为空");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");


        // 用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ". 请使用" + chartType;
        }

        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //  压缩后的数据
        String csvData = chartData;
        userInput.append(csvData).append("\n");

        String result = testDeepSeekAiManager.doChat(userInput.toString());
        // 获取结果
        String[] splits = result.split("【【【【【");
//        System.out.println(splits.length);
//        System.out.println(splits[0]);
//
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成错误");
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();

        // 插入到数据库
        Chart newChart = new Chart();
        newChart.setId(chart.getId());
        newChart.setName(name);
        newChart.setGoal(goal);
        newChart.setChartType(chartType);
        newChart.setChartData(csvData);
        newChart.setGenChart(genChart);
        newChart.setGenResult(genResult);
        newChart.setStatus("succeed");
        boolean saveResult = this.saveOrUpdate(newChart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        BIResponse biResponse = new BIResponse();
        biResponse.setCharId(chart.getId());

        return biResponse;
    }

    /**
     * 再次图表生成 队伍
     * @param chartRegenRequest
     * @param request
     * @return
     */

    @Override
    public BIResponse regenChartByAsyncMqFromTeam(ChartRegenRequest chartRegenRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
//        // 判断能否生成图表
//        boolean canGenChart = userService.canGenerateChart(loginUser);
//        if (!canGenChart) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您同时生成图表过多，请稍后再生成");
//        }
//        userService.increaseUserGeneratIngCount(userId);
//        // 先校验用户积分是否足够
//        boolean hasScore = userService.userHasScore(loginUser);
//        if (!hasScore) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户积分不足");
//        }

        // 参数校验
        Long chartId = chartRegenRequest.getId();
        String name = chartRegenRequest.getName();
        String goal = chartRegenRequest.getGoal();
        String chartData = chartRegenRequest.getChartData();
        String chartType = chartRegenRequest.getChartType();
        Long teamId = chartRegenRequest.getTeamId();
        ThrowUtils.throwIf(chartId == null || chartId <= 0, ErrorCode.PARAMS_ERROR, "图表不存在");
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "图表名称为空");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(chartData), ErrorCode.PARAMS_ERROR, "原始数据为空");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");
        ThrowUtils.throwIf(teamId == null, ErrorCode.PARAMS_ERROR, "队伍Id为空");

        // 查看需要重新生成的图表是否存在
        ChartQueryRequest chartQueryRequest = new ChartQueryRequest();
        chartQueryRequest.setId(chartId);
        Long chartCount = chartMapper.selectCount(this.getQueryWrapper(chartQueryRequest));
        if (chartCount <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表不存在");
        }

        // 限流
        redisLimiterManager.doRateLimit("genChartByAI_" + userId);

        // 用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ". 请使用" + chartType;
        }

        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //  压缩后的数据
        String csvData = chartData;
        userInput.append(csvData).append("\n");

        CompletableFuture.runAsync(() -> {
            Chart updateChart = new Chart();
            updateChart.setId(chartId);
            updateChart.setStatus("running");
            boolean b = this.updateById(updateChart);
            if(!b){
                handleChartUpdateError(chartId, "图表状态执行中更改失败");
                return;
            }

            // 调用AI
            String result = testDeepSeekAiManager.doChat(userInput.toString());
            // 获取结果
            String[] splits = result.split("【【【【【");
            System.out.println(splits[0]);

            if(splits.length < 3){
                handleChartUpdateError(chartId, "生成错误");
                return;
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chartId);
            updateChartResult.setChartType(chartType);
            updateChartResult.setChartData(csvData);
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);

            updateChartResult.setStatus("succeed");
            boolean updateResult = this.saveOrUpdate(updateChartResult);
            if(!updateResult){
                handleChartUpdateError(chartId, "更新图表成功状态失败");
                return;
            }
        }, threadPoolExecutor);

        BIResponse biResponse = new BIResponse();
        biResponse.setCharId(chartId);

        return biResponse;
    }


    /**
     * 添加图表到队伍
     * @param chartAddToTeamRequest
     * @param request
     * @return
     */
    @Override
    public boolean addChartToTeam(ChartAddToTeamRequest chartAddToTeamRequest, HttpServletRequest request) {
        Long chartId = chartAddToTeamRequest.getChartId();
        Long teamId = chartAddToTeamRequest.getTeamId();
        Chart chart = this.getById(chartId);
        if (chart == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表不存在");
        }
        Team team = teamService.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }

        QueryWrapper<TeamChart> queryWrapper = new QueryWrapper();
        queryWrapper.eq("teamId",teamId).eq("chartId",chartId);
        Long count = teamChartMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "图表已存在");

        TeamChart teamChart = TeamChart.builder().teamId(teamId).chartId(chartId).build();
        return teamChartService.save(teamChart);
    }

}






