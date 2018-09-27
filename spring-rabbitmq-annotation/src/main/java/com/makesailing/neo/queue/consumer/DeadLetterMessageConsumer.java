package com.makesailing.neo.queue.consumer;

import com.alibaba.fastjson.JSONObject;
import com.makesailing.neo.constant.DLXMessage;
import com.makesailing.neo.constant.RoutingKeyConstant;
import com.makesailing.neo.queue.service.MessageQueueService;
import com.rabbitmq.client.Channel;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * # 死信队列消费者
 *
 * @author <a href="mailto:jamie.li@wolaidai.com">jamie.li</a>
 * @date 2018/9/27 17:44
 */
@Component
public class DeadLetterMessageConsumer implements ChannelAwareMessageListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeadLetterMessageConsumer.class);

	@Autowired
	private MessageQueueService messageQueueService;

	@Override
	public void onMessage(Message message, Channel channel) throws Exception {
		LOGGER.info("-----接收 直连交换机 死信队列 信息-----");
		if (Objects.isNull(message) || ArrayUtils.isEmpty(message.getBody())) {
			LOGGER.warn("receive userInfo from the register, the message is empty");
			return;
		}

		try {
			String msg = new String(message.getBody(), "UTF-8");
			LOGGER.info("receive DLXMessage from the register, the messageBody  [{}]" , msg);
			DLXMessage dlxMessage = JSONObject.parseObject(msg, DLXMessage.class);

			messageQueueService.send(dlxMessage.getExchange(), RoutingKeyConstant.DIRECT_REPEAT_TRADE_ROUTING_KEy,
				dlxMessage.getContent());
			// TODO 省略其业务逻辑
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}


