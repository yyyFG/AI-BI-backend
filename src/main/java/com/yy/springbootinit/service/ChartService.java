package com.yy.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yy.springbootinit.model.dto.chart.ChartQueryRequest;
import com.yy.springbootinit.model.dto.chart.ChartRegenRequest;
import com.yy.springbootinit.model.dto.chart.GenChartByAIRequest;
import com.yy.springbootinit.model.dto.team_chart.ChartAddToTeamRequest;
import com.yy.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yy.springbootinit.model.vo.BIResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author DCX
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2025-03-19 16:01:02
*/
public interface ChartService extends IService<Chart> {
    /**
     * 分页查询图表信息
     * @param chartQueryRequest
     * @return
     */
    Page<Chart> pageChart(ChartQueryRequest chartQueryRequest);

    /**
     * 根据查询
     *
     * @param chartQueryRequest
     * @return
     */
    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);


    /**
     * 分页获取图表信息团队
     * @param chartQueryRequest
     * @return
     */
    Page<Chart> pageTeamChart(ChartQueryRequest chartQueryRequest);


    /**
     * 智能分析 同步
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    BIResponse genChartByAI(MultipartFile multipartFile,
                                   GenChartByAIRequest genChartByAIRequest, HttpServletRequest request);


    /**
     * 智能分析 异步
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    BIResponse genChartByAIAsync(MultipartFile multipartFile,
                                        GenChartByAIRequest genChartByAIRequest, HttpServletRequest request);


    /**
     * 智能分析（异步 消息队列）
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    BIResponse genChartByAIAsyncMQ(MultipartFile multipartFile,
                                                 GenChartByAIRequest genChartByAIRequest, HttpServletRequest request);


    /**
     * 再次生成图表
     *
     * @param id
     * @param request
     * @return
     */
    BIResponse RegenChartByAI(long id, HttpServletRequest request);


    /**
     * 再次生成图表 团队
     * @param chartRegenRequest
     * @param request
     * @return
     */
    BIResponse regenChartByAsyncMqFromTeam(ChartRegenRequest chartRegenRequest, HttpServletRequest request);


    /**
     * 添加图表到队伍
     * @param chartAddToTeamRequest
     * @param request
     * @return
     */
    boolean addChartToTeam(ChartAddToTeamRequest chartAddToTeamRequest, HttpServletRequest request);
}
