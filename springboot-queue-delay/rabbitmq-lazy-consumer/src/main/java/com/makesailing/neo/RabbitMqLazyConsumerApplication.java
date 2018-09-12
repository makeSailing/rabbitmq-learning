package com.makesailing.neo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * # 延迟消息 消费者
 *
 * @author jamie
 * @date 2018/9/12 15:54
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.makesailing.neo")
public class RabbitMqLazyConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RabbitMqLazyConsumerApplication.class, args);
		log.info("延迟消息 消费者 启动成功 ~~~");
	}
}


