package com.xuecheng.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author growdane@gmail.com
 * @date 2020-02-12 15:38
 */

public class Producer01 {

    private static final String QUEUE = "gao";

    public static void main(String[] args) {
        Connection connection = null;
        Channel channel = null;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        //rabbitmq默认虚拟机名称为“/”，虚拟机相当于一个独立的mq服务
        factory.setVirtualHost("/");
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE, true, false, false, null);
            String message = "hello, gao " + System.currentTimeMillis();
            channel.basicPublish("", QUEUE, null, message.getBytes());
            System.out.println("Send Message is:'" + message + "'");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


    }
}
