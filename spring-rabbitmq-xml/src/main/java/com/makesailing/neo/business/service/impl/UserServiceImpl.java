package com.makesailing.neo.business.service.impl;

import com.alibaba.fastjson.JSON;
import com.makesailing.neo.business.service.UserService;
import com.makesailing.neo.domain.UserEntity;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * #
 *
 * @author jamie.li
 * @date 2018/9/19 17:37
 */
@Service(UserService.SERVICE_ID)
public class UserServiceImpl implements UserService {

	@Autowired
	private AmqpTemplate amqpTemplate;

	@Override
	public Long userRegister(UserEntity userEntity) {
		// todo 插入数据库待定

		//发送mq
		amqpTemplate.convertAndSend("register.direct.exchange","register.account", JSON.toJSONString(userEntity));

		return System.currentTimeMillis();
	}
}


