package com.makesailing.neo.queue.service;

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

/**
 * #
 *
 * @author jamie
 * @date 2018/9/25 17:25
 */
public class TopicConsumer2 implements MessageListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(TopicConsumer2.class);

	@Override
	public void onMessage(Message message) {
		LOGGER.info("-----接收 topic exchange 事件-----");
		if (Objects.isNull(message) || ArrayUtils.isEmpty(message.getBody())) {
			LOGGER.warn("topic exchange is message is empty");
			return;
		}

		try {
			String msg = new String(message.getBody(), "UTF-8");
			LOGGER.info("receive topic exchange is message is messageBody  [{}]" , msg);

			// TODO 省略其业务逻辑
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}


