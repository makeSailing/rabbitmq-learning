package com.makesailing.neo.polling.distribution.provider;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/1 22:42
 */
public class Send {

  private static final String QUEUE_NAME = "test_work_poling_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1. 获取连接
    Connection connection = ConnectionUtils.getConnection();
    // 2. 创建通道
    Channel channel = connection.createChannel();
    // 3.申明这个通道的队列
    /**
     * queue: 队列名称
     * durable: 是否持久化，true表示RabbitMQ重启后，队列仍然存在
     * exclusive: true表示当前连接的专用队列，在连接断开后，会自动删除该队列
     * autoDelete: true 表示当没有任何消费者使用时，自动删除该队列
     * arguments: 该队列其他配置参数
     */
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    for (int i = 0; i < 20; i++) {
      String message = "Hello RabbitMQ " + i;
      /** 4. 发送消息
       * exchange: 指定交换机，这里为工作队列，不需要使用，默认""即可，不能为null
       * routingKey: 路由key，这里为队列名称
       * props: 其他消息属性，路由头信息等等
       * body: 消息byte内容
       */
      channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
      System.out.println(" [x] Send '" + message + "'");

    }
    // 5. 关闭通道和连接
    channel.close();
    connection.close();
  }
}
