package com.makesailing.neo.provider;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * # 生产者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/1 17:41
 */
public class Send {

  private static final String QUEUE_NAME = "test_simple_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1.获取连接
    Connection connection = ConnectionUtils.getConnection();
    // 2.从连接开一个通道
    Channel channel = connection.createChannel();
    // 3.申明一个队列,没有就会创建
    /**
     * queue: 队列名称
     * durable: 是否持久化，true表示RabbitMQ重启后，队列仍然存在
     * exclusive: true表示当前连接的专用队列，在连接断开后，会自动删除该队列
     * autoDelete: true 表示当没有任何消费者使用时，自动删除该队列
     * arguments: 该队列其他配置参数
     */
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    // 4.消息内容
    String message = "Hello RabbitMQ !";
    // 5.发送消息
    /**
     * exchange: 指定交换机，这里为简单队列，不需要使用，默认""即可，不能为null
     * routingKey: 路由key，这里为队列名称
     * props: 其他消息属性，路由头信息等等
     * body: 消息byte内容
     */
    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

    System.out.println("[ x ] Sent '" + message + "'");

    // 6.关闭通道和连接
    channel.close();
    connection.close();

  }

}
