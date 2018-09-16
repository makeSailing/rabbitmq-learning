## RabbitMQ实战教程(二) : 简单队列

###  1. 简单队列

官方内容参考：<http://www.rabbitmq.com/tutorials/tutorial-one-java.html>

即一处生产者对应一个消费者,一对一的关系,不多个消费者消费

![](https://user-gold-cdn.xitu.io/2018/9/16/165e13290fb5b646?w=394&h=115&f=png&s=7685)

### 2. 代码演示

####  2.1 采用 maven 方式 创建项目

首先引入`RabbitMQ`的Java客户端依赖

```xml
<dependency>
    <groupId>com.rabbitmq</groupId>
    <artifactId>amqp-client</artifactId>
    <version>5.0.0</version>
</dependency>
```

#### 2.2  工具类

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

#### 2.3 生产者

```java
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
 * @Date: Created in  2018/9/16 14:45
 */
public class Send {

  public static final String QUEUE_NAME = "test_simple_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1. 获取连接
    Connection connection = ConnectionUtils.getConnection();
    // 2.创建信道
    Channel channel = connection.createChannel();
    // 3.申明一个队列,没有就会创建
    /**
     * queue : 队列名称
     */
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // 4.发送消息
    String message = " Hello RabbitMQ";
    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

    System.out.println(" Send 发送的 ' " + message + " '");

    // 关闭通道和连接
    channel.close();
    connection.close();
  }
}

```

Api 说明 : 

```java
Queue.DeclareOk queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete,Map<String, Object> arguments)throws IOException
queue: 队列名称
durable:是否持久化,true表示RabbitMQ重启后,队列仍然存在
exclusive: true表示当前连接的专用队列,在连接断开后,会自动删除该队列
autoDelete: true表示当没有任何消费者使用时,自动删除该队列
arguments: 该队列其他配置参数
```

```java
void basicPublish(String exchange, String routingKey, BasicProperties props, byte[] body)throws IOException

exchange: 指定交换机，这里为简单队列，不需要使用，默认""即可，不能为null
routingKey: 交换机为"",路由key这里为队列名称.
props: 其他消息属性，路由头信息等等
body: 消息byte内容
```

#### 2.4 消费者

```java
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

```

Api说明:

```java
String basicConsume(String queue, boolean autoAck, Consumer callback) throws IOException;
queue: 队列名称
autoAck: 是否自动应答,即生产者发送消息即认为该消息被消费
callback: 回调处理类,即消息被消费时进行回调处理
```

### 3 . 测试

- 执行 `Send`生产者的main方法

  可以看到在`RabbitMQ Management` --> `Queue`中会有自己声明的队列 ,Total代表消息的总条数,现有1条消息

  ![](https://user-gold-cdn.xitu.io/2018/9/16/165e15027ed814a4?w=927&h=549&f=png&s=56486)

- 执行 `Receive`接收者的main方法

  消费者获取到了一条消息，并且`RabbitMQ Management`中的对应的队列的消息总条数为0

  ![](https://user-gold-cdn.xitu.io/2018/9/16/165e152304ecc220?w=797&h=213&f=png&s=38758)

  ![](https://user-gold-cdn.xitu.io/2018/9/16/165e152e49c809b6?w=936&h=557&f=png&s=57138)

### 示例说明

系统会为每个队列都隐式的绑定一个默认的交换机,交换机的名称为`AMQP default`,类型为直连接`direct`,当你手动创建一个队列时,后台会自动将这个队列绑定到一个名称为空`Direct`类型交换机上,绑定路由名称与队列名称相同,相当于`channel.queueBind(queue:"QUEUE_NAME",exchange:"AMQP default",routingKey:"QUEUE_NAME")`; 所以 `Hello RabbitMQ`消息虽然没有显示声明交换机,当路由键和队列名称一样时就将消息发送到这个默认的交换机里 . 有了这个默认的交换机和绑定,我们就可以像其他轻量级的队列，如Redis那样，直接操作队列来处理消息。不过理论上是可以的，但实际上在RabbitMQ里直接操作是不可取的。消息始终都是先发送到交换机，由交换级经过路由传送给队列，消费者再从队列中获取消息的。不过由于这个默认交换机和路由的关系，使我们只关心队列这一层即可，这个比较适合做一些简单的应用，毕竟没有发挥RabbitMQ的最大功能(RabbitMQ可以重量级消息队列)，如果都用这种方式去使用的话就真是杀鸡用宰牛刀了。

**额外测试：**

你可以先启动两个消费者，然后再执行一个生产者，会发现先启动的消费者才能收到消息，其他的不会收到消息，表示消息只能由一个消费者消费，这就是简单队列模型.