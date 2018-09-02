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

  private static final String QUEUE_NAME = "test_transaction_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1. 创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 2. 创建通道
    Channel channel = connection.createChannel();
    // 3. 申明一个一个队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    try {
      //开启事务
      channel.txSelect();
      // 发给info路由键消息
      String infoMessage = "Hello ,tx message";
      channel.basicPublish("", QUEUE_NAME, null, infoMessage.getBytes());
      System.out.println(" [ x ] Send  message '" + infoMessage + "'");

      //int i = 1 / 0;

      //提交事务
      channel.txCommit();
    } catch (Exception e){
      channel.txRollback(); // 回滚事务
      System.out.println("send message txRollback");
    }

    // 关闭连接与通道
    channel.close();
    connection.close();

  }
}
