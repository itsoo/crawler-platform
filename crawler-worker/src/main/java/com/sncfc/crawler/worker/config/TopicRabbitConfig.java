package com.sncfc.crawler.worker.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicRabbitConfig {
	@Value("${config.rabbitmq.exchang.name}")
	private String mqExchangName;

	@Value("${config.rabbitmq.routingKey}")
	private String routingKey;

	@Value("${config.rabbitmq.queue.name}")
	private String mqQueueName;

	@Value("${config.rabbitmq.queue.exclusive}")
	private boolean mqQueueExclusive;

	@Value("${config.rabbitmq.durable}")
	private boolean durable;

	@Value("${config.rabbitmq.autoDelete}")
	private boolean autoDelete;

	@Bean("exchangName")
	public String getExchangName() {
		return mqExchangName;
	}

	@Bean("routingKey")
	public String getRoutingKey() {
		return routingKey;
	}

	@Bean
	public Queue queueMessage() {
		return new Queue(mqQueueName);
	}

	@Bean
	TopicExchange exchange() {
		return new TopicExchange(mqExchangName);
	}

	@Bean
	Binding bindingExchangeMessage(Queue queueMessage, TopicExchange exchange) {
		return BindingBuilder.bind(queueMessage).to(exchange).with(routingKey);
	}

}
