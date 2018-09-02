package com.makesailing.neo.polling.distribution.consumer;

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
 * # 消费者1
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/1 22:51
 */
public class Recv {

  private static final String QUEUE_NAME = "test_work_poling_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1.获取连接
    Connection connection = ConnectionUtils.getConnection();
    // 2.创建通道
    Channel channel = connection.createChannel();

    // 3.申明要消息的队列
    channel.queueDeclare( QUEUE_NAME, false, false, false, null);
    // 4.创建一个回调的消费者处理类
    Consumer consumer = new DefaultConsumer(channel){
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
          throws IOException {
        // 接收到的消息
        String message = new String(body);
        System.out.println(" [1] Received '" + message + "'");

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          System.out.println(" [1] done ");
        }

      }
    };

    // 消费消息
    /**
     * queue 队列名称
     * autoAck : 是否自动应答,即生产者发送消息即认为该消息被消费
     * callback: 回调处理类,即消息被消费时被回调处理
     */
    channel.basicConsume(QUEUE_NAME, true, consumer);
  }
}
