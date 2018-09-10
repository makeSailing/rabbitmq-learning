package com.makesailing.neo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 22:35
 */
@Slf4j
@SpringBootApplication
public class RabbitmqConsumerNode2Application {

  public static void main(String[] args) {
    SpringApplication.run(RabbitmqConsumerNode2Application.class, args);
    log.info("【【【【【消息队列-消息消费者 节点2 启动成功.】】】】");
  }
}
