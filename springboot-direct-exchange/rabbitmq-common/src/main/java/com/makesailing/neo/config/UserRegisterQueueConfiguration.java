package com.makesailing.neo.config;

import com.makesailing.neo.enums.ExchangeEnum;
import com.makesailing.neo.enums.QueueEnum;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * # 用户注册消息队列配置
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 18:40
 */
@Configuration
public class UserRegisterQueueConfiguration {

  /**
   * 配置路由交换对象实例
   * @return
   */
  @Bean
  public DirectExchange userRegisterDirectExchange() {
    return new DirectExchange(ExchangeEnum.USER_REGISTER.getValue());
  }

  /**
   * 配置用户注册队列实例并设置持久化队列
   * @return
   */
  @Bean
  public Queue userRegisterQueue() {
    return new Queue(QueueEnum.USER_REGISTER.getName(), true);
  }

  /**
   *
   * @return
   */
  @Bean
  public Binding userRegisterBinging() {
    return BindingBuilder.bind(userRegisterQueue()).to(userRegisterDirectExchange())
        .with(QueueEnum.USER_REGISTER.getRoutingKey());
  }
}
