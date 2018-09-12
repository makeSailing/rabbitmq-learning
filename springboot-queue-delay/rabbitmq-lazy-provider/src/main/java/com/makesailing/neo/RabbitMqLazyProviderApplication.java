package com.makesailing.neo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * #
 *
 * @author jamie
 * @date 2018/9/12 15:41
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.makesailing.neo")
public class RabbitMqLazyProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(RabbitMqLazyProviderApplication.class, args);
		log.info("RabbitMq 延迟提供者启动成功 ~~~");
	}
}


