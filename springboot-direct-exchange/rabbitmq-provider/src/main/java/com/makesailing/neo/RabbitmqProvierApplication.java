package com.makesailing.neo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 17:32
 */
@Slf4j
@SpringBootApplication
public class RabbitmqProvierApplication {

  public static void main(String[] args) {
    SpringApplication.run(RabbitmqProvierApplication.class, args);
    log.info("【【【【【消息队列-消息提供者启动成功.】】】】】");
  }
}
