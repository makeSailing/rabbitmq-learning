package com.makesailing.neo.queue.service;

import com.makesailing.neo.enums.ExchangeEnum;
import com.makesailing.neo.enums.QueueEnum;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 18:29
 */
public interface QueueMessageService extends RabbitTemplate.ConfirmCallback {

  /**
      * 发送消息到rabbitmq消息队列
     * @param message 消息内容
     * @param exchangeEnum 交换配置枚举
     * @param queueEnum 队列配置枚举
     * @throws Exception
     */
  public void send(Object message, ExchangeEnum exchangeEnum, QueueEnum queueEnum) throws Exception;



}
