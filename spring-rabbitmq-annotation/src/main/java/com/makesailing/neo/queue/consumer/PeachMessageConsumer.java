package com.makesailing.neo.queue.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

/**
 * #
 *
 * @author jamie
 * @date 2018/10/23 16:42
 */
@Slf4j
@Component
public class PeachMessageConsumer implements ChannelAwareMessageListener {

	@Override
	public void onMessage(Message message, Channel channel) throws Exception {
		log.info(" ------ 接收 桃子交换机 信息 ------");
		log.info("message properties [{}]", message);
		String info = new String(message.getBody(), "UTF-8");
		log.info("message info [{}]", info);
		channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
	}
}


