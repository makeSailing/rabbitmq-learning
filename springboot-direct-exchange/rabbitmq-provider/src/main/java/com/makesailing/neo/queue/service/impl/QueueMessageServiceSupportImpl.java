package com.makesailing.neo.queue.service.impl;

import com.makesailing.neo.enums.ExchangeEnum;
import com.makesailing.neo.enums.QueueEnum;
import com.makesailing.neo.queue.service.QueueMessageService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Slf4j
@Component
public class QueueMessageServiceSupportImpl implements QueueMessageService {

	/**
	 * 消息队列模板
	 */
	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public void send(Object message, ExchangeEnum exchangeEnum, QueueEnum queueEnum) throws Exception {
		//设置回调为当前类对象
		rabbitTemplate.setConfirmCallback(this);
		//构建回调id为uuid
		CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
		//发送消息到消息队列
		rabbitTemplate.convertAndSend(exchangeEnum.getValue(), queueEnum.getRoutingKey(), message, correlationId);
	}

    /**
     * 消息回调确认方法
     * @param correlationData 请求数据对象
     * @param ack 是否发送成功
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        System.out.println(" 回调id:" + correlationData.getId());
        if (ack) {
            System.out.println("消息发送成功");
        } else {
            System.out.println("消息发送失败:" + cause);
        }
    }
}

