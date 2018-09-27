package com.makesailing.neo.queue.service.impl;

import com.makesailing.neo.queue.service.MessageQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * #
 *
 * @author jamie.li
 * @date 2018/9/27 11:08
 */
@Service(MessageQueueService.SERVICE_ID)
public class MessageQueueServiceImpl implements MessageQueueService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageQueueServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public void send(String exchange, String routingKey, Object message) {
		LOGGER.info("send info exchange [{}] , routingKey [{}] , message [{}]", exchange, routingKey, message);
		rabbitTemplate.convertAndSend(exchange, routingKey, message);
	}
}


