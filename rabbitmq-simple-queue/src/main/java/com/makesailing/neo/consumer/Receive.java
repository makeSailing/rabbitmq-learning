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
 * # 消息接收者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 14:55
 */
public class Receive {

  public static final String QUEUE_NAME = "test_simple_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1.获取连接
    Connection connection = ConnectionUtils.getConnection();
    // 2.创建信道
    Channel channel = connection.createChannel();
    // 3.申明要消息的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    // 4.创建一个回调的消费者处理类
    Consumer consumer = new DefaultConsumer(channel){
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
          throws IOException {
        super.handleDelivery(consumerTag, envelope, properties, body);
        // 接收到的消息
        String message = new String(body);
        System.out.println("Receive 接收到的消息 " + message);
      }
    };

    channel.basicConsume(QUEUE_NAME, true, consumer);

  }
}
