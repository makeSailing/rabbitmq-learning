package com.makesailing.neo.fair.consumer;

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
 * # 轮询分发 消费者2
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 16:54
 */
public class Recvice2 {
  public static final String QUEUE_NAME = "test_work_fair_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1.创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 2.创建信道
    Channel channel = connection.createChannel();
    // 3.声明信道中的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // 设置每次从队列获取消息的数量
    // 换句话讲 在接收该 Consumer的 ack之前,他不会将新的message发给它
    int prefetchCount = 1 ;
    channel.basicQos(prefetchCount);
    // 4. 创建一个回调类
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
          throws IOException {
        super.handleDelivery(consumerTag, envelope, properties, body);
        String message = new String(body, "UTF-8");
        // 工作
        doWork(message);

        channel.basicAck(envelope.getDeliveryTag(), false);
      }
    };

    // 消费消息
    channel.basicConsume(QUEUE_NAME, false, consumer);

  }

  private static void doWork(String message) {
    System.out.println(" [2] Received '" + message + "', 处理业务中...");
    // 模仿消费者处理业务的时间，也让其他消费者有机会获取到消息，实际开发中不需要，这里只是模拟
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      System.out.println("[2] done");
    }
  }
}
