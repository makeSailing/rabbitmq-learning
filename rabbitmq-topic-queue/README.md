

####  一 . 主题模式

> 跟路由模式类似,只不过路由模式是指定固定的路由键 . 而主题模式是可以模糊匹配路由键 , 类似于 SQL中 = 等于 like的关系

![](https://user-gold-cdn.xitu.io/2018/9/2/165996e3d90db104?w=405&h=154&f=png&s=16015)

#### 二 . topic交换机

这个就不解决了 ,配置路由键的时候可以配置 * , # 来模糊匹配

> ```java
> * (star) can substitute for exactly one word.
>
> * 号表示可以精确匹配一个单词
>
> # (hash) can substitute for zero or more words.
>
> # 号可以匹配0个或者多个单词
>
> ```

eg : 如上图的主题模式中 , Q1绑定* .orange. 路由键, Q2绑定两个路由键 , 分别是 *.*.rabbit以及lazy.#

```
1.如果生产者发送路由键为 quick.orange.rabbit , C1和C2都可以接收到
2.如果为lazy.orange.elephant,C1和C2都可以接收到
3.如果quick.orange.fox ,只有C1可以接收到
4.如果为lazy.brown.fox , 只有C2可以接收到
5.如果为lazy.prink.rabbit ,C1、C2都可以接收到
6.如果为quick.brown.fox , 都不接收到

```

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

    private static final String EXCHANGE_NAME = "test_exchange_topic";

    public static void main(String[] args) throws IOException, TimeoutException {
      // 1. 创建连接
      Connection connection = ConnectionUtils.getConnection();
      // 2. 创建通道
      Channel channel = connection.createChannel();
      // 3. 申明一个topic分发交换机
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

      // 发给info路由键消息
      String infoMessage = "Hello ,quick.orange.rabbit";
      channel.basicPublish(EXCHANGE_NAME, "quick.orange.rabbit", null, infoMessage.getBytes());
      System.out.println(" [ x ] Send topic message '" + infoMessage + "'");

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
   * # 消费者1
   *
   * @Author: jamie.li
   * @Date: Created in  2018/9/2 10:58
   */
  public class Recv {

    // 短信队列
    private static final String QUEUE_NAME = "test_queue_topic_1";
    private static final String EXCHANGE_NAME = "test_exchange_topic";

    public static void main(String[] args) throws IOException, TimeoutException {
      // 1. 获取连接
      Connection connection = ConnectionUtils.getConnection();
      // 2. 创建通道
      Channel channel = connection.createChannel();
      // 3. 申明要消费的队列
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);

      // 4. 绑定队列到交换机
      channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "*.orange.*");
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

- 消费者2

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
      private static final String QUEUE_NAME = "test_queue_topic_2";
      private static final String EXCHANGE_NAME = "test_exchange_topic";

      public static void main(String[] args) throws IOException, TimeoutException {
          // 获取连接
          Connection connection = ConnectionUtils.getConnection();

          // 打开通道
          Channel channel = connection.createChannel();

          // 申明要消费的队列
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);

          // 绑定队列到交换机
          channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "*.*.rabbit");
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "lazy.#");
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

![](https://user-gold-cdn.xitu.io/2018/9/2/165997c5086dbbb3?w=708&h=542&f=png&s=53971)

**注意点**

> 如果在没有队列绑定到交换机上面时,往交换机发送消息会丢失,之后绑定在交换机上面的队列接收不到之前的消息,也就是先执行第一次发送,创建了交换机,但是还没有队列绑定在交换机上面,如果这次发送的消息就会丢失.

然后再启动两个消费者,再执行生产者. 

Send : 

>  [x] Sent message : 'hello, quick.orange.rabbit'	

Recv : 

>   [1] Received 'hello, quick.orange.rabbit'
>   [1] done 

Recv2 : 

> [2] Received 'hello, quick.orange.rabbit'
> [2] done 

> 我们可以看到生产者往quick.orange.rabbit路由键发送消息时,两个消费者都收到了消息，说明都匹配到了。

#### 五 . 其他情况测试结果

- 1. 测试生产者发送lazy.orange.elephant

  >```java
  >结果：两个消费者也都能收到消息。	
  >```



- 2. 测试生产者发送quick.orange.fox

  >只要消费者1能接收到。

  ​







































