package com.makesailing.neo.constant;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/22 16:49
 */
public interface Urls {

  String ROOT = "/v1";

  public interface User {

    String USER = "/user";
    /**
     * 用户详情
     */
    String USER_DETAIL = ROOT + USER + "/{id}/detail";
    /**
     * 保存用户
     */
    String SAVE_USER = ROOT + "/save" + USER;
    /**
     * 修改用户
     */
    String UPDATE_USER = ROOT + "/update" + USER;
    /**
     * 删除用户
     */
    String DELETE_USER = ROOT + "/delete/{id}" + USER;
    /**
     * 用户列表
     */
    String USER_LIST = ROOT + USER + "/list";
  }
}
