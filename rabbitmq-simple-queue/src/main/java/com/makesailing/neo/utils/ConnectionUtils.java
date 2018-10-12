package com.makesailing.neo.utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * # RabbitMQ连接工具类
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 14:38
 */
public class ConnectionUtils {

  public static final String host = "127.0.0.1";

  public static final Integer port = 5672;

  public static Connection getConnection() throws IOException, TimeoutException {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(host);
    connectionFactory.setPort(port);
    // 如果有 用户名 密码 vhost 配置即可
    connectionFactory.setUsername("jamie");
    connectionFactory.setPassword("123456");
    connectionFactory.setVirtualHost("/simple");

    //客户端的消费消息
    Map<String,Object> clientProperties = new HashMap<>(16);
    clientProperties.put("desc","支付系统2.0");
    clientProperties.put("author","zhihao.miao");
    clientProperties.put("user","zhihao.miao@xxx.com");

    connectionFactory.setClientProperties(clientProperties);
    return connectionFactory.newConnection("测试");
  }
}
