package com.makesailing.neo.controller;

import com.alibaba.fastjson.JSON;
import com.makesailing.neo.business.service.UserService;
import com.makesailing.neo.constant.Urls;
import com.makesailing.neo.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping(Urls.User.SAVE_USER)
	public String saveUser(@RequestBody User user) {
		Long userId = userService.saveUser(user);
		user.setId(userId);
		return JSON.toJSONString(user);
	}
	@RequestMapping(value = "/hello")
	public void hello() {
		System.out.println("hello");
	}
}


