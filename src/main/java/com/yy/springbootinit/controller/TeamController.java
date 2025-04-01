package com.yy.springbootinit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.yy.springbootinit.annotation.AuthCheck;
import com.yy.springbootinit.common.BaseResponse;
import com.yy.springbootinit.common.DeleteRequest;
import com.yy.springbootinit.common.ErrorCode;
import com.yy.springbootinit.common.ResultUtils;
import com.yy.springbootinit.constant.UserConstant;
import com.yy.springbootinit.exception.BusinessException;
import com.yy.springbootinit.exception.ThrowUtils;
import com.yy.springbootinit.model.dto.chart.ChartQueryRequest;
import com.yy.springbootinit.model.dto.chart.ChartRegenRequest;
import com.yy.springbootinit.model.dto.team.TeamAddRequest;
import com.yy.springbootinit.model.dto.team.TeamQueryRequest;
import com.yy.springbootinit.model.entity.Chart;
import com.yy.springbootinit.model.entity.Team;
import com.yy.springbootinit.model.vo.BIResponse;
import com.yy.springbootinit.model.vo.TeamVO;
import com.yy.springbootinit.service.ChartService;
import com.yy.springbootinit.service.TeamService;
import com.yy.springbootinit.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequestMapping("/team")
@RestController
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @Resource
    private ChartService chartService;


    @PostMapping("/add")
    public BaseResponse<Boolean> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String name = teamAddRequest.getName();
        if (teamService.count(new QueryWrapper<Team>().eq("name", name)) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "队伍已存在");
        }
        boolean b = teamService.addTeam(teamAddRequest, request);
        return ResultUtils.success(b);
    }

    @PostMapping("/list/page")
    public BaseResponse<Page<TeamVO>> listTeamByPage(@RequestBody TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<TeamVO> teamVOS = teamService.listTeam(teamQueryRequest, request);
        return ResultUtils.success(teamVOS);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody Team team, HttpServletRequest request) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = teamService.joinTeam(team, request);
        return ResultUtils.success(b);
    }

    @PostMapping("/exit")
    public BaseResponse<Boolean> exitTeam(@RequestBody Team team, HttpServletRequest request) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = teamService.exitTeam(team, request);
        return ResultUtils.success(b);
    }

    @PostMapping("/page/my/joined")
    public BaseResponse<Page<TeamVO>> pageMyJoinedTeam(@RequestBody TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<TeamVO> teamVOS = teamService.pageMyJoinedTeam(teamQueryRequest, request);
        return ResultUtils.success(teamVOS);
    }

    @PostMapping("/chart/page")
    public BaseResponse<Page<Chart>> listTeamChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
        Long teamId = chartQueryRequest.getTeamId();
        if (teamId == null || teamId < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Chart> chartPage = chartService.pageTeamChart(chartQueryRequest);
        return ResultUtils.success(chartPage);
    }

    @GetMapping("/list/my/joined")
    public BaseResponse<List<Team>> listAllMyJoinedTeam(HttpServletRequest request) {
        return ResultUtils.success(teamService.listAllMyJoinedTeam(request));
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/page")
    public BaseResponse<Page<Team>> pageTeam(@RequestBody TeamQueryRequest teamQueryRequest) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Team> teamPage = teamService.pageTeam(teamQueryRequest);
        return ResultUtils.success(teamPage);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody Team team, HttpServletRequest request) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = teamService.updateTeam(team, request);
        return ResultUtils.success(b);
    }

    @AuthCheck(mustRole = "admin")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = teamService.deleteTeam(deleteRequest);
        return ResultUtils.success(b);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(Long id) {
        if (id == null || id < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        return ResultUtils.success(team);
    }

    /**
     * 再次生成图表 团队
     *
     * @param chartRegenRequest
     * @param request
     * @return
     */
    @PostMapping("/chart/regen")
    public BaseResponse<BIResponse> regenChart(@RequestBody ChartRegenRequest chartRegenRequest,
                                               HttpServletRequest request) {
        ThrowUtils.throwIf(userService.getLoginUser(request) == null, ErrorCode.NOT_LOGIN_ERROR, "未登录");
        ThrowUtils.throwIf(chartRegenRequest == null, ErrorCode.PARAMS_ERROR);

        BIResponse biResponse = chartService.regenChartByAsyncMqFromTeam(chartRegenRequest, request);
        return ResultUtils.success(biResponse);
    }


}