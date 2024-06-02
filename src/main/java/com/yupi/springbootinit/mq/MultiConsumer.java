package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MultiConsumer {
    private final static String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("localhost");

        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.queueDeclare(TASK_QUEUE_NAME,true,false,false,null);
        System.out.println("[x] watting for message,To exit press crtl + c");

        //预计设置为1，取消注释后，这样RabbitMQ就会在给消费者发送消息之前等待之前的消息被处理完
        //channel.basicQos(1);

        DeliverCallback deliverCallback =  (consumerTag,delivery) -> {
            String message = new String(delivery.getBody(),"UTF-8");

            try{
                System.out.println("[x] received '" + message + "'");
                Thread.sleep(20000);
            }catch(InterruptedException e){
                e.printStackTrace();
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
            }finally{
                System.out.println("[x] Done");
                //手动发送应答，告诉RabbitMQ 消息已经被处理
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);

            }

//            System.out.println("[x] received '" + message + "'");
//            try{
//                doWork(message);
//            }finally {
//                System.out.println("[x] Done");
//                channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
//            }
        };
        channel.basicConsume(TASK_QUEUE_NAME,false,deliverCallback,consumerTag ->{});
    }

//    private static void doWork(String task){
//        for (char ch: task.toCharArray()) {
//            if (ch == '.') {
//                try{
//                    Thread.sleep(1000);
//                }catch(InterruptedException _ignored){
//                    Thread.currentThread().interrupt();
//                }
//            }
//        }
//    }
}
