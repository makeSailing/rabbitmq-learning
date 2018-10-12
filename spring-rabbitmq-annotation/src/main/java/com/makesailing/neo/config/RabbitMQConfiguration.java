package com.makesailing.neo.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.MessageConverter;
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

	@Value("${rabbitmq.virtualHost}")
	private String virtualHost;

	@Value("${rabbitmq.publisher.confirms}")
	private Boolean publisherConfirms;

	@Value("${rabbitmq.channelCacheSize}")
	private Integer channelCacheSize;

	/**
	 * 连接信息
	 */
	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost(host);
		connectionFactory.setPort(port);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
		connectionFactory.setVirtualHost(virtualHost);
		connectionFactory.setPublisherConfirms(publisherConfirms);

		return connectionFactory;
	}

	/**
	 * @RabbitListener 注解需要Bean
	 * 添加 监听容器工厂,那么 SimpleMessageListenerContainer 就不需要了,默认采用的是 SimpleMessageConverter进行消息转换
	 * 也可以在 SimpleRabbitListenerContainerFactory 自定义消费转换器
	 * 如果 ContentType 为 test/pain,那么消费端类型就必须为String类型进行接收,如果是byte[]进行接收就会进行报错,当然使用Message是可以的
	 * @param connectionFactory
	 * @return
	 */
	@Bean
	public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(ConnectionFactory connectionFactory){
		//SimpleRabbitListenerContainerFactory发现消息中有content_type有text就会默认将其转换成string类型的
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		//factory.setMessageConverter(new TestMessageConverter());
		return factory;
	}

	@Bean
	public RabbitAdmin rabbitAdmin() {
		RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory());
		return rabbitAdmin;
	}


	/**
	 * rabbitmq 模版
	 */
	@Bean
	public RabbitTemplate rabbitTemplate() {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
		// 默认的采用 jackson ,因为fastjson 强大,这里采用 fastjson进行转换
		//rabbitTemplate.setMessageConverter(messageConverter());
		return rabbitTemplate;
	}

	//@Bean
	public MessageConverter messageConverter() {
		return new FastJsonMessageConverter();
	}


}


