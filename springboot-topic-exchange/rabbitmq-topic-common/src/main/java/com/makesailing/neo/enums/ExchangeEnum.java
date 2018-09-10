package com.makesailing.neo.enums;

/**
 * # rabbit 交换配置枚举
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/10 22:22
 */
public enum ExchangeEnum {

  /**
   * 用户注册交换配置枚举
   */
  USER_REGISTER_TOPIC_EXCHANGE("register.topic.exchange");

  private String name;

  ExchangeEnum(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
