package com.makesailing.neo.queue.publisher.confirms.spring;

import com.alibaba.fastjson.JSON;
import com.makesailing.neo.domain.User;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

/**
 * #
 *
 * jamie.li
 * @date 2018/10/15 15:56
 */
@Slf4j
@EnableRabbit
@ComponentScan
@ImportResource({"classpath:/applicationContext.xml"})
public class SpringSend {

	public static User registerUser() {
		User user = new User();
		user.setUsername("jamie");
		user.setPassword("123456");
		user.setEmail("123@qq.com");
		user.setRegip("127.0.0.1");
		return user;
	}

	public static void sendEmail(User user) {
		// 发送用户注册邮件操作
		log.info("发送用户注册邮件操作 user info [{}]", JSON.toJSONString(user));
	}

	public static void main(String[] args) throws Exception {

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringSend.class);

		User user = registerUser();

		sendEmail(user);

		String userJson = JSON.toJSONString(user);
		UserRegisterQueueService registerQueueService = context.getBean(UserRegisterQueueService.class);

		registerQueueService.send(userJson,"test.order.direct.exchange","test.order.routing.key");

		TimeUnit.SECONDS.sleep(30);

		context.close();

	}

	/**
	 * 总结 :::
	 * 在容器中的ConnectionFactory实例中加上setPublisherConfirms属性
	 factory.setPublisherConfirms(true);
	 在RabbitTemplate实例中增加setConfirmCallback回调方法。
	 发送消息的时候，需要指定CorrelationData，用于标识该发送的唯一id。
	 对比与java client的publisher confirm：
	 1.spring amqp不支持批量确认，底层的rabbitmq java client方式支持批量确认。
	 2.spring amqp提供的方式更加的简单明了。
	 */


}


