package com.makesailing.neo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 18:57
 */
@Slf4j
@SpringBootApplication
public class RabbitmqConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(RabbitmqConsumerApplication.class, args);
    log.info("【【【【【消息队列-消息消费者启动成功.】】】】");
  }

}
