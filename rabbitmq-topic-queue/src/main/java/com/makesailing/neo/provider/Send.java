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

  private static final String EXCHANGE_NAME = "test_exchange_topic";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1. 创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 2. 创建通道
    Channel channel = connection.createChannel();
    // 3. 申明一个topic分发交换机
    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

    // 发给info路由键消息
    String infoMessage = "Hello ,quick.orange.rabbit";
    channel.basicPublish(EXCHANGE_NAME, "quick.orange.rabbit", null, infoMessage.getBytes());
    System.out.println(" [ x ] Send topic message '" + infoMessage + "'");

    // 关闭连接与通道
    channel.close();
    connection.close();

  }
}
