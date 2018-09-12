package com.makesailing.neo.config;

import com.makesailing.neo.enums.QueueEnum;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * #
 *
 * @author jamie
 * @date 2018/9/12 14:00
 */
@Configuration
public class MessageRabbitMqConfiguration {

	/**
	 * 消息中心实际消费队列交换配置
	 * @return
	 */
	@Bean
	public DirectExchange messageDirect() {
		DirectExchange directExchange = (DirectExchange) ExchangeBuilder
			.directExchange(QueueEnum.MESSAGE_QUEUE.getExchange()).durable(true).build();
		return directExchange;
	}

	/**
	 * 消息中心延迟消息队列配置
	 * @return
	 */
	@Bean
	public DirectExchange messageTtlDirect() {
		DirectExchange directExchange = (DirectExchange) ExchangeBuilder
			.directExchange(QueueEnum.MESSAGE_TTL_QUEUE.getExchange()).durable(true).build();
		return directExchange;
	}

	/**
	 * 消息中心实际消费队列
	 * @return
	 */
	@Bean
	public Queue messageQueue() {
		Queue queue = QueueBuilder.durable(QueueEnum.MESSAGE_QUEUE.getName()).build();
		return queue;
	}

	/**
	 * 消息中心 Ttl 队列
	 * @return
	 */
	@Bean
	public Queue messageTtlQueue() {
		Queue queue = QueueBuilder.durable(QueueEnum.MESSAGE_TTL_QUEUE.getName())
			.withArgument("x-dead-letter-exchange", QueueEnum.MESSAGE_QUEUE.getExchange())
			.withArgument("x-dead-letter-routing-key",QueueEnum.MESSAGE_QUEUE.getRoutingKey()).build();
		return queue;
	}

	/**
	 * 消息中心实际消息交换与队列绑定
	 * @param messageDirect
	 * @param messageQueue
	 * @return
	 */
	@Bean
	public Binding messageBinding(DirectExchange messageDirect, Queue messageQueue) {
		Binding binding = BindingBuilder.bind(messageQueue).to(messageDirect)
			.with(QueueEnum.MESSAGE_QUEUE.getRoutingKey());
		return binding;
	}

	/**
	 * 消息中心TTL绑定实际消息中心实际消费交换机
	 * @param messageTtlDirect
	 * @param messageTtlQueue
	 * @return
	 */
	@Bean
	public Binding messageTtlBinding(DirectExchange messageTtlDirect, Queue messageTtlQueue) {
		Binding binding = BindingBuilder.bind(messageTtlQueue).to(messageTtlDirect)
			.with(QueueEnum.MESSAGE_TTL_QUEUE.getRoutingKey());
		return binding;
	}

}


