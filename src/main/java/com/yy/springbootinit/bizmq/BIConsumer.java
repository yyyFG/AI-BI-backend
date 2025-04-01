package com.yy.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.yy.springbootinit.common.ErrorCode;
import com.yy.springbootinit.constant.BIMqConstant;
import com.yy.springbootinit.exception.BusinessException;
import com.yy.springbootinit.manager.TestDeepSeekAiManager;
import com.yy.springbootinit.model.entity.Chart;
import com.yy.springbootinit.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
@Slf4j
public class BIConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private TestDeepSeekAiManager testDeepSeekAiManager;

    // 指定程序监听的消息队列和确认机制
    @RabbitListener(queues = {BIMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receivedMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        if(StringUtils.isBlank(message)){
            // 如果失败，消息拒绝
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if(chart == null){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表为空");
        }
        // 先修改图表任务状态为 “执行中”, 等待执行成功后，修改为“已完成”，保存执行结果；执行失败后，状态修改为：“失败”，记录任务失败信息
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean b = chartService.updateById(updateChart);
        if(!b){
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(), "图表状态执行中更改失败");
            return;
        }
        // 调用AI
        String result = testDeepSeekAiManager.doChat(buildUserInput(chart));
        // 获取结果
        String[] splits = result.split("【【【【【");
//        System.out.println(splits.length);
//        System.out.println(splits[0]);
//
        if(splits.length < 3){
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(), "生成错误");
            return;
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);

        updateChartResult.setStatus("succeed");
        boolean updateResult = chartService.updateById(updateChartResult);
        if(!updateResult){
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
            return;
        }
        // 消息确认
        channel.basicAck(deliveryTag,false);
    }


    /**
     * 构建用户输入
     * @param chart
     * @return
     */
    private String buildUserInput(Chart chart){
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        //  压缩后的数据
        String csvData = chart.getChartData();


        // 用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal += ". 请使用" + chartType;
        }

        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");

        return userInput.toString();
    }

    private void handleChartUpdateError(long chartId, String execMessage){
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("failed");
        updateChart.setExecMessage("execMessage");
        boolean b = chartService.updateById(updateChart);
        if(!b){
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }


}
