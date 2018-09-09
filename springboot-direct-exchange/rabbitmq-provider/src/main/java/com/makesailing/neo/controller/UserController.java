package com.makesailing.neo.controller;

import com.makesailing.neo.domain.UserEntity;
import com.makesailing.neo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 18:06
 */
@RestController
@RequestMapping("/user")
public class UserController {

  @Autowired
  private UserService userService;

  /**
   * 保存用户
   */
  @PostMapping("/save")
  public UserEntity save(@RequestBody UserEntity userEntity) throws Exception {
    userService.save(userEntity);
    return userEntity;
  }

}
