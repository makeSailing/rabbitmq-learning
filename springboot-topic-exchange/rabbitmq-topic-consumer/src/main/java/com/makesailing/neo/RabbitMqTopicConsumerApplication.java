package com.makesailing.neo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/10 23:05
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.makesailing.neo")
public class RabbitMqTopicConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(RabbitMqTopicConsumerApplication.class, args);
    log.info("【【【【【Topic队列消息Consumer启动成功】】】】】");
  }
}
