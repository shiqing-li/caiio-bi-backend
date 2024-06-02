package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class FanoutConsumer {
    private final static String EXCHANGE_NAME = "fanout_exchange";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("localhost");

        final Connection connection = factory.newConnection();
        final Channel channel1 = connection.createChannel();
        final Channel channel2 =connection.createChannel();

        channel1.exchangeDeclare(EXCHANGE_NAME,"fanout");
        String queueName1 = "xiaoming";
        channel1.queueDeclare(queueName1,true,false,false,null);
        channel1.queueBind(queueName1,EXCHANGE_NAME,"");


        channel2.exchangeDeclare(EXCHANGE_NAME,"fanout");
        String queueName2 = "xiaoli";
        channel2.queueDeclare(queueName2,true,false,false,null);
        channel2.queueBind(queueName2,EXCHANGE_NAME,"");

        System.out.println("[x] watting for message,To exit press crtl + c");


        //预计设置为1，取消注释后，这样RabbitMQ就会在给消费者发送消息之前等待之前的消息被处理完
        //channel.basicQos(1);

        DeliverCallback deliverCallback1 =  (consumerTag,delivery) -> {
            String message = new String(delivery.getBody(),"UTF-8");
            System.out.println("[小王] Received :" + message  );
        };

        DeliverCallback deliverCallback2 =  (consumerTag,delivery) -> {
            String message = new String(delivery.getBody(),"UTF-8");
            System.out.println("[小李] Received :" + message  );
        };
        channel1.basicConsume(queueName1,false,deliverCallback1,consumerTag ->{});
        channel2.basicConsume(queueName2,false,deliverCallback2,consumerTag ->{});
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
