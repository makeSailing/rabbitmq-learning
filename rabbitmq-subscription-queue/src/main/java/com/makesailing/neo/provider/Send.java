package com.makesailing.neo.provider;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * # 发送者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/2 10:50
 */
public class Send {

  private static final String EXCHANGE_NAME = "test_exchange_fanout";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1. 创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 2. 创建通道
    Channel channel = connection.createChannel();
    // 3. 申明一个fanout 分发交换机
    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

    String message = "Hello RabbitMQ";

    // 发送消息
    channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());

    System.out.println("[ x ] Send '" + message + "'");

    // 关闭连接与通道
    channel.close();
    connection.close();

  }
}
