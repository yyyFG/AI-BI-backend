package com.yy.springbootinit.bizmq;


import com.yy.springbootinit.constant.BIMqConstant;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class BIProducer {


//    @Resource
//    private RabbitTemplate rabbitTemplate;
//
//
//    /**
//     * 发送消息
//     * @param message
//     */
//    public void sendMessage(String message){
//        rabbitTemplate.convertAndSend(BIMqConstant.BI_EXCHANGE_NAME, BIMqConstant.BI_ROUTING_KEY, message);
//    }
}
