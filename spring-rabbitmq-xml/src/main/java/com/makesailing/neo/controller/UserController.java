package com.makesailing.neo.controller;

import com.alibaba.fastjson.JSON;
import com.makesailing.neo.business.service.UserService;
import com.makesailing.neo.domain.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * #
 *
 * @author jamie.li
 * @date 2018/9/19 16:40
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/save")
	public String userRegister(@RequestBody UserEntity userEntity) {
		Long userId = userService.userRegister(userEntity);
		userEntity.setId(userId);
		return JSON.toJSONString(userEntity);
	}

	@GetMapping("/hello")
	public void hello() {
		System.out.println("hello");
	}
}


