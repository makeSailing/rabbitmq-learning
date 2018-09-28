package com.makesailing.neo.queue.service.impl;

import com.alibaba.fastjson.JSON;
import com.makesailing.neo.constant.DLXMessage;
import com.makesailing.neo.constant.ExchangeConstant;
import com.makesailing.neo.constant.RoutingKeyConstant;
import com.makesailing.neo.queue.service.MessageQueueService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
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
		//设置回调为当前类对象
		//rabbitTemplate.setConfirmCallback(this);
		//构建回调id为uuid
		//CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
		//String[] routingKeys = {routingKey, RoutingKeyConstant.DIRECT_DEAD_MAIL_QUEUE_FAIL,
		//	RoutingKeyConstant.MAIL_QUEUE_ROUTING_KEY};
		//for (int i = 0; i < 10; i++) {
		//	rabbitTemplate.convertAndSend(exchange, routingKeys[i % 3], message + " >>> " + i);
		//}
			rabbitTemplate.convertAndSend(exchange, routingKey, message);

	}

	@Override
	public void sendDeadLetterMsg(String queueName, String message, long times) {
		LOGGER.info("sendDeadLetterMsg info queueName [{}] , message [{}] , times [{}]", queueName, message, times);
		DLXMessage dlxMessage = new DLXMessage(queueName, message, times);
		MessagePostProcessor processor = messagePostProcessor -> {
			messagePostProcessor.getMessageProperties().setExpiration(times + "");
			return messagePostProcessor;
		};
		//设置回调为当前类对象
		rabbitTemplate.setConfirmCallback(this);
		//构建回调id为uuid
		CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
		dlxMessage.setExchange(ExchangeConstant.DIRECT_EXCHAGE);
		rabbitTemplate.convertAndSend(ExchangeConstant.DIRECT_EXCHAGE, RoutingKeyConstant.DIRECT_DEAD_LETTER_ROUTING_KEY,
			JSON.toJSONString(dlxMessage), processor,correlationId);

	}

	@Override
	public void confirm(CorrelationData correlationData, boolean ack, String cause) {
		LOGGER.info(" 回调id:" + correlationData.getId());
		if (ack) {
			LOGGER.info("消息发送成功");
		} else {
			LOGGER.info("消息发送失败:" + cause);
		}
	}
}


