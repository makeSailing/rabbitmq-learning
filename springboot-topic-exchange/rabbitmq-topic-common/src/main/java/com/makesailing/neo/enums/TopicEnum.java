package com.makesailing.neo.enums;

/**
 * # rabbit 消息队列 topic 交换路由key配置枚举
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/10 22:32
 */
public enum TopicEnum {
  /**
   * 用户注册 topic路由 key配置
   */
  USER_REGISTER("register.user"),;

  private String topicRouteKey;

  TopicEnum(String topicRouteKey) {
    this.topicRouteKey = topicRouteKey;
  }

  public String getTopicRouteKey() {
    return topicRouteKey;
  }
}
