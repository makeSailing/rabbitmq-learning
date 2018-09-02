####  一 . 订阅模式

> 跟订阅模式类似,只不过在订阅模式的基础上加上了类型,订阅模式是分发到所有绑定到交换机的队列,路由模式只分发到绑定在交换机上面指定的路由键的队列.

![](https://user-gold-cdn.xitu.io/2018/9/2/165989d6459037d3?w=402&h=168&f=png&s=18564)



#### 二 . direct交换机

生产者申明一个direct类型的交换机,然后发送消息到这个交换机指定路由键.

消费者指定消费这个交换机的这个路由键,即可接收到消息,其他消费者收不到.

订阅模式中生产者:

> ```java
> channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
> ```

第二个参数就是路由的键

消费者 : 

> ```java
> channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
> ```

第三个参数就是路由键

#### 三 . 代码演示

> 同样的,只是交换机类型改为direct , 加了个路由键而已.
>
> 这里按图演示两个,即表示一个日志事件,根据日志类型进行处理

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

    private static final String EXCHANGE_NAME = "test_exchange_routing_direct";

    public static void main(String[] args) throws IOException, TimeoutException {
      // 1. 创建连接
      Connection connection = ConnectionUtils.getConnection();
      // 2. 创建通道
      Channel channel = connection.createChannel();
      // 3. 申明一个direct分发交换机
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

      // 发给info路由键消息
      String infoMessage = "Hello ,info";
      channel.basicPublish(EXCHANGE_NAME, "info", null, infoMessage.getBytes());
      System.out.println(" [ x ] Send rounting info message '" + infoMessage + "'");

      // 发送error路由键消息
      String errorMessage = "Hello , error";
      channel.basicPublish(EXCHANGE_NAME, "error", null, errorMessage.getBytes());
      System.out.println("[ x ] Send rounting info message '" + errorMessage + "'");

      // 关闭连接与通道
      channel.close();
      connection.close();

    }
  }

  ```

- info消费者

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
    private static final String QUEUE_NAME = "test_queue_routing_info";
    private static final String EXCHANGE_NAME = "test_exchange_routing_direct";

    public static void main(String[] args) throws IOException, TimeoutException {
      // 1. 获取连接
      Connection connection = ConnectionUtils.getConnection();
      // 2. 创建通道
      Channel channel = connection.createChannel();
      // 3. 申明要消费的队列
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);

      // 4. 绑定队列到交换机
      channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "info");
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

- error消费者

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
      private static final String QUEUE_NAME = "test_queue_routing_error";
      private static final String EXCHANGE_NAME = "test_exchange_routing_direct";

      public static void main(String[] args) throws IOException, TimeoutException {
          // 获取连接
          Connection connection = ConnectionUtils.getConnection();

          // 打开通道
          Channel channel = connection.createChannel();

          // 申明要消费的队列
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);

          // 绑定队列到交换机
          channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "error");

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

![](https://user-gold-cdn.xitu.io/2018/9/2/16598a5ed106ffe9?w=694&h=535&f=png&s=48056)

**注意点**

> 如果在没有队列绑定到交换机上面时,往交换机发送消息会丢失,之后绑定在交换机上面的队列接收不到之前的消息,也就是先执行第一次发送,创建了交换机,但是还没有队列绑定在交换机上面,如果这次发送的消息就会丢失.

然后再启动两个消费者,再执行生产者. 

Send : 

> [x] Sent routing info message : 'Hello , error'
>  [x] Sent routing error message :  'Hello,  error'

Recv : 

>  [1] Received 'Hello ,info'
>  [1] done 

Recv2 : 

>[2] Received 'Hello , error'
> [2] done 

> 我们可以看到生产者往info路由键发送消息时,只有执行消费者info路由键的消费者才能接收到消息 , error 路由键同样

#### 五 . 多绑定的情况

- 1. 同一个消费者绑定队列到多个路由键

  >```java
  >channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "info");
  >channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "error");
  >channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "warn");
  >```

  > 如果一个消费者绑定了这三个路由键,那么当生产者发送其中一个路由键时,该消费者都接收到消息


- 多个消费者绑定相同的路由键

  即消费者1绑定info , 消费者2绑定 info ,error

  >那么生产者发送info路由键消息时，消费者1, 2都能接收到消息，发送error路由键消息时，只有消费者2能接收到消息。

  ​






































