package com.makesailing.neo.queue.service.impl;

import com.makesailing.neo.enums.ExchangeEnum;
import com.makesailing.neo.queue.service.QueueMessageService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * # 消息队列业务逻辑实现
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/10 23:20
 */
@Service
public class QueueMessageServiceImpl implements QueueMessageService {

  /**
   * 消息队列模版
   */
  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Override
  public void send(Object message, ExchangeEnum exchange, String routingKey) throws Exception {
    // 发送消息到消息队列
    rabbitTemplate.convertAndSend(exchange.getName(), routingKey, message);
  }
}
