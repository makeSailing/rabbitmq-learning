package com.makesailing.neo.consumer;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * #
 *
 * @author jamie
 * @date 2018/9/12 15:57
 */
@Slf4j
@Component
@RabbitListener(queues = "message.center.create")
public class MessageConsumer {

	@RabbitHandler
	public void handler(String content) {
		log.info("消息内容 : {}", content);
		log.info("消费结束时间 : [{}]", LocalDateTime.now());
	}
}


