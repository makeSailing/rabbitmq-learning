package com.makesailing.neo.utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/1 22:40
 */
public class ConnectionUtils {

  private static final String host = "127.0.0.1";
  private static final int port = 5672;

  /**
   * 获取 RabbitMQ Connection 连接
   * @return
   * @throws IOException
   * @throws TimeoutException
   */
  public static Connection getConnection() throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    factory.setPort(port);

    factory.setUsername("guest");
    factory.setPassword("guest");
    return factory.newConnection();
  }
}
