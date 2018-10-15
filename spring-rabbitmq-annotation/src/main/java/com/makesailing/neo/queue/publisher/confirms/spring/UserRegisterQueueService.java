package com.makesailing.neo.queue.publisher.confirms.spring;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #
 *
 * jamie.li
 * @date 2018/10/15 16:07
 */
@Slf4j
@Component
public class UserRegisterQueueService implements RabbitTemplate.ConfirmCallback {

	/**
	 * 消息队列模板
	 */
	@Autowired
	private RabbitTemplate rabbitTemplate;

	public void send(Object message, String exchange, String routingKey) throws Exception {
		//设置回调为当前类对象
		rabbitTemplate.setConfirmCallback(this);
		//构建回调id为uuid
		CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
		//发送消息到消息队列
		rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationId);
	}

	/**
	 * 消息回调确认方法
	 * @param correlationData 请求数据对象
	 * @param ack 是否发送成功
	 * @param cause
	 */
	@Override
	public void confirm(CorrelationData correlationData, boolean ack, String cause) {
		log.info(" ===== 消息进行消费了 ======");

		log.info(" 回调id:" + correlationData.getId());
		if (ack) {
			log.info("消息id为: " + correlationData + "的消息，已经被ack成功");
		} else {
			log.info("消息id为: " + correlationData + "的消息，消息nack，失败原因是：" + cause);
		}
	}
}


