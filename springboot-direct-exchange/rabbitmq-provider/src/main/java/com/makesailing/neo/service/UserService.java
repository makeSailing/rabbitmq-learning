package com.makesailing.neo.service;

import com.makesailing.neo.domain.UserEntity;
import com.makesailing.neo.enums.ExchangeEnum;
import com.makesailing.neo.enums.QueueEnum;
import com.makesailing.neo.queue.service.QueueMessageService;
import com.makesailing.neo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 18:00
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

  @Autowired
  private UserRepository userRepository;

  /**
   * 消息队列业务逻辑实现
   */
  @Autowired
  private QueueMessageService queueMessageService;

  /**
   * 保存用户信息
   * @param userEntity
   * @return
   */
  public Long save(UserEntity userEntity) throws Exception {
    userRepository.save(userEntity);

    /**
     * 将消息写入消息队列
     */
    queueMessageService.send(userEntity.getId(), ExchangeEnum.USER_REGISTER, QueueEnum.USER_REGISTER);

    return userEntity.getId();
  }
}
