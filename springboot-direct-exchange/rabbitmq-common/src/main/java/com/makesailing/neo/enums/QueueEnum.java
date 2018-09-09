package com.makesailing.neo.enums;

/**
 * # 队列配置枚举
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 18:36
 */
public enum QueueEnum {

  USER_REGISTER("user.register.queue", "user.register")
  ;
  private String name;

  private String routingKey;

  QueueEnum(String name, String routingKey) {
    this.name = name;
    this.routingKey = routingKey;
  }

  public String getName() {
    return name;
  }

  public String getRoutingKey() {
    return routingKey;
  }
}
