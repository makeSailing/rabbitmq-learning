package com.makesailing.neo.business.service.impl;

import com.alibaba.fastjson.JSON;
import com.makesailing.neo.business.service.UserService;
import com.makesailing.neo.domain.User;
import com.makesailing.neo.mappers.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * #
 *
 * @author jamie.li
 * @date 2018/9/19 17:37
 */
@Service(UserService.SERVICE_ID)
public class UserServiceImpl implements UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private AmqpTemplate amqpTemplate;

	@Autowired
	private UserMapper userMapper;

	@Value("${user.register.exchange}")
	private String directExchange;

	@Value("${user.register.routingkey}")
	private String routingKey;

	@Override
	public Long saveUser(User user) {
		LOGGER.info("userRegister user parameter [{}]", user);
		userMapper.insertSelective(user);
		//发送mq
		amqpTemplate.convertAndSend(directExchange, routingKey, JSON.toJSONString(user));

		return user.getId();
	}
}


