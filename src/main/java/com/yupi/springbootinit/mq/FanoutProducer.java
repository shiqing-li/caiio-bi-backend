package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class FanoutProducer {
    private final static String EXCHANGE_NAME = "fanout_exchange";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()){
            channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
            Scanner scanner = new Scanner(System.in);
            while(scanner.hasNext()){
                String message = scanner.nextLine();
                channel.basicPublish(EXCHANGE_NAME,"",null,message.getBytes(StandardCharsets.UTF_8));
                System.out.println("[x] Sent '" + message +"'");
            }
//            String message = args.length < 1 ? "info: Hello World!" : String.join(" ",args);
//            channel.basicPublish(EXCHANGE_NAME,"",null,message.getBytes(StandardCharsets.UTF_8));
//            System.out.println("[x] Sent '" + message +"'");
        }
    }
}
