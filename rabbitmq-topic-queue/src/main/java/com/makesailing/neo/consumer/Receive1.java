package com.makesailing.neo.consumer;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * # 消费者1
 *
 * @author jamie.li
 * @date 2018/9/18 14:51
 */
public class Receive1 {

	private static final String EXCHANGE_NAME = "test_exchange_topic";
	private static final String ROUTING_KEY = "*.orange.*";
	private static final String QUEUE_NAME = "test_queue_topic_1";

	public static void main(String[] args) throws IOException, TimeoutException {
		// 创建连接
		Connection connection = ConnectionUtils.getConnection();
		// 获取信道
		final Channel channel = connection.createChannel();

		//声明要消费的队列
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		// 队列绑定交换机
		channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY, null);

		// 设置每次消费1条消息,消费完后再取队列中获取消息,需要手动应答
		channel.basicQos(1);

		Consumer consumer = new DefaultConsumer(channel){
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
				throws IOException {
				super.handleDelivery(consumerTag, envelope, properties, body);
				String message = new String(body, "UTF-8");
				System.out.println(" Receive1 : ' " + message + " ', 处理业务中...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					System.out.println("Receive1 Done");
					channel.basicAck(envelope.getDeliveryTag(),false);
				}
			}
		};

		// 手动应答
		channel.basicConsume(QUEUE_NAME, false, consumer);
	}
}


