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
 * # info 日志消费者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 23:02
 */
public class InfoReceive {

  private static final String EXCHANGE_NAME = "test_exchange_direct";
  // info日志队列
  private static final String QUEUE_NAME = "test_queue_routing_info";

  private static final String INTO_ROUTING_NAME= "info";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 获取连接
    Connection connection = ConnectionUtils.getConnection();

    // 打开通道
    Channel channel = connection.createChannel();

    // 申明要消费的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // 绑定队列到交换机
    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, INTO_ROUTING_NAME);

    // 这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它。
    channel.basicQos(1);

    // 创建一个回调的消费者处理类
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        // 接收到的消息
        String message = new String(body);
        System.out.println(" InfoReceive '" + message + "' , 任务处理中");

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          System.out.println(" InfoReceive done ");
          channel.basicAck(envelope.getDeliveryTag(), false);
        }
      }
    };

    // 消费消息
    channel.basicConsume(QUEUE_NAME, false, consumer);

  }
}