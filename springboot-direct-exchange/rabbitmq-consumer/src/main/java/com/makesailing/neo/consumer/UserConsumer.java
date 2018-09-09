package com.makesailing.neo.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 19:02
 */
@Component
@RabbitListener(queues ="user.register.queue")
public class UserConsumer {

  @RabbitHandler
  public void execute(Long userId) {
    // 省略业务逻辑处理

    System.out.println("用户: " + userId + "完成了注册");

  }
}
