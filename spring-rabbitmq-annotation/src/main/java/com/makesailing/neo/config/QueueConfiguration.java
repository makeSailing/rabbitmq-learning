package com.makesailing.neo.config;

import com.makesailing.neo.constant.ExchangeConstant;
import com.makesailing.neo.constant.QueueConstant;
import com.makesailing.neo.constant.RoutingKeyConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * # 队列配置
 *
 * @author jamie.li
 * @date 2018/9/26 17:46
 */
@Configuration
public class QueueConfiguration {

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

}



