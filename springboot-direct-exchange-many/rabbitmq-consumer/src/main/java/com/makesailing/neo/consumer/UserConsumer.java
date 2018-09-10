package com.makesailing.neo.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 19:02
 */
@Slf4j
@Component
@RabbitListener(queues ="user.register.queue")
public class UserConsumer {

  @RabbitHandler
  public void execute(Long userId) {
    // 省略业务逻辑处理

    log.info("用户注册消费者【节点2】获取消息，用户编号：{}",userId);

  }
}
