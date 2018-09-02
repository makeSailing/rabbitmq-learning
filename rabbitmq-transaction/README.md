

####  一 . RabbitMQ消息确认机制之事务机制

1. 服务器异常数据丢失问题

解决方案 : 采用持久化数据,即声明队列时设置

> ```java
> queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
>
> durable = true
> ```

1.生产者发送消息之后,不知道到底有没有发送到RabbitMQ服务器,默认是不知道的 . 而且有时时候我们在发送消息之后,后面的逻辑出问题了,我们不想要发送之前的消息了,需要撤回该怎么做 ? 

解决方案 : 

 	1. AMQP 事务机制
 	2. Confirm模式

#### 二 . 事务机制

AMQP提供了以下几个方法

> ```java
> txSelect	将当前channel设置为transaction模式
> txCommit	提交当前事务
> txRollback	事务回滚
>
> ```

#### 三 . 代码演示

> 只是路由键可以模糊匹配了,先设定为生产者发送quick.orange.rabbit ,消费者1绑定到 .orange. , 消费者2绑定到 ..rabbit以及lazy.#

- 连接RabbitMQ工具类

  ```java
  package com.makesailing.neo.utils;

  import com.rabbitmq.client.Connection;
  import com.rabbitmq.client.ConnectionFactory;
  import java.io.IOException;
  import java.util.concurrent.TimeoutException;

  /**
   * #
   *
   * @Author: jamie.li
   * @Date: Created in  2018/9/1 22:40
   */
  public class ConnectionUtils {

    private static final String host = "127.0.0.1";
    private static final int port = 5672;

    /**
     * 获取 RabbitMQ Connection 连接
     * @return
     * @throws IOException
     * @throws TimeoutException
     */
    public static Connection getConnection() throws IOException, TimeoutException {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(host);
      factory.setPort(port);

      factory.setUsername("guest");
      factory.setPassword("guest");
      return factory.newConnection();
    }
  }

  ```

- 生产者

  ```java
  package com.makesailing.neo.provider;

  import com.makesailing.neo.utils.ConnectionUtils;
  import com.rabbitmq.client.Channel;
  import com.rabbitmq.client.Connection;
  import java.io.IOException;
  import java.util.concurrent.TimeoutException;

  /**
   * # 发送者
   *
   * @Author: jamie.li
   * @Date: Created in  2018/9/2 10:50
   */
  public class Send {

    private static final String QUEUE_NAME = "test_transaction_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
      // 1. 创建连接
      Connection connection = ConnectionUtils.getConnection();
      // 2. 创建通道
      Channel channel = connection.createChannel();
      // 3. 申明一个一个队列
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);

      try {
        //开启事务
        channel.txSelect();
        // 发给info路由键消息
        String infoMessage = "Hello ,tx message";
        channel.basicPublish("", QUEUE_NAME, null, infoMessage.getBytes());
        System.out.println(" [ x ] Send  message '" + infoMessage + "'");

        //int i = 1 / 0;

        //提交事务
        channel.txCommit();
      } catch (Exception e){
        channel.txRollback(); // 回滚事务
        System.out.println("send message txRollback");
      }

      // 关闭连接与通道
      channel.close();
      connection.close();

    }
  }

  ```

- 消费者1

  ```java
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
   * # info消费者
   *
   * @Author: jamie.li
   * @Date: Created in  2018/9/2 10:58
   */
  public class Recv {

    // 短信队列
    private static final String QUEUE_NAME = "test_transaction_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
      // 1. 获取连接
      Connection connection = ConnectionUtils.getConnection();
      // 2. 创建通道
      Channel channel = connection.createChannel();
      // 3. 申明要消费的队列
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);


      // 6.创建一个回调的消费者处理类
      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
          // 接收到的消息
          String message = new String(body);
          System.out.println(" [1] Received '" + message + "'");

        }
      };

      // 消费消息
      channel.basicConsume(QUEUE_NAME, false, consumer);
    }

  }

  ```

  ​


#### 四 . 测试结果

能够正常收发 . 

这时候生产者加上异常

> int i = 1/0 ;

Send : 

>  send message txRollback

Recv : 

>   

消费者并没有接收到消息。说明生产者的消息回滚了，事务生效。

#### 五 . 缺点

事务确实能够解决provider与consumer之前的消息确认的问题,只有消息成功被consumer接受,事务提交才能成功,否则我们便可以在捕获异常进行事务回滚同时进行消息重发,但是使用事务机制的话会降低RabbMQ的性能,那么有没有更好的方法既然保障provider知道消息已经正确送到,又能基本上不带来性能上的损失呢 ? 从AMQP协议的层面看是没有更好的方法,但是RabbitMQ提供了一个更好的方案,即将channel设置成confirm模式.






































