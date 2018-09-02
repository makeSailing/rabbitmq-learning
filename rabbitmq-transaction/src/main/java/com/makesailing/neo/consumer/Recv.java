package com.makesailing.neo.consumer;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * # info消费者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/2 10:58
 */
public class Recv {

  // 短信队列
  private static final String QUEUE_NAME = "test_transaction_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1. 获取连接
    Connection connection = ConnectionUtils.getConnection();
    // 2. 创建通道
    Channel channel = connection.createChannel();
    // 3. 申明要消费的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);


    // 6.创建一个回调的消费者处理类
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        // 接收到的消息
        String message = new String(body);
        System.out.println(" [1] Received '" + message + "'");

      }
    };

    // 消费消息
    channel.basicConsume(QUEUE_NAME, false, consumer);
  }

}
