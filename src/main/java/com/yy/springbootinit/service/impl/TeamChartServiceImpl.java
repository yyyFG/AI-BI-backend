package com.yy.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yy.springbootinit.common.DeleteRequest;
import com.yy.springbootinit.common.ErrorCode;
import com.yy.springbootinit.exception.BusinessException;
import com.yy.springbootinit.exception.ThrowUtils;
import com.yy.springbootinit.mapper.TeamChartMapper;
import com.yy.springbootinit.model.dto.chart.ChartQueryRequest;
import com.yy.springbootinit.model.dto.team.TeamAddRequest;
import com.yy.springbootinit.model.entity.Team;
import com.yy.springbootinit.model.entity.TeamChart;
import com.yy.springbootinit.model.entity.TeamUser;
import com.yy.springbootinit.model.entity.User;
import com.yy.springbootinit.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author DCX
 * @description 针对表【team_chart(队伍图表关系表)】的数据库操作Service实现
 * @createDate 2025-03-19 16:01:02
*/
@Service
public class TeamChartServiceImpl extends ServiceImpl<TeamChartMapper, TeamChart>
    implements TeamChartService {


    @Resource
    private UserService userService;

    @Resource
    private TeamUserService teamUserService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTeamChart(Team team, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long teamId = team.getId();
        ThrowUtils.throwIf((!isInTeam(team, request)), ErrorCode.PARAMS_ERROR, "不在该队伍中");

        QueryWrapper<TeamUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        long count = teamUserService.count(queryWrapper);

        return true;
    }


    private boolean isInTeam(Team team, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        QueryWrapper<TeamUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", team.getId());
        queryWrapper.eq("userId", userId);
        long count = teamUserService.count(queryWrapper);
        return count > 0;
    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public boolean addTeamChart(ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
//        User loginUser = userService.getLoginUser(request);
//        Long userId = loginUser.getId();
//        QueryWrapper<TeamUser> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("userId", userId);
//        List<TeamUser> teamUsers = teamUserService.list(queryWrapper);
//
//
//        String name = teamAddRequest.getName();
//        String imgUrl = teamAddRequest.getImgUrl();
//        String description = teamAddRequest.getDescription();
//        Integer maxNum = teamAddRequest.getMaxNum();
//        if (StringUtils.isEmpty(name)) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称不能为空");
//        }
//
//        if (StringUtils.isEmpty(imgUrl)) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片不能为空");
//        }
//
//        // 增加图片校验
//
//
//
//        if (StringUtils.isEmpty(description) || description.length() > 100) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不能为空或长度大于100");
//        }
//        if (maxNum == null || maxNum < 1) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最大人数不得为空或者小于1");
//        }
//        Team team = new Team();
//        BeanUtils.copyProperties(teamAddRequest, team);
//        User loginUser = userService.getLoginUser(request);
//        Long userId = loginUser.getId();
//        team.setUserId(userId);
//        boolean b1 = this.save(team);
//        TeamUser teamUser = new TeamUser();
//        teamUser.setTeamId(team.getId());
//        teamUser.setUserId(userId);
//        boolean b2 = teamUserService.save(teamUser);
//        return b1 && b2;
//    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeamChart(DeleteRequest deleteRequest) {
        Long teamId = deleteRequest.getId();
//        Team team = this.getById(teamId);
//        if (team == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
//        }
//        QueryWrapper<TeamChart> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("teamId", teamId);
////        boolean b1 = teamChartService.remove(queryWrapper);
//        QueryWrapper<TeamUser> teamUserQueryWrapper = new QueryWrapper<>();
//        teamUserQueryWrapper.eq("teamId", teamId);
//        boolean b2 = teamUserService.remove(teamUserQueryWrapper);
//        boolean b3 = this.removeById(teamId);
//        return b1 && b2 && b3;
        return true;
    }


    @Override
    public Boolean updateTeamChart(Team team, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        if (!userService.isAdmin(request) && !userId.equals(team.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您无权限修改队伍信息");
        }
//        return this.updateById(team);
        return true;
    }
}




