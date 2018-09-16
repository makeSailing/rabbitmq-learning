package com.makesailing.neo.fair.provider;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 16:31
 */
public class Send {

  public static final String QUEUE_NAME = "test_work_fair_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1.创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 2.创建信道
    Channel channel = connection.createChannel();
    // 3.声明信道中的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);


    for (int i = 0; i < 20; i++) {
      String meassage = "Hello RabbitMQ " + i;
      //发送消息
      channel.basicPublish("", QUEUE_NAME, null, meassage.getBytes());
      System.out.println("Send 发送消息" + meassage);

    }

    // 关闭信道与连接
    channel.close();
    connection.close();
  }
}
