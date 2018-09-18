package com.makesailing.neo.provider;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * # 生产者
 *
 * @author jamie.li
 * @date 2018/9/18 11:36
 */
public class Send {

	private static final String EXCHANGE_NAME = "test_exchange_topic";
	private static final String ROUTING_KEY = "quick.orange.rabbit";

	public static void main(String[] args) throws IOException, TimeoutException {
		// 创建连接
		Connection connection = ConnectionUtils.getConnection();
		// 获取信道
		Channel channel = connection.createChannel();

		// 声明一个 topic 交换机
		channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

		// 发送消息
		String message = "Hello , quick.orange.rabbit";

		channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, message.getBytes());

		System.out.println("[ Send ] " + message);

		// 关闭连接
		channel.close();
		connection.close();
	}
}


