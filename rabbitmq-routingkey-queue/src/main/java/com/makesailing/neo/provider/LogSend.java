package com.makesailing.neo.provider;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * # 日志消息 提供者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 22:41
 */
public class LogSend {

  private static final String EXCHANGE_NAME = "test_exchange_direct";

  private static final String INTO_ROUTING_NAME= "info";
  private static final String WARN_ROUTING_NAME= "warn";
  private static final String ERROR_ROUTING_NAME= "error";


  public static void main(String[] args) throws IOException, TimeoutException {
    // 创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 获取信道
    Channel channel = connection.createChannel();
    // 声明一个direct 路由交换机
    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

    // 发送info路由键消息
    String infoMessage = "Hello RabbitMQ Info Log";
    channel.basicPublish(EXCHANGE_NAME, INTO_ROUTING_NAME, null, infoMessage.getBytes());
    System.out.println("  LogSend routing info message : '" + infoMessage + "'");
    // 发送warn路由键消息
    String warnMessage = "Hello RabbitMQ Warn Log";
    channel.basicPublish(EXCHANGE_NAME, WARN_ROUTING_NAME, null, warnMessage.getBytes());
    System.out.println("  LogSend routing warn message : '" + warnMessage + "'");
    // 发送info路由键消息
    String errorMessage = "Hello RabbitMQ Error Log";
    channel.basicPublish(EXCHANGE_NAME, ERROR_ROUTING_NAME, null, errorMessage.getBytes());
    System.out.println("  LogSend routing error message : '" + errorMessage + "'");

    channel.close();
    connection.close();
  }
}
