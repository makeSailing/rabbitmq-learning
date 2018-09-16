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
 * # 短信消费者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 21:02
 */
public class SMSReceive {

  // 短信队列
  private static final String QUEUE_NAME = "test_fanout_queue_sms";
  private static final String EXCHANGE_NAME = "test_exchange_fanout";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 获取信道
    Channel channel = connection.createChannel();
    // 申明要消费的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    // 绑定队列到交换机
    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

    // 限制消费的队列数 ,并设置自动应答为 false
    channel.basicQos(1);

    //创建一个回调的消费者处理类
    Consumer consumer = new DefaultConsumer(channel){
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
          throws IOException {
        super.handleDelivery(consumerTag, envelope, properties, body);
        // 接收到的消息
        String message = new String(body,"utf-8");
        System.out.println("SMSReceive " + message);

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }finally {
          System.out.println("SMSReceive done"  );
          // 关闭自动应答后, 在消费者处理消费数据之后一定要对消息进行手动反馈处理
          //可以是以下方式的一种
          channel.basicAck(envelope.getDeliveryTag(), false);
          // 拒绝当前消息,并使这条消息重新返回队列中
          //channel.basicNack(envelope.getDeliveryTag(), false, true);
          // 相当于
          //channel.basicReject(envelope.getDeliveryTag(), true);
        }
      }
    };
    channel.basicConsume(QUEUE_NAME, false, consumer);
  }
}
