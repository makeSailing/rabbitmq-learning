package com.makesailing.neo.config;

import com.makesailing.neo.enums.ExchangeEnum;
import com.makesailing.neo.enums.QueueEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * # 用户注册消息队列配置
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/10 22:38
 */
@Slf4j
@Configuration
public class UserRegisterQueueConfiguration {

  /**
   * 配置用户注册主题交换
   */
  @Bean
  public TopicExchange userTopicExchange() {
    TopicExchange topicExchange = new TopicExchange(ExchangeEnum.USER_REGISTER_TOPIC_EXCHANGE.getName());
    log.info("用户注册交换实例化成功");
    return topicExchange;
  }

  /**
   * 用户注册成功,发送邮件消息队列,并持久化
   */
  @Bean
  public Queue sendRegisterMailQueue() {
    Queue queue = new Queue(QueueEnum.USER_REGISTER_SEND_MAIL.getName(), true);
    log.info("用户注册成功,发送邮件消息队列成功");
    return queue;
  }

  /**
   * 创建用户注册账号队列并持久化
   */
  @Bean
  public Queue createAccountQueue() {
    Queue queue = new Queue(QueueEnum.USER_REGISTER_CREATE_ACCOUNT.getName(), true);
    log.info("创建用户注册帐号队列成功");
    return queue;
  }

  /**
   * 绑定用户发送注册激活邮件到用户注册主题配置
   */
  @Bean
  public Binding sendMailBinding(TopicExchange userTopicExchange, Queue sendRegisterMailQueue) {
    //
    Binding binding = BindingBuilder.bind(sendRegisterMailQueue).to(userTopicExchange)
        .with(QueueEnum.USER_REGISTER_SEND_MAIL.getRoutingKey());
    log.info("绑定发送邮件到注册成功");
    return binding;
  }

  /**
   * 绑定用户创建账户到用户注册主题交换配置
   */
  @Bean
  public Binding createAccountBinding(TopicExchange userTopicExchange, Queue createAccountQueue) {
    Binding binding = BindingBuilder.bind(createAccountQueue).to(userTopicExchange)
        .with(QueueEnum.USER_REGISTER_CREATE_ACCOUNT.getRoutingKey());
    log.info("绑定创建账号到注册交换成功");
    return binding;
  }


}
