package com.yy.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yy.springbootinit.bizmq.BIProducer;
import com.yy.springbootinit.common.BaseResponse;
import com.yy.springbootinit.common.DeleteRequest;
import com.yy.springbootinit.common.ErrorCode;
import com.yy.springbootinit.common.ResultUtils;
import com.yy.springbootinit.constant.CommonConstant;
import com.yy.springbootinit.exception.BusinessException;
import com.yy.springbootinit.exception.ThrowUtils;
import com.yy.springbootinit.manager.RedisLimiterManager;
import com.yy.springbootinit.manager.TestDeepSeekAiManager;
import com.yy.springbootinit.model.dto.chart.*;
import com.yy.springbootinit.model.dto.team_chart.ChartAddToTeamRequest;
import com.yy.springbootinit.model.entity.Chart;
import com.yy.springbootinit.model.entity.User;
import com.yy.springbootinit.model.vo.BIResponse;
import com.yy.springbootinit.service.ChartService;
import com.yy.springbootinit.service.UserService;
import com.yy.springbootinit.utils.ExcelUtils;
import com.yy.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 图表接口
 *
 */
@RestController
@RequestMapping("/Chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;


    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);

        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }


    /**
     * 智能分析(同步)
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BIResponse> genChartByAI(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件为空");
        ThrowUtils.throwIf(genChartByAIRequest == null, ErrorCode.PARAMS_ERROR, "分析诉求为空");
        ThrowUtils.throwIf(userService.getLoginUser(request) == null, ErrorCode.NOT_LOGIN_ERROR);

        BIResponse biResponse = chartService.genChartByAI(multipartFile, genChartByAIRequest, request);

        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析（异步）
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BIResponse> genChartByAIAsync(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件为空");
        ThrowUtils.throwIf(genChartByAIRequest == null, ErrorCode.PARAMS_ERROR, "分析诉求为空");
        ThrowUtils.throwIf(userService.getLoginUser(request) == null, ErrorCode.NOT_LOGIN_ERROR);

        BIResponse biResponse = chartService.genChartByAIAsync(multipartFile, genChartByAIRequest, request);

        return ResultUtils.success(biResponse);
    }


    /**
     * 智能分析（异步 消息队列）
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BIResponse> genChartByAIAsyncMQ(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件为空");
        ThrowUtils.throwIf(genChartByAIRequest == null, ErrorCode.PARAMS_ERROR, "分析诉求为空");
        ThrowUtils.throwIf(userService.getLoginUser(request) == null, ErrorCode.NOT_LOGIN_ERROR);

        BIResponse biResponse = chartService.genChartByAIAsyncMQ(multipartFile, genChartByAIRequest, request);

        return ResultUtils.success(biResponse);
    }


    /**
     * 更新
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }


    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }



    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
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

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name),"name",name);
        queryWrapper.eq(StringUtils.isNotBlank(goal),"goal",goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType),"chartType",chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId),"userId",userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);

        return queryWrapper;
    }


    /**
     * 再次生成图表
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/regenChart")
    public BaseResponse<BIResponse> RegenChartByAI(long id, HttpServletRequest request) {

        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "id输入错误");
        ThrowUtils.throwIf(userService.getLoginUser(request) == null, ErrorCode.NOT_LOGIN_ERROR);

        BIResponse biResponse = chartService.RegenChartByAI(id, request);

        return ResultUtils.success(biResponse);
    }


    /**
     * 添加图表到队伍
     * @param chartAddToTeamRequest
     * @param request
     * @return
     */
    @PostMapping("/add/team")
    public BaseResponse<Boolean> addChartToTeam(@RequestBody ChartAddToTeamRequest chartAddToTeamRequest, HttpServletRequest request) {
        if (chartAddToTeamRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = chartService.addChartToTeam(chartAddToTeamRequest, request);
        return ResultUtils.success(b);
    }
}
