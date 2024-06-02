package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class SingleConsumer {
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("localhost");

        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME,false,false,false,null);

        System.out.println("[x] Watting for message.To exit press CTRL + C");
        //定义如何处理消息，定义一个DeliverCallback来处理消接收到的消息
        DeliverCallback deliverCallback = (consumerTag,deliver) -> {
            //将消息转换成字符串
            String message = new String(deliver.getBody(), StandardCharsets.UTF_8);
            //控制台打印
            System.out.println("[x] '" +message+"'");
        };
        //在队列上消费队列中的消息，接收到的消息会交给DeliverCallback处理
        channel.basicConsume(QUEUE_NAME,true,deliverCallback,consumerTag -> {});
    }
}
