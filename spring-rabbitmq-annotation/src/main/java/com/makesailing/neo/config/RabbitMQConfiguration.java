package com.makesailing.neo.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * #
 *
 * @author jamie.li
 * @date 2018/9/26 17:01
 */
@Configuration
public class RabbitMQConfiguration {


	@Value("${rabbitmq.host}")
	private String host;

	@Value("${rabbitmq.port}")
	private Integer port;

	@Value("${rabbitmq.username}")
	private String username;

	@Value("${rabbitmq.password}")
	private String password;

	@Value("${rabbitmq.vhost}")
	private String virtualHost;

	@Value("${rabbitmq.channelCacheSize}")
	private Integer channelCacheSize;

	/**
	 * 连接信息
	 * @return
	 */
	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost(host);
		connectionFactory.setPort(port);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
		connectionFactory.setVirtualHost(virtualHost);
		connectionFactory.setConnectionCacheSize(channelCacheSize);
		return connectionFactory;
	}

	/**
	 *  rabbitmq 模版
	 * @return
	 */
	@Bean
	public RabbitTemplate rabbitTemplate() {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
		// 默认的采用 jackson ,因为fastjson 强大,这里采用 fastjson进行转换
		rabbitTemplate.setMessageConverter(new FastJsonMessageConverter());
		return rabbitTemplate;
	}
}


