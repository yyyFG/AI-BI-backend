package com.yy.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yy.springbootinit.common.DeleteRequest;
import com.yy.springbootinit.model.dto.team.TeamAddRequest;
import com.yy.springbootinit.model.entity.Team;
import com.yy.springbootinit.model.entity.TeamChart;

import javax.servlet.http.HttpServletRequest;


/**
 * @author DCX
 * @description 针对表【team_chart(队伍图表关系表)】的数据库操作Service
 * @createDate 2025-03-19 16:01:02
*/
public interface TeamChartService extends IService<TeamChart> {


    /**
     * 队伍图表上传
     * @param teamAddRequest
     * @param request
     * @return
     */
    boolean addTeamChart(Team team, HttpServletRequest request);


    /**
     * 队伍图表删除
     * @param deleteRequest
     * @return
     */
    boolean deleteTeamChart(DeleteRequest deleteRequest);


    /**
     * 队伍图表修改
     * @param team
     * @param request
     * @return
     */
    Boolean updateTeamChart(Team team, HttpServletRequest request);
}
