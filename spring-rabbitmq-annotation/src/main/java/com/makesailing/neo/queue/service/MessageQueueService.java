package com.makesailing.neo.queue.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * # 消息队列服务接口
 *
 * @author jamie.li
 * @date 2018/9/27 11:02
 */
public interface MessageQueueService extends RabbitTemplate.ConfirmCallback {

	String SERVICE_ID = "messageQueueService";

	/**
	 * 发送消息到队列
	 * @param exchange
	 * @param routingKey
	 * @param message
	 */
	public void send(String exchange, String routingKey, Object message);


	/**
	 * 延迟发送消息到队列
	 * @param queueName 队列名称
	 * @param message 消息内容
	 * @param times 延迟时间 单位毫秒
	 */
	public void sendDeadLetterMsg(String queueName,String message,long times);
}
