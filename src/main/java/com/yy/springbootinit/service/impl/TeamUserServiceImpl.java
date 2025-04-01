package com.yy.springbootinit.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yy.springbootinit.mapper.TeamUserMapper;
import com.yy.springbootinit.model.entity.TeamUser;
import com.yy.springbootinit.service.TeamUserService;
import org.springframework.stereotype.Service;

/**
 * @author DCX
 * @description 针对表【team_user(队伍用户关系表)】的数据库操作Service实现
 * @createDate 2025-03-19 16:01:02
*/
@Service
public class TeamUserServiceImpl extends ServiceImpl<TeamUserMapper, TeamUser>
    implements TeamUserService {

}




