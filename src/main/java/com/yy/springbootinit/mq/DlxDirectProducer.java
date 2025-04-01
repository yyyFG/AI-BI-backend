package com.yy.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.Scanner;

public class DlxDirectProducer {

  private static final String EXCHANGE_NAME = "dlx-direct_exchange";
    private static final String WORKD_EXCHANGE_NAME = "direct2_exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
        // 声明死信交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        // 创建队列，随机分配一个队列名称
        String queueName = "laoban_dlx_queue";
        channel.queueDeclare(queueName, true, false, false, null);;
        channel.queueBind(queueName, EXCHANGE_NAME, "laoban");

        String queueName2 = "waibao_dlx_quque";
        channel.queueDeclare(queueName2, true, false, false, null);
        channel.queueBind(queueName2, EXCHANGE_NAME, "waibao");

        DeliverCallback laobandeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");

            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            System.out.println(" [laoban] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        DeliverCallback waibaodeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");

            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            System.out.println(" [waibao] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        channel.basicConsume(queueName, true, laobandeliverCallback, consumerTag -> { });
        channel.basicConsume(queueName2, true, waibaodeliverCallback, consumerTag -> { });

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            String userInput = scanner.nextLine();
            String[] strings = userInput.split(" ");
            if(strings.length < 1) continue;
            String message = strings[0];
            String routingKey = strings[1];

            channel.basicPublish(WORKD_EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "'with routing:'" + routingKey + "'");
        }

    }
  }
  //..
}