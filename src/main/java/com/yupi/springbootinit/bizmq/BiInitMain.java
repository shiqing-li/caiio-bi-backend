package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class BiInitMain {
    public static void main(String[] args) {
        try{
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();

            Channel channel = connection.createChannel();

            channel.queueDeclare(BiMqConstant.BI_QUEUE_NAME,true,false,false,null);

            channel.exchangeDeclare(BiMqConstant.BI_EXCHANGE_NAME,"direct");

            channel.queueBind(BiMqConstant.BI_QUEUE_NAME,BiMqConstant.BI_EXCHANGE_NAME,BiMqConstant.BI_ROUTING_KEY);
        }catch (IOException e){

        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
