package com.makesailing.neo.provider;

import com.makesailing.neo.utils.ConnectionUtils;
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

  private static final String QUEUE_NAME = "test_queue_confirm1";

  public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
    // 1. 创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 2. 创建通道
    Channel channel = connection.createChannel();
    // 3. 申明一个一个队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // 开启confirm模式
    channel.confirmSelect();
    // 发给info路由键消息
    String message = "Hello ,confirm message";
    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
    if (channel.waitForConfirms()) {
      System.out.println(" [ x ] Send  message '" + message + "' ok");
    } else {
      System.out.println(" [ x ] Send  message '" + message + "' fail");
    }

    // 关闭连接与通道
    channel.close();
    connection.close();

  }
}
