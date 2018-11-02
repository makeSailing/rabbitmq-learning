package com.makesailing.neo.queue.rpc.client.consumer;

import com.makesailing.neo.common.ConnectionUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;

/**
 * # 消息接收
 * RPC Server步骤：
 * 1.创建服务
 * 2.监听一个队列（sms），监听客户端发送的消息
 * 3.收到消息之后，调用服务，得到调用结果
 * 4.从消息属性中，获取reply_to, correlation_id属性，把调用结果发送给reply_to指定的队列中，发送的消息属性要带上reply_to。
 * 5.一次调用处理成功
 *
 * @author jamie
 * @date 2018/10/27 11:20
 */
@Slf4j
public class Receive {

	public static final String RPC_QUEUQU = "test.rpc.queue";
	public static final String RPC_EXCHANGE = "test.rpc.exchange";
	public static final String RPC_ROUTING_KEY = "test.rpc.routing.key";

	public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {

		Connection connection = ConnectionUtils.getConnection();
		Channel channel = connection.createChannel();

		// 声明队列 交换机 并进行绑定
		channel.queueDeclare(RPC_QUEUQU, true, false, false, null);
		channel.exchangeDeclare(RPC_EXCHANGE, BuiltinExchangeType.DIRECT);
		channel.queueBind(RPC_QUEUQU, RPC_EXCHANGE, RPC_ROUTING_KEY);

		// 每次只能接收一条
		channel.basicQos(1);

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
				throws IOException {
				String message = new String(body, "UTF-8");
				log.info("服务端: 已经接收到消息 [{}]", message);

				// 服务端接收消息,并进行处理 ---
				String response = "{'code': 200, 'data': '" + message + "'}";

				String replyTo = properties.getReplyTo();
				String correlationId = properties.getCorrelationId();
				AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(correlationId).build();
				channel.basicPublish("", replyTo, props, response.getBytes());
				log.info("服务端：请求已处理完毕，响应结果" + response + "已发送到响应队列中");
				// 手动应答
				channel.basicAck(envelope.getDeliveryTag(), false);

			}
		};
		channel.basicConsume(RPC_QUEUQU, false, consumer);
		log.info("服务端：已订阅请求队列(rpc_queue), 开始等待接收请求消息...");

		TimeUnit.SECONDS.sleep(60);

		channel.close();
		connection.close();

	}

}


