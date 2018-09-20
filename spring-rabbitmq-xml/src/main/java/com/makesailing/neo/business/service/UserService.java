package com.makesailing.neo.business.service;

import com.makesailing.neo.domain.UserEntity;

/**
 * # 用户服务
 *
 * @author jamie.li
 * @date 2018/9/19 17:36
 */
public interface UserService {

	String SERVICE_ID = "userService";

	/**
	 * 用户注册
	 * @param userEntity
	 * @return
	 */
	Long userRegister(UserEntity userEntity);
}
