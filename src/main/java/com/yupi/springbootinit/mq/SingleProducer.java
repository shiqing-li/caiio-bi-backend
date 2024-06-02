package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

public class SingleProducer {
    public static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws Exception{
        ConnectionFactory connectionFactory = new ConnectionFactory();

        connectionFactory.setHost("localhost");

        try(Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();){
            //通道上生命一个队列
            channel.queueDeclare(QUEUE_NAME,false,false,false,null);
            //创建要发送到的消息
            String message = "Hello world";
            //使用channel.basicPublish方法将消息发送到指定的队列中，
            channel.basicPublish("",QUEUE_NAME,null,message.getBytes(StandardCharsets.UTF_8));
            //
            System.out.println("[x] Sent '" + message  + "'");
        }
    }
}
