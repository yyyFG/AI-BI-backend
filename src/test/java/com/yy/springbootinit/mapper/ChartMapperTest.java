package com.yy.springbootinit.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ChartMapperTest {

    @Resource
    private ChartMapper chartMapper;

    @Test
    void queryChatData() {
        String chartId = "1904224260218466305";
        String querySql = String.format("select * from chart_%s",chartId);
//        List<Map<String, Object>> resultData = chartMapper.queryChartData(querySql);
//        System.out.println(resultData);
    }
}