package com.yy.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yy.springbootinit.mapper.TeamChartMapper;
import com.yy.springbootinit.model.entity.TeamChart;
import com.yy.springbootinit.service.TeamChartService;
import org.springframework.stereotype.Service;

/**
 * @author DCX
 * @description 针对表【team_chart(队伍图表关系表)】的数据库操作Service实现
 * @createDate 2025-03-19 16:01:02
*/
@Service
public class TeamChartServiceImpl extends ServiceImpl<TeamChartMapper, TeamChart>
    implements TeamChartService {

}




