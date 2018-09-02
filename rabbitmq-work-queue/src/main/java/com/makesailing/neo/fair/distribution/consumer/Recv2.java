package com.makesailing.neo.fair.distribution.consumer;

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
 * # 消费者2
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/1 23:02
 */
public class Recv2 {

  private static final String QUEUE_NAME = "test_work_fair_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1.创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 2.创建通道
    Channel channel = connection.createChannel();
    // 3.申明要消息的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // 公平分发确定发给消费者只有1 个
    // 这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。
    // 换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它。
    int prefetchCount = 1;
    channel.basicQos(prefetchCount);
    // 4.创建一个回调消息的处理类
    Consumer consumer = new DefaultConsumer(channel){
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
          throws IOException {
        String message = new String(body);
        System.out.println(" [2] Recevied '" + message + "'");
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          System.out.println(" [2] done ");
          channel.basicAck(envelope.getDeliveryTag(), false);
        }

      }
    };

    // 消费消息
    channel.basicConsume(QUEUE_NAME, false, consumer);
  }
}
