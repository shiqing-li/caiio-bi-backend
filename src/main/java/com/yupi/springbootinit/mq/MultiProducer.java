package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class MultiProducer {

    private final static String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] argv) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("localhost");

        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()){
            channel.queueDeclare(TASK_QUEUE_NAME,true,false,false,null);

            Scanner scanner = new Scanner(System.in);
            while(scanner.hasNext()){
                //创建要发送到的消息
                String message = scanner.nextLine();
                //使用channel.basicPublish方法将消息发送到指定的队列中，
                channel.basicPublish("",TASK_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes(StandardCharsets.UTF_8));
                //
                System.out.println("[x] Sent '" + message  + "'");
            }

        }
    }
}
