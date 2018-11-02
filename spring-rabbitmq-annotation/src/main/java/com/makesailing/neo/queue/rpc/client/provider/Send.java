package com.makesailing.neo.queue.rpc.client.provider;

import com.makesailing.neo.common.ConnectionUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;

/**
 * # RPC 客户端发送消息
 * RPC Client步骤：
 * 1.监听reply_to对应的队列(RPC调用结果发送指定的队列）
 * 2.发送消息，消息属性需要带上reply_to，correlation_id属性
 * 3.服务端处理完成之后，reply_to对应的队列就会收到异步处理结果消息
 * 4.收到消息之后，进行处理，根据消息属性的correlation_id找到对应的请求
 * 5.一次客户端调用就完成了。
 *
 * @author jamie
 * @date 2018/10/27 10:56
 */
@Slf4j
public class Send {

	public static final String MESSAGE = "Hello RPC RabbitMQ";
	public static final String RPC_EXCHANGE = "test.rpc.exchange";
	public static final String RPC_ROUTING_KEY = "test.rpc.routing.key";

	public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
		// 获取连接
		Connection connection = ConnectionUtils.getConnection();
		// 创建信道 mango Receive
		Channel channel = connection.createChannel();

		// 预先定义响应结果,即预先订阅响应结果的队列,先订阅响应结果的队列,再发送消息到请求队列
		String correlationId = UUID.randomUUID().toString();
		String replyTo = channel.queueDeclare().getQueue();
		// 监听服务端的回调
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
				throws IOException {
				// 判断是否自己发送的
				if (Objects.equals(properties.getCorrelationId(), correlationId)) {
					String message = new String(body, "UTF-8");
					log.info("已经收到服务器的响应结果 [{}]", message);
				}

			}
		};

		channel.basicConsume(replyTo, true, consumer);

		// 将消息发送到请求队列
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().replyTo(replyTo)
			.correlationId(correlationId).deliveryMode(2).contentEncoding("UTF-8").build();

		channel.basicPublish(RPC_EXCHANGE, RPC_ROUTING_KEY, properties, MESSAGE.getBytes());
		log.info("已发出请求请求消息：" + MESSAGE);

		TimeUnit.SECONDS.sleep(20);

		channel.close();
		connection.close();
	}

}


