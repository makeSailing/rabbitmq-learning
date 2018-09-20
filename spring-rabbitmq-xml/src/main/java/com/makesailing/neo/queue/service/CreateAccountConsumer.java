package com.makesailing.neo.queue.service;

import java.io.UnsupportedEncodingException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;

/**
 * #
 *
 * @author jamie.li
 * @date 2018/9/19 18:19
 */
@Component
public class CreateAccountConsumer implements MessageListener {

	@Override
	public void onMessage(Message message) {
		try {

			MessageProperties messageProperties = message.getMessageProperties();
			System.out.println("消费属性 : " + messageProperties);

			String msg = new String(message.getBody(), "UTF-8");

			System.out.println("CreateAccountConsumer 所接收到的消息:" + msg);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}


