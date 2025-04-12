package com.yy.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yy.springbootinit.common.BaseResponse;
import com.yy.springbootinit.common.DeleteRequest;
import com.yy.springbootinit.model.dto.team.TeamAddRequest;
import com.yy.springbootinit.model.dto.team.TeamQueryRequest;
import com.yy.springbootinit.model.entity.Team;
import com.yy.springbootinit.model.entity.TeamUser;
import com.yy.springbootinit.model.entity.User;
import com.yy.springbootinit.model.vo.TeamVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author DCX
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2025-03-19 16:01:02
*/
public interface TeamService extends IService<Team> {

    boolean addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request);

    Page<TeamVO> listTeam(TeamQueryRequest teamQueryRequest, HttpServletRequest request);

    boolean joinTeam(Team team, HttpServletRequest request);

    boolean exitTeam(Team team, HttpServletRequest request);

    Page<TeamVO> pageMyJoinedTeam(TeamQueryRequest teamQueryRequest, HttpServletRequest request);

    List<Team> listAllMyJoinedTeam(HttpServletRequest request);

    Page<Team> pageTeam(TeamQueryRequest teamQueryRequest);

    Boolean updateTeam(Team team, HttpServletRequest request);

    List<User> pageMyTeamUser(TeamUser teamUser, HttpServletRequest request);

    boolean deleteTeamUser(DeleteRequest deleteRequest, HttpServletRequest request);

    boolean deleteTeam(DeleteRequest deleteRequest, HttpServletRequest request);
}
