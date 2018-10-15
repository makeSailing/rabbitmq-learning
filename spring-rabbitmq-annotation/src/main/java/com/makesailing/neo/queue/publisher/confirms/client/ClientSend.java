package com.makesailing.neo.queue.publisher.confirms.client;


import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

/**
 * # RabbitMQ java Client实现发送确认 publisher confirms
 *
 * jamie.li
 * @date 2018/10/15 14:36
 */
@Slf4j
public class ClientSend {

	static AtomicLong id = new AtomicLong(0);

	static TreeSet<Long> tags = new TreeSet<>();

	public static Long send(Channel channel, byte[] bytes) throws IOException {
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().deliveryMode(2).
			contentEncoding("UTF-8").build();
		channel.basicPublish("test.order.direct.exchange", "test.order.routing.key", properties, bytes);
		return id.incrementAndGet();
	}

	public static void main(String[] args) throws Exception {

		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost("127.0.0.1");
		connectionFactory.setPort(5672);
		connectionFactory.setUsername("jamie");
		connectionFactory.setPassword("123456");
		connectionFactory.setVirtualHost("/simple");

		Connection connection = connectionFactory.newConnection();
		Channel channel = connection.createChannel();

		// 使当前的channel处于确认模式
		channel.confirmSelect();

		/**
		 * deliveryTag 消息id
		 * multiple 是否批量
		 * 		true : 意味着,小于等于deliveryTag 的消息都处理成功了
		 * 		false : 只是成功了deliveryTag这一条消息
		 *
		 */
		channel.addConfirmListener(new ConfirmListener() {

			// 处理拒绝消息
			@Override
			public void handleAck(long deliveryTag, boolean multiple) throws IOException {
				log.info("========== handleAck ==========");
				log.info("deliveryTag info [{}] ", deliveryTag);
				log.info("multiple info [{}] ", multiple);

				//处理成功发送的消息
				if (multiple) {
					//批量操作
					for (Long _id : new TreeSet<>(tags.headSet(deliveryTag + 1))) {
						tags.remove(_id);
					}
				} else {
					//单个确认
					tags.remove(deliveryTag);
				}

				log.info("未处理的消息 [{}]", tags);
			}

			// 消息发送失败或者落地失败
			@Override
			public void handleNack(long deliveryTag, boolean multiple) throws IOException {
				log.info("========== handleNack ==========");
				log.info("deliveryTag info [{}] ", deliveryTag);
				log.info("multiple info [{}] ", multiple);
			}

		});

		/**
		 * 当Channel设置成confirm模式时，发布的每一条消息都会获得一个唯一的deliveryTag
		 * deliveryTag在basicPublish执行的时候加1
		 */


		Long id = send(channel, "你的外卖已经送达".getBytes());

		tags.add(id);
		// 表示等待已经发送给broker的消息act或者nack之后才会继续执行。
		//channel.waitForConfirms();
		// 表示等待已经发送给broker的消息act或者nack之后才会继续执行，如果有任何一个消息触发了nack则抛出IOException
		//channel.waitForConfirmsOrDie();

		id = send(channel,"你的外卖已经送达".getBytes());
		tags.add(id);
		channel.waitForConfirms();

		id = send(channel,"呵呵，不接电话".getBytes());
		tags.add(id);
		channel.waitForConfirms();

		TimeUnit.SECONDS.sleep(10);

		/**
		 * 总结 : 如果同时发送三条消息,channel.waitForConfirms()或者channel.waitForConfirmsOrDie() 不开启,那么 multiple则会为true
		 * 批量处理,不需要等待发送者确认 . 如果开启,则单个处理,需要等待发送者d确认.
		 */

		channel.close();
		connection.close();
	}

}


