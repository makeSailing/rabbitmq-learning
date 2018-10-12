package com.makesailing.neo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;


/**
 * #
 *
 * jamie.li
 * @date 2018/10/12 13:34
 */
@Component
@Slf4j
public class TestMessageConverter implements MessageConverter {


	@Override
	public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
		log.info("----- toMessage info ----- >>> object [{}} , messageProperties [{}]", object, messageProperties);
		return new Message(object.toString().getBytes(), messageProperties);
	}

	/**
	 * 将 message 对象转换成 java对象, 返回的数据类型就是消息端处理器接收的类型,如果消费者没有此类型接收会抛出异常
	 * @param message
	 * @return
	 * @throws MessageConversionException
	 */
	@Override
	public Object fromMessage(Message message) throws MessageConversionException {
		log.info("----- fromMessage info ----- >>> message [{}} ", message);
		return new String(message.getBody());
		// 使用自定义MyBody,那么消费端也需要使用自定义MyBody,否则会抛出异常 NoSuchMethodException
		//return new MyBody(message.getBody());
	}

}

