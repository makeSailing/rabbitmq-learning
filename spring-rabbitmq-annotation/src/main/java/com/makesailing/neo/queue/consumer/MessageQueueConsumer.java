package com.makesailing.neo.queue.consumer;

import com.makesailing.neo.constant.QueueConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * # 消费队列 - 消费者
 *
 * @author jamie.li
 * @date 2018/9/27 14:14
 */
@Component
@RabbitListener(queues = QueueConstant.DIRECT_QUEUE)
public class MessageQueueConsumer {

	public static final Logger LOGGER = LoggerFactory.getLogger(MessageQueueConsumer.class);

	@RabbitHandler
	public void directQueueProcess(String content) {
		LOGGER.info("directQueueProcess receive messgage content [{}]", content);

		//TODO 省略其业务逻辑
	}
}


