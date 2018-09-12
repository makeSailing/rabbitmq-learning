package com.makesailing.neo.provider;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * #
 *
 * @author jamie
 * @date 2018/9/12 15:01
 */
@Slf4j
@Component
public class MessageProvider {

	@Autowired
	private AmqpTemplate rabbitMqTemplate;

	/**
	 * 发送延迟消息
	 * @param messageContent 消息内容
	 * @param exchange 交换机
	 * @param routingKey 路由键
	 * @param dalayTimes 过期时间
	 */
	public void sendMessage(Object messageContent,String exchange,String routingKey,final Long dalayTimes) {
		if (StringUtils.isEmpty(exchange)) {
			log.error("未找到消息队列 : [{}] ,所属的交换机", exchange);
		}
		log.info("延迟: [{}] 毫秒写入消息队列 : [{}] ,消息内容 : [{}]", dalayTimes, routingKey, JSON.toJSONString(messageContent));
		// 执行发送消息到指定队列
		rabbitMqTemplate.convertAndSend(exchange, routingKey, messageContent, message -> {
			// 设置延迟毫秒值
			message.getMessageProperties().setExpiration(String.valueOf(dalayTimes));
			return message;
		});

	}
}


