## RabbitMQ实战教程(五)-路由模式

### 1. 路由模式

> 跟订阅模式类似,只不过在订阅模式的基础上加上路由,订阅模式是分发到所有绑定到该交换机的队列,路由模式只分发到绑定在该交换机上面指定的路由键队列.

![](https://user-gold-cdn.xitu.io/2018/9/16/165e319389396035?w=393&h=170&f=png&s=18586)

### 2. Direct 直接交换机

直连交换机(`Direct Exchange`)是一种带路由功能的交换机,它将消息中的`Routing Key`与该交换机关联的所有`Binding`中的`Routing Key`进行比较,如果`完全相等`将消息发送到`Binding`对应的队列中 .

适用场景 : 根据任务的优先级把消息发送到对应的队列中,分配更多资源处理优先级高的队列.

生产者声明一个direct类型的交换机,然后发送消息到这个交换机指定路由键. 消费者指定消费交换机的路由键,即可以接到到消息,其他消费者接收不到 .

在`Fanout`交换机中

生产者 : 

> ```java
> // 第二个参数就是路由键
> channel.basicPublish(EXCHANE_NAME,"",null,message.getBytes());
> ```

消费者 : 

> ```java
> // 第三个参数就是路由键
> channel.queueBind(QUEUE,EXCAHNGE_NAME,"");
> ```

![](https://user-gold-cdn.xitu.io/2018/9/16/165e31a139535c55?w=433&h=227&f=png&s=25432)

### 3. 代码演示

> 同样的,只是交换机类型改为`driect`,加了个路由键而已 .
>
> 这里演示3个,即表示一个日志事件,根据日志类型进行处理

#### 3.1 工具类

```java
package com.makesailing.neo.utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * # RabbitMQ连接工具类
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 14:38
 */
public class ConnectionUtils {

  public static final String host = "127.0.0.1";

  public static final Integer port = 5672;

  public static Connection getConnection() throws IOException, TimeoutException {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(host);
    connectionFactory.setPort(port);
    // 如果有 用户名 密码 vhost 配置即可
    connectionFactory.setUsername("jamie");
    connectionFactory.setPassword("123456");
    connectionFactory.setVirtualHost("/simple");
    return connectionFactory.newConnection();
  }
}

```

#### 3.2 日志生产者

```java
package com.makesailing.neo.provider;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * # 日志消息 提供者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 22:41
 */
public class LogSend {

  private static final String EXCHANGE_NAME = "test_exchange_direct";

  private static final String INTO_ROUTING_NAME= "info";
  private static final String WARN_ROUTING_NAME= "warn";
  private static final String ERROR_ROUTING_NAME= "error";


  public static void main(String[] args) throws IOException, TimeoutException {
    // 创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 获取信道
    Channel channel = connection.createChannel();
    // 声明一个direct 路由交换机
    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

    // 发送info路由键消息
    String infoMessage = "Hello RabbitMQ Info Log";
    channel.basicPublish(EXCHANGE_NAME, INTO_ROUTING_NAME, null, infoMessage.getBytes());
    System.out.println("  LogSend routing info message : '" + infoMessage + "'");
    // 发送warn路由键消息
    String warnMessage = "Hello RabbitMQ Warn Log";
    channel.basicPublish(EXCHANGE_NAME, WARN_ROUTING_NAME, null, warnMessage.getBytes());
    System.out.println("  LogSend routing warn message : '" + warnMessage + "'");
    // 发送info路由键消息
    String errorMessage = "Hello RabbitMQ Error Log";
    channel.basicPublish(EXCHANGE_NAME, ERROR_ROUTING_NAME, null, errorMessage.getBytes());
    System.out.println("  LogSend routing error message : '" + errorMessage + "'");

    channel.close();
    connection.close();
  }
}

```

#### 3.3 error日志消费者

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
 * # 错误日志 消费者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 23:09
 */
public class ErrorReceive {
  private static final String EXCHANGE_NAME = "test_exchange_direct";
  // info日志队列
  private static final String QUEUE_NAME = "test_queue_routing_error";

  private static final String ERROR_ROUTING_NAME= "error";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 获取连接
    Connection connection = ConnectionUtils.getConnection();

    // 打开通道
    Channel channel = connection.createChannel();

    // 申明要消费的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // 绑定队列到交换机
    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ERROR_ROUTING_NAME);

    // 这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它。
    channel.basicQos(1);

    // 创建一个回调的消费者处理类
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        // 接收到的消息
        String message = new String(body);
        System.out.println(" ErrorReceive '" + message + "' , 任务处理中");

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          System.out.println(" ErrorReceive done ");
          channel.basicAck(envelope.getDeliveryTag(), false);
        }
      }
    };

    // 消费消息
    channel.basicConsume(QUEUE_NAME, false, consumer);

  }
}

```

#### 3.4 info日志消费者

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

```

#### 3.5 warn日志消费者

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
 * # 警告日志 消费者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 23:09
 */
public class WarnReceive {
  private static final String EXCHANGE_NAME = "test_exchange_direct";
  // info日志队列
  private static final String QUEUE_NAME = "test_queue_routing_warn";

  private static final String INTO_ROUTING_NAME= "info";
  private static final String WARN_ROUTING_NAME= "warn";
  private static final String ERROR_ROUTING_NAME= "error";


  public static void main(String[] args) throws IOException, TimeoutException {
    // 获取连接
    Connection connection = ConnectionUtils.getConnection();

    // 打开通道
    Channel channel = connection.createChannel();

    // 申明要消费的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // 绑定队列到交换机
    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, WARN_ROUTING_NAME);
    //channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, INTO_ROUTING_NAME);
    //channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ERROR_ROUTING_NAME);

    // 这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它。
    channel.basicQos(1);

    // 创建一个回调的消费者处理类
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        // 接收到的消息
        String message = new String(body);
        System.out.println("WarnReceive '" + message + "' , 任务处理中");

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          System.out.println(" WarnReceive done ");
          channel.basicAck(envelope.getDeliveryTag(), false);
        }
      }
    };

    // 消费消息
    channel.basicConsume(QUEUE_NAME, false, consumer);

  }
}

```

### 4. 测试

提前在`RabbitMQ Management`创建一个`direct`交换机，或者先执行一次生产者（执行时会判断交换机是否存在，不存在则创建交换机），这样保证交换机存在，不然直接启动消费者会提示交换机不存在。

**注意点**

> 如果在没有队列绑定在交换机上面时，往交换机发送消息会丢失，之后绑定在交换机上面的队列接收不到之前的消息，也就是先执行第一次发送，创建了交换机，但是还没有队列绑定在交换机上面，如果这次发送的消息就会丢失。

然后再启动3上消费者,最后在启动生产者.

**运行结果 :**

![](https://user-gold-cdn.xitu.io/2018/9/17/165e5021a8d39431?w=753&h=338&f=png&s=60750)


![](https://user-gold-cdn.xitu.io/2018/9/17/165e503f2157a363?w=724&h=261&f=png&s=45057)


![](https://user-gold-cdn.xitu.io/2018/9/17/165e50422ceed4ab?w=595&h=244&f=png&s=42787)


![](https://user-gold-cdn.xitu.io/2018/9/17/165e5045e7651a7a?w=740&h=276&f=png&s=46682)

### 5. 多绑定情况

#### 5.1 同一消费者绑定队列多个路由键

> ```java
> channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "info");
> channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "error");
> channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "warn");
> ```

在`warn消费者中队列绑定多个路由键 : 

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
 * # 警告日志 消费者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 23:09
 */
public class WarnReceive {
  private static final String EXCHANGE_NAME = "test_exchange_direct";
  // info日志队列
  private static final String QUEUE_NAME = "test_queue_routing_warn";

  private static final String INTO_ROUTING_NAME= "info";
  private static final String WARN_ROUTING_NAME= "warn";
  private static final String ERROR_ROUTING_NAME= "error";


  public static void main(String[] args) throws IOException, TimeoutException {
    // 获取连接
    Connection connection = ConnectionUtils.getConnection();

    // 打开通道
    Channel channel = connection.createChannel();

    // 申明要消费的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // 绑定队列到交换机
    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, WARN_ROUTING_NAME);
    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, INTO_ROUTING_NAME);
    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ERROR_ROUTING_NAME);

    // 这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它。
    channel.basicQos(1);

    // 创建一个回调的消费者处理类
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        // 接收到的消息
        String message = new String(body);
        System.out.println("WarnReceive '" + message + "' , 任务处理中");

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          System.out.println(" WarnReceive done ");
          channel.basicAck(envelope.getDeliveryTag(), false);
        }
      }
    };

    // 消费消息
    channel.basicConsume(QUEUE_NAME, false, consumer);

  }
}

```

**运行结果:**

![](https://user-gold-cdn.xitu.io/2018/9/17/165e508498b16ce6?w=750&h=338&f=png&s=59824)

> 如果一个消费者绑定了这3个路由键,那么当生产者发送其中一个路由键消息时,该消费者都能接收到消息.

#### 5.2 多个消费者绑定相同的路由键

即消费者1绑定info,消费者2绑定 info、error .

> 那么生产者发送info路由键消息时,消费者1 、2都能接收到消息,发送error路由键消息时,只有消费者2能接收到消息.
































