package com.makesailing.neo.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * # 用户注册
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/10 23:11
 */
@Slf4j
@Component
@RabbitListener(queues = "register.account")
public class CreateAccountConsumer {

  /**
   * 处理消息 创建用户注册
   */
  @RabbitHandler
  public void handler(String userId) {
    log.info("用户: [{}] ,注册成功,自动创建账户信息" ,userId);
    // .创建用户逻辑
  }
}
