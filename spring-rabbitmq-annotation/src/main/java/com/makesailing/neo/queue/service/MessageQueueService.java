package com.makesailing.neo.queue.service;

/**
 * # 消息队列服务接口
 *
 * @author jamie.li
 * @date 2018/9/27 11:02
 */
public interface MessageQueueService {

	String SERVICE_ID = "messageQueueService";

	/**
	 * 发送消息到队列
	 * @param exchange
	 * @param routingKey
	 * @param message
	 */
	public void send(String exchange, String routingKey, Object message);
}
