package com.makesailing.neo.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/10 23:07
 */
@Slf4j
@Component
@RabbitListener(queues = "register.mail")
public class SendMailConsumer {

  /**
   * 处理消息 发送用户注册成功邮件
   * @param userId
   */
  @RabbitHandler
  public void handler(String userId) {
    log.info("用户: [{}] ,注册成功,自动发送邮件", userId);
    // .. 发送注册成功邮件逻辑
  }
}
