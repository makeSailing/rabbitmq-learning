package com.makesailing.neo.queue.service;

import com.makesailing.neo.enums.ExchangeEnum;

/**
 * # 消息队列业务
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/10 23:17
 */
public interface QueueMessageService {

  /**
   * 发送消息到rabbit消息队列
   * @param message
   * @param exchange
   * @param routingKey
   * @throws Exception
   */
  public void send(Object message, ExchangeEnum exchange, String routingKey) throws Exception;
}
