####  一 . 订阅模式

即一个生产者发送消息给多个消费者,且每个消费者都收到一次,也即是一个消息能够被多个消费者消费 . 类似于我们订阅同一微信公众号,微信公众号推送图文,我们每个人都能收到一份

![](https://user-gold-cdn.xitu.io/2018/9/2/165983e9ead49978?w=352&h=162&f=png&s=12638)

#### 二 .fanout交换机

之前我们直接发送消息到队列,这里指定的交换机名称是 "" .

> ```java
> channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
> ```

生产者发送消息给交换机,交换机分发绑定在交换机上的队列,每个队列对应一个消费者 . 

交换机有几种类型 ,分别是 : **direct , topic ,handlers , fanout**

这里我们只讨论一种,就是 **fanout**

> fanout 表示分发,即交换机的消息分发到每个绑定在交换机的队列上

根据上面的图片所示,有几个过程 : 

1. 生产者发送消息到交换机
2. 队列绑定到交换机
3. 消费者消费队列

#### 三 . 代码演示

> 实际情况一般是队列有可能对应多个消费者,比如一个注册事件.既要发送邮件,又要发送短信 , 所有先把消息发送到交换机,然后分发到两个队列 , 一个是邮件队列,一个是短信队列 . 服务器可能有多台,也即是每个队列可能有多个消费者,为了提高性能,也就是订阅模式与工作队列结合的一种形式 . 下面演示这个情况

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
  import com.rabbitmq.client.BuiltinExchangeType;
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

    private static final String EXCHANGE_NAME = "test_exchange_fanout";

    public static void main(String[] args) throws IOException, TimeoutException {
      // 1. 创建连接
      Connection connection = ConnectionUtils.getConnection();
      // 2. 创建通道
      Channel channel = connection.createChannel();
      // 3. 申明一个fanout 分发交换机
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

      String message = "Hello RabbitMQ";

      // 发送消息
      channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());

      System.out.println("[ x ] Send '" + message + "'");

      // 关闭连接与通道
      channel.close();
      connection.close();

    }
  }

  ```

- 短信消费者

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
   * # 短信消费者
   *
   * @Author: jamie.li
   * @Date: Created in  2018/9/2 10:58
   */
  public class Recv {

    // 短信队列
    private static final String QUEUE_NAME = "test_queue_fanout_sms";
    private static final String EXCHANGE_NAME = "test_exchange_fanout";

    public static void main(String[] args) throws IOException, TimeoutException {
      // 1. 获取连接
      Connection connection = ConnectionUtils.getConnection();
      // 2. 创建通道
      Channel channel = connection.createChannel();
      // 3. 申明要消费的队列
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);

      // 4. 绑定队列到交换机
      channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
      // 5. 这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它。
      channel.basicQos(1);

      // 6.创建一个回调的消费者处理类
      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
          // 接收到的消息
          String message = new String(body);
          System.out.println(" [1] Received '" + message + "'");

          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          } finally {
            System.out.println(" [1] done ");
            channel.basicAck(envelope.getDeliveryTag(), false);
          }
        }
      };

      // 消费消息
      channel.basicConsume(QUEUE_NAME, false, consumer);
    }

  }

  ```

  ​

- 邮件消费者

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
   * 邮件消费者
   */
  public class Recv2 {

      // 邮件队列
      private static final String QUEUE_NAME = "test_queue_fanout_email";
      private static final String EXCHANGE_NAME = "test_exchange_fanout";

      public static void main(String[] args) throws IOException, TimeoutException {
          // 获取连接
          Connection connection = ConnectionUtils.getConnection();

          // 打开通道
          Channel channel = connection.createChannel();

          // 申明要消费的队列
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);

          // 绑定队列到交换机
          channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

          // 这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它。
          channel.basicQos(1);

          // 创建一个回调的消费者处理类
          Consumer consumer = new DefaultConsumer(channel) {
              @Override
              public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                  // 接收到的消息
                  String message = new String(body);
                  System.out.println(" [2] Received '" + message + "'");

                  try {
                      Thread.sleep(1000);
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
  ```

#### 四 . 测试结果

提前在管理控制台创建一个direct,或者先执行一次生产者(执行时会判断交换机是否存在,不存在则创建交换机) , 这样保证交换机存在,不能直接启动消费者会提示交换机不存在.

![](https://user-gold-cdn.xitu.io/2018/9/2/1659887dca7637c5?w=879&h=530&f=png&s=47891)

**注意点**

> 如果在没有队列绑定到交换机上面时,往交换机发送消息会丢失,之后绑定在交换机上面的队列接收不到之前的消息,也就是先执行第一次发送,创建了交换机,但是还没有队列绑定在交换机上面,如果这次发送的消息就会丢失.

然后再启动两个消费者,再执行生产者. 

Send : 

> [ x ] Send 'Hello RabbitMQ'

Recv : 

> [1] Received 'Hello RabbitMQ'
>  [1] done

Recv2 : 

>[1] Received 'Hello RabbitMQ'
> [1] done

同样的我们可以再加两个消费者,分别消费短信和邮件,也就是变成了工作队列

所以上面代码可以实现订阅模式+工作队列的结合











































