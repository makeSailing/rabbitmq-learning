package com.makesailing.neo.queue.service;

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;

/**
 * #
 *
 * @author jamie
 * @date 2018/9/26 10:07
 */
@Component
public class FanoutConsumerSms implements MessageListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(FanoutConsumerSms.class);

	@Override
	public void onMessage(Message message) {
		LOGGER.info("-----接收 fannout exchange 事件-----");
		if (Objects.isNull(message) || ArrayUtils.isEmpty(message.getBody())) {
			LOGGER.warn("fannout exchange is message is empty");
			return;
		}

		try {
			String msg = new String(message.getBody(), "UTF-8");
			LOGGER.info("receive fannout exchange is message is messageBody  [{}]" , msg);

			// TODO 省略其业务逻辑
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}


