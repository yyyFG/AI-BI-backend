package com.yy.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yy.springbootinit.constant.BIMqConstant;

public class BIinitMain {

//    public static void main(String[] args) {
//        // 创建连接
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("localhost");
//        try {
//            Connection connection = factory.newConnection();
//            Channel channel = connection.createChannel();
//            String EXCHANGE_NAME = BIMqConstant.BI_EXCHANGE_NAME;
//            // 声明交换机
//            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
//
//            // 创建队列，随机分配一个队列名称
//            String queueName = BIMqConstant.BI_QUEUE_NAME;
//            channel.queueDeclare(queueName, true, false, false, null);
//            channel.queueBind(queueName, EXCHANGE_NAME, BIMqConstant.BI_ROUTING_KEY);
//
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }
}
