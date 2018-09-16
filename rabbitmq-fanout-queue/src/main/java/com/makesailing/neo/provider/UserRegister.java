package com.makesailing.neo.provider;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * # 消息 生产者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 20:53
 */
public class UserRegister {

  public static final String EXCHANGE_NAME = "test_exchange_fanout";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1. 获取连接
    Connection connection = ConnectionUtils.getConnection();
    // 2.获取一个信道
    Channel channel = connection.createChannel();
    // 3.声明一个fanout分发交换机
    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

    String message = "user register success";

    //发送消息, 订阅模式不需要 routingKey,可以写成 "",或者随意写个名字,但是不能为null
    channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());

    System.out.println(" Send " + message);

    // 关闭资源
    channel.close();
    connection.close();
  }

}
