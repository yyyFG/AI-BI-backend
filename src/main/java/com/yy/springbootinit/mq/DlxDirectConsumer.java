package com.yy.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

public class DlxDirectConsumer {

  private static final String WORKD_EXCHANGE_NAME = "direct2_exchange";
  private static final String DEA_EXCHANGE_NAME = "dlx-direct_exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(WORKD_EXCHANGE_NAME, "direct");

    // 指定死信队列参数
    Map<String, Object> args = new HashMap<String, Object>();
    // 要绑定到哪个交换机
    args.put("x-dead-letter-exchange", DEA_EXCHANGE_NAME);
    // 指定死信要转发到哪个死信队列
    args.put("x-dead-letter-exchange-routing-key", "waibao");

    String queueName = "dog_queue";
    channel.queueDeclare(queueName, true, false, false, args);;
    channel.queueBind(queueName, WORKD_EXCHANGE_NAME, "dog");

    // 创建队列，指定消息过期参数
    Map<String, Object> args2 = new HashMap<String, Object>();
    args2.put("x-dead-letter-exchange", DEA_EXCHANGE_NAME);
    args2.put("x-dead-letter-exchange-routing-key", "laoban");

    String queueName2 = "cat_queue";
    channel.queueDeclare(queueName2, true, false, false, args2);
    channel.queueBind(queueName2, WORKD_EXCHANGE_NAME, "cat");


    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");

        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
        System.out.println(" [小王] Received '" +
            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };

      DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");

          channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
          System.out.println(" [小李] Received '" +
                  delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
      };
    channel.basicConsume(queueName, false, deliverCallback1, consumerTag -> { });
    channel.basicConsume(queueName2, false, deliverCallback2, consumerTag -> { });
  }


}