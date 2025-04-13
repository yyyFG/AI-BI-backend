package com.yy.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
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
import com.yy.springbootinit.model.entity.TeamUser;
import com.yy.springbootinit.model.entity.User;
import com.yy.springbootinit.model.vo.BIResponse;
import com.yy.springbootinit.model.vo.TeamVO;
import com.yy.springbootinit.service.ChartService;
import com.yy.springbootinit.service.TeamService;
import com.yy.springbootinit.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static com.yy.springbootinit.constant.CommonConstant.BASE_URL;

@RequestMapping("/team")
@RestController
@CrossOrigin
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

    @PostMapping("/page/my/TeamUser")
    public BaseResponse<List<User>> pageMyTeamUser(@RequestBody TeamUser teamUser, HttpServletRequest request){
        ThrowUtils.throwIf(teamUser == null, ErrorCode.PARAMS_ERROR);

        List<User> teamUserPage = teamService.pageMyTeamUser(teamUser, request);

        ThrowUtils.throwIf(teamUserPage == null, ErrorCode.PARAMS_ERROR,"队伍为空");

        return ResultUtils.success(teamUserPage);
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能为空");
        }

        Long teamId = team.getId();
        Team oldTeam = teamService.getById(teamId);
        String teamAvatar = team.getImgUrl();
        if(StringUtils.isEmpty(teamAvatar)) team.setImgUrl(oldTeam.getImgUrl());
        else {
            String imgUrl = BASE_URL + team.getImgUrl();
            team.setImgUrl(imgUrl);
        }
        return ResultUtils.success(teamService.updateTeam(team,request));
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = teamService.deleteTeam(deleteRequest,request);
        return ResultUtils.success(b);
    }

    @PostMapping("/deleteUser")
    public BaseResponse<Boolean> deleteTeamUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = teamService.deleteTeamUser(deleteRequest,request);
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
        // 用户登录
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "未登录");
        // 校验用户积分是否足够
        boolean hasScore = userService.userHasScore(loginUser);
        ThrowUtils.throwIf(!hasScore, ErrorCode.PARAMS_ERROR, "用户积分不足");

        ThrowUtils.throwIf(chartRegenRequest == null, ErrorCode.PARAMS_ERROR);

        BIResponse biResponse = chartService.regenChartByAsyncMqFromTeam(chartRegenRequest, request);
        return ResultUtils.success(biResponse);
    }


    @PostMapping("/chart/add")
    public BaseResponse<BIResponse> addTeamChart(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        BIResponse biResponse = new BIResponse();
        return ResultUtils.success(biResponse);
    }


    @PostMapping("/chart/delete")
    public BaseResponse<BIResponse> deleteTeamChart(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        BIResponse biResponse = new BIResponse();
        return ResultUtils.success(biResponse);
    }


    @PostMapping("/chart/update")
    public BaseResponse<BIResponse> updateTeamChart(@RequestBody Team team, HttpServletRequest request){
        BIResponse biResponse = new BIResponse();
        return ResultUtils.success(biResponse);
    }


}