package com.makesailing.neo.enums;

/**
 * # rabbit 队列配置枚举
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/10 22:25
 */
public enum QueueEnum {

  /**
   * 用户注册 创建帐户消息队列
   */
  USER_REGISTER_CREATE_ACCOUNT("register.account", "register.#"),

  /**
   * e用户注册 发送邮件注册成功消息队列
   */
  USER_REGISTER_SEND_MAIL("register.mail","register.#")
  ;

  private String name;

  private String routingKey;

  QueueEnum(String name, String rountingKey) {
    this.name = name;
    this.routingKey = rountingKey;
  }

  public String getName() {
    return name;
  }

  public String getRoutingKey() {
    return routingKey;
  }
}
