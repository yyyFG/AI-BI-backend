package com.yy.springbootinit.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yy.springbootinit.common.BaseResponse;
import com.yy.springbootinit.common.DeleteRequest;
import com.yy.springbootinit.common.ErrorCode;
import com.yy.springbootinit.common.ResultUtils;
import com.yy.springbootinit.constant.CommonConstant;
import com.yy.springbootinit.exception.BusinessException;
import com.yy.springbootinit.exception.ThrowUtils;
import com.yy.springbootinit.mapper.TeamMapper;
import com.yy.springbootinit.mapper.UserMapper;
import com.yy.springbootinit.model.dto.team.TeamAddRequest;
import com.yy.springbootinit.model.dto.team.TeamQueryRequest;
import com.yy.springbootinit.model.entity.Team;
import com.yy.springbootinit.model.entity.TeamChart;
import com.yy.springbootinit.model.entity.TeamUser;
import com.yy.springbootinit.model.entity.User;
import com.yy.springbootinit.model.vo.TeamVO;
import com.yy.springbootinit.service.TeamChartService;
import com.yy.springbootinit.service.TeamService;
import com.yy.springbootinit.service.TeamUserService;
import com.yy.springbootinit.service.UserService;
import com.yy.springbootinit.utils.DateUtil;
import com.yy.springbootinit.utils.SqlUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.yy.springbootinit.constant.CommonConstant.BASE_URL;

/**
 * @author DCX
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2025-03-19 16:01:02
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserService userService;

    @Resource
    private TeamUserService teamUserService;

    @Resource
    private TeamChartService teamChartService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request) {
        String name = teamAddRequest.getName();
        String imgUrl = BASE_URL + teamAddRequest.getImgUrl();
        String description = teamAddRequest.getDescription();
        Integer maxNum = teamAddRequest.getMaxNum();
        if (StringUtils.isEmpty(name)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称不能为空");
        }

        if (StringUtils.isEmpty(imgUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片不能为空");
        }

        if (StringUtils.isEmpty(description) || description.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不能为空或长度大于100");
        }
        if (maxNum == null || maxNum < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最大人数不得为空或者小于1");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        team.setImgUrl(imgUrl);
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        team.setUserId(userId);
        boolean b1 = this.save(team);
        TeamUser teamUser = new TeamUser();
        teamUser.setTeamId(team.getId());
        teamUser.setUserId(userId);
        boolean b2 = teamUserService.save(teamUser);
        return b1 && b2;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(DeleteRequest deleteRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long headerId = loginUser.getId();
        Long teamId = deleteRequest.getId();
        Team team = this.getById(teamId);
        ThrowUtils.throwIf(!headerId.equals(team.getUserId()), ErrorCode.NO_AUTH_ERROR, "不是队长无法删除队伍");

        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        QueryWrapper<TeamChart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        boolean b1 = teamChartService.remove(queryWrapper);
        QueryWrapper<TeamUser> teamUserQueryWrapper = new QueryWrapper<>();
        teamUserQueryWrapper.eq("teamId", teamId);
        boolean b2 = teamUserService.remove(teamUserQueryWrapper);
        boolean b3 = this.removeById(teamId);
        return b1 && b2 && b3;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeamUser(DeleteRequest deleteRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long headerId = loginUser.getId();
        Long userId = deleteRequest.getId();
        ThrowUtils.throwIf(userId.equals(headerId), ErrorCode.PARAMS_ERROR, "队长不能删除");

        TeamUser teamUser = teamUserService.getOne(new QueryWrapper<TeamUser>().eq("userId",userId));
        ThrowUtils.throwIf(teamUser == null, ErrorCode.PARAMS_ERROR, "队员为空");

        QueryWrapper<TeamUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        boolean result = teamUserService.remove(queryWrapper);

        return result;
    }

    @Override
    public Page<TeamVO> listTeam(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        String searchParam = teamQueryRequest.getSearchParams();
        long current = teamQueryRequest.getCurrent();
        long pageSize = teamQueryRequest.getPageSize();
        String sortField = teamQueryRequest.getSortField();
        String sortOrder = teamQueryRequest.getSortOrder();

        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(searchParam), "name", searchParam);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder), sortField);
        Page<Team> teamPage = this.page(new Page<>(current, pageSize), queryWrapper);
        List<Team> teamPageRecords = teamPage.getRecords();
        List<TeamVO> teamVOs = this.getTeamVOList(teamPageRecords, request);
        Page<TeamVO> teamVOPage = new Page<>(current, pageSize);
        teamVOPage.setRecords(teamVOs);
        teamVOPage.setTotal(teamPage.getTotal());
        return teamVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(Team team, HttpServletRequest request) {
        Long teamId = team.getId();
        team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        if (isInTeam(team, request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已在该队伍中");
        }
        QueryWrapper<TeamUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        long count = teamUserService.count(queryWrapper);
        if (count >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数已满");
        }
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        TeamUser teamUser = new TeamUser();
        teamUser.setTeamId(teamId);
        teamUser.setUserId(userId);
        boolean b = teamUserService.save(teamUser);
        return b;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean exitTeam(Team team, HttpServletRequest request) {
        Long teamId = team.getId();
        team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        if (!isInTeam(team, request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不在队伍中");
        }
        QueryWrapper<TeamUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        long count = teamUserService.count(queryWrapper);
        if (count <= 1) {
            queryWrapper.eq("userId", userId);
            boolean b1 = teamUserService.remove(queryWrapper);
            boolean b2 = this.removeById(teamId);
            QueryWrapper<TeamChart> teamChartQueryWrapper = new QueryWrapper<>();
            teamChartQueryWrapper.eq("teamId", teamId);
            boolean b3 = teamChartService.remove(teamChartQueryWrapper);
            return b1 && b2 && b3;
        }

        queryWrapper.orderBy(true, false, "createTime").last("limit 2");
        TeamUser teamUser = teamUserService.list(queryWrapper).get(1);
        Long newCaptainId = teamUser.getUserId();
        team.setUserId(newCaptainId);
        boolean b1 = this.updateById(team);
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId).eq("userId", userId);
        boolean b2 = teamUserService.remove(queryWrapper);
        return b1 && b2;
    }

    @Override
    public Page<TeamVO> pageMyJoinedTeam(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        String searchParam = teamQueryRequest.getSearchParams();
        long current = teamQueryRequest.getCurrent();
        long pageSize = teamQueryRequest.getPageSize();
        String sortField = teamQueryRequest.getSortField();
        String sortOrder = teamQueryRequest.getSortOrder();

        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        QueryWrapper<TeamUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder), sortField);
        Page<TeamUser> teamUserPage = teamUserService.page(new Page<>(current, pageSize), queryWrapper);
        List<Long> teamIds = teamUserPage.getRecords().stream().map(TeamUser::getTeamId)
                .collect(Collectors.toList());
        List<Team> teams = this.listByIds(teamIds);
        List<TeamVO> teamVOs = this.getTeamVOList(teams, request);
        Page<TeamVO> teamVOPage = new Page<>(current, pageSize);
        teamVOPage.setRecords(teamVOs);
        teamVOPage.setTotal(teamUserPage.getTotal());
        return teamVOPage;
    }

    @Override
    public List<User> pageMyTeamUser(TeamUser teamUser, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long headerId = loginUser.getId();
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",headerId);
        Team team = teamMapper.selectOne(queryWrapper);
        Long id = team.getId();
        QueryWrapper<TeamUser> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("teamId",id);

        List<TeamUser> teamUserList = teamUserService.list(queryWrapper2);
        List<Long> userIds = teamUserList.stream()
                .map(TeamUser::getUserId)  // 提取 userId
                .collect(Collectors.toList());  // 收集成 List
//        System.out.println(userIds);
        List<User> users = userMapper.selectList(new QueryWrapper<User>().in("id", userIds).ne("id",headerId).select("id", "userName"));
        // 提取 userName
        List<String> userNames = users.stream()
                .map(User::getUserName)
                .collect(Collectors.toList());
        System.out.println(userNames);

        return users;
    }

    @Override
    public List<Team> listAllMyJoinedTeam(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        List<Team> teams = this.list(queryWrapper);
        List<Long> teamIds = teams.stream().map(Team::getId).collect(Collectors.toList());

        return this.listByIds(teamIds);
    }

    @Override
    public Page<Team> pageTeam(TeamQueryRequest teamQueryRequest) {
        String searchParams = teamQueryRequest.getSearchParams();
        long current = teamQueryRequest.getCurrent();
        long pageSize = teamQueryRequest.getPageSize();
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(searchParams), "name", searchParams);
        queryWrapper.like(StringUtils.isNotEmpty(searchParams), "description", searchParams);
        Page<Team> teamPage = this.page(new Page<>(current, pageSize), queryWrapper);
        return teamPage;
    }

    @Override
    public Boolean updateTeam(Team team, HttpServletRequest request) {
        Long teamId = team.getId();
        Team oldTeam = this.getById(teamId);

        if(team.getName() == null) team.setName(oldTeam.getName());
        if(team.getDescription() == null) team.setDescription(oldTeam.getDescription());
        if(team.getMaxNum() == null) team.setMaxNum(oldTeam.getMaxNum());

        if(!oldTeam.getName().equals(team.getName())){
            String teamName = team.getName();
            if(this.count(new QueryWrapper<Team>().eq("name", teamName)) > 0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名已存在");
            }
        }
        return this.updateById(team);
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

    private List<TeamVO> getTeamVOList(List<Team> teams, HttpServletRequest request) {
        return teams.stream().map(team -> {
            TeamVO teamVO = new TeamVO();
            BeanUtils.copyProperties(team, teamVO);
            teamVO.setUserVO(userService.getUserVOById(team.getUserId()));
            teamVO.setInTeam(this.isInTeam(team, request));
            return teamVO;
        }).collect(Collectors.toList());
    }


}




