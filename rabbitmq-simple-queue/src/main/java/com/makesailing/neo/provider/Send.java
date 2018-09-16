package com.makesailing.neo.provider;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * # 生产者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 14:45
 */
public class Send {

  public static final String QUEUE_NAME = "test_simple_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1. 获取连接
    Connection connection = ConnectionUtils.getConnection();
    // 2.创建信道
    Channel channel = connection.createChannel();
    // 3.申明一个队列,没有就会创建
    /**
     * queue : 队列名称
     */
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // 4.发送消息
    String message = " Hello RabbitMQ";
    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

    System.out.println(" Send 发送的 ' " + message + " '");

    // 关闭通道和连接
    channel.close();
    connection.close();
  }
}
