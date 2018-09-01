package com.makesailing.neo.consumer;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/1 17:57
 */
public class Recv {

  private static final String QUEUE_NAME = "test_simple_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1.获取连接
    Connection connection = ConnectionUtils.getConnection();
    // 2.创建通道
    Channel channel = connection.createChannel();
    // 3.申明要消费的队列
    /**
     * queue: 队列名称
     * autoAck: 是否自动应答，即生产者发送消息即认为该消息被消费
     * callback: 回调处理类，即消息被消费时进行回调处理
     */
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    // 4.创建一个回调消费者的处理类
    Consumer consumer = new DefaultConsumer(channel){
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
          throws IOException {
        // 接收到的消息
        String message = new String(body);
        System.out.println(" [x] Received '" + message + "'");
      }
    };
    // 5.消费者消费消息
    channel.basicConsume(QUEUE_NAME, true, consumer);

  }
}
