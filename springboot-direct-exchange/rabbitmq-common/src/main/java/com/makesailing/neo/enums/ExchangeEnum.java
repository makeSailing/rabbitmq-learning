package com.makesailing.neo.enums;


/**
 * # rabbit 交换配置枚举
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 18:33
 */

public enum ExchangeEnum {

  USER_REGISTER("user.register.topic.exchange");

  private String value;

  ExchangeEnum(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
