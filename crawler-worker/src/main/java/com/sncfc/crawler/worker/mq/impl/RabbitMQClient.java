package com.sncfc.crawler.worker.mq.impl;

import com.sncfc.crawler.worker.mq.IMQClient;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQClient implements IMQClient {
    private static final Logger logger = Logger.getLogger(RabbitMQClient.class);

    @Autowired
    private String exchangName;

    @Autowired
    private String routingKey;

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Override
    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(exchangName, routingKey, message);
    }
}
