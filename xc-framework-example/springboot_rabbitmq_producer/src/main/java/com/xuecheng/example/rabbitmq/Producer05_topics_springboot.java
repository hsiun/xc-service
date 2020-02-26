package com.xuecheng.example.rabbitmq;

import com.xuecheng.example.rabbitmq.config.RabbitmqConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author growdane@gmail.com
 * @date 2020-02-12 16:48
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class Producer05_topics_springboot {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void testSendByTopics() {
        for (int i = 0; i < 5; i++) {
            String message = "sms email inform to user" + i;
            rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_TOPICS_INFORM, "inform.sms.email", message);
            System.out.println("Send Message is:'" + message + "'");
        }
    }
}
