package com.makesailing.neo.queue.comsumer.acknowledgements.client;

import com.makesailing.neo.common.ConnectionUtils;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * #
 *
 *jamie.li
 * @date 2018/10/15 17:32
 */
@Slf4j
public class ClientConsumer {

	public static void main(String[] args) throws Exception {
		Connection connection = ConnectionUtils.getConnection();
		Channel channel = connection.createChannel();

		Consumer consumer = new DefaultConsumer(channel){
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
				throws IOException {
				log.info("======= handleDelivery =======");
				super.handleDelivery(consumerTag, envelope, properties, body);

				// 接收到的消息
				String message = new String(body,"utf-8");
				log.info("接收到的消息 [{}]", message);
				log.info("消息属性 : [{}]", properties);

				// 自动确认模式,抛出异常也会消费消息,这也就造成实际意义的消息丢失
				int a = 1 / 0;
			}
		};

		channel.basicConsume("order", true, consumer);

		TimeUnit.SECONDS.sleep(30);

		channel.close();
		connection.close();

	}
}


