package com.makesailing.neo.config;

import com.makesailing.neo.constant.ExchangeConstant;
import com.makesailing.neo.constant.QueueConstant;
import com.makesailing.neo.constant.RoutingKeyConstant;
import com.makesailing.neo.queue.consumer.DirectMessageConsumer;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * # 队列配置
 *
 * @author jamie.li
 * @date 2018/9/26 17:46
 */
@Configuration
public class QueueConfiguration extends RabbitMQConfiguration{

	// ########################   direct queue 配置  ####################################

	@Bean
	public DirectExchange directExchange() {
		DirectExchange directExchange = new DirectExchange(ExchangeConstant.DIRECT_EXCHAGE, true, false);
		return directExchange;
	}

	@Bean
	public Queue directQueue() {
		Queue queue = new Queue(QueueConstant.TEST_DIRECT_QUEUE, true, false, false);
		return queue;
	}

	@Bean
	public Binding directBinding() {
		Binding binding = BindingBuilder.bind(directQueue()).to(directExchange())
			.with(RoutingKeyConstant.DIRECT_ROUTING_KEY);
		return binding;
	}

	@Autowired
	private DirectMessageConsumer directMessageConsumer;

	@Bean
	public SimpleMessageListenerContainer listenerContainer() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory());
		container.setQueues(directQueue());
		container.setMessageListener(directMessageConsumer);
		// 如果设置了 MANUAL(手动),消费者那边需要手动答复,不能rabbit server 不会删除这个已经消费掉的消息 ,默认值是 AUTO
		//container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		return container;
	}

}



