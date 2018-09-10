package com.makesailing.neo.binud.service.impl;

import com.makesailing.neo.binud.service.UserService;
import com.makesailing.neo.enums.ExchangeEnum;
import com.makesailing.neo.enums.TopicEnum;
import com.makesailing.neo.queue.service.QueueMessageService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/10 23:27
 */
@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private QueueMessageService queueMessageService;

  @Override
  public String randomCreateUser() throws Exception {
    //  用户编号
    String userId = UUID.randomUUID().toString();
    // 发送消息到 rabbitmq 服务器
    queueMessageService.send(userId,ExchangeEnum.USER_REGISTER_TOPIC_EXCHANGE,TopicEnum.USER_REGISTER.getTopicRouteKey());
    return userId;
  }
}
