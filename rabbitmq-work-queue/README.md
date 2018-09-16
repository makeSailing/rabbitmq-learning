## RabbitMQ实战教程(四) : 工作队列

###  1. 工作队列

- 简单队列不足 : 不支持多个消费者

  即一个生产者可以对应多个消费者同时消费,相比简单队列支持多消费者 . 因为实际工作中,生产者服务一般都是很简单的业务逻辑处理之后就发送到队列,消费者接收到队列的消息之后,进行复杂的业务逻辑处理,所以一般都是多个消费者进行处理.如是是一个消费者进行处理,那么队列会积压很多消息.

![](https://user-gold-cdn.xitu.io/2018/9/16/165e2214b92fcca2?w=324&h=140&f=png&s=9897)

工作队列分为两种情况:

- `轮询分发`

  > 在默认情况下, `RabbitMQ`将逐个发送消息到在序列中的下一个消费者(而不考虑每个任务处理的时长等等,且是提前一次性分配,并非一个一个的分配) . 平均每个消费者获取相同数量的消息.  这种分发消息机制称为 `轮询分发`
  >
  > 当消息进入队列 ,`RabbitMQ`就会分发消息 .它不看消费者的应答的数目 ,也不关心消费者处理消息的能力,只是盲目的将第n条消息发给第n个消费者

- `公平分发`

  > 根据消费者处理性能,性能好的消费的数据量多,性能差的消费的数据量少 .这种分发消息机制称为 `公平分发`

### 2.  轮询分发 

> 一处生产者,两个消费者, 其中消费者1处理需要 1s,消费者2处理需要2s .

#### 2.1 连接工具类

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

#### 2.2  生产者

```java
package com.makesailing.neo.polling.provider;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 16:31
 */
public class Send {

  public static final String QUEUE_NAME = "test_work_polling_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1.创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 2.创建信道
    Channel channel = connection.createChannel();
    // 3.声明信道中的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    for (int i = 0; i < 20; i++) {
      String meassage = "Hello RabbitMQ " + i;
      //发送消息
      channel.basicPublish("", QUEUE_NAME, null, meassage.getBytes());
      System.out.println("Send 发送消息" + meassage);
    }

    // 关闭信道与连接
    channel.close();
    connection.close();
  }
}

```

#### 2.3  消费者1

```java
package com.makesailing.neo.polling.consumer;

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
 * # 轮询分发 消费者1
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 16:41
 */
public class Recvice1 {

  public static final String QUEUE_NAME = "test_work_polling_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1.创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 2.创建信道
    Channel channel = connection.createChannel();
    // 3.声明信道中的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    // 4. 创建一个回调类
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
          throws IOException {
        super.handleDelivery(consumerTag, envelope, properties, body);
        String message = new String(body, "UTF-8");
        // 工作
        doWork(message);
      }
    };

    // 消费消息
    channel.basicConsume(QUEUE_NAME, true, consumer);

  }

  private static void doWork(String message) {
    System.out.println(" [1] Received '" + message + "', 处理业务中...");
    // 模仿消费者处理业务的时间，也让其他消费者有机会获取到消息，实际开发中不需要，这里只是模拟
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      System.out.println("[1] done");
    }
  }
}

```

#### 2.4 消费者2 

```java
package com.makesailing.neo.polling.consumer;

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
 * # 轮询分发 消费者2
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 16:54
 */
public class Recvice2 {
  public static final String QUEUE_NAME = "test_work_polling_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 1.创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 2.创建信道
    Channel channel = connection.createChannel();
    // 3.声明信道中的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    // 4. 创建一个回调类
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
          throws IOException {
        super.handleDelivery(consumerTag, envelope, properties, body);
        String message = new String(body, "UTF-8");
        // 工作
        doWork(message);
      }
    };

    // 消费消息
    channel.basicConsume(QUEUE_NAME, true, consumer);

  }

  private static void doWork(String message) {
    System.out.println(" [2] Received '" + message + "', 处理业务中...");
    // 模仿消费者处理业务的时间，也让其他消费者有机会获取到消息，实际开发中不需要，这里只是模拟
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      System.out.println("[2] done");
    }
  }
}

```

#### 2.5 测试

为了能让两个消费者均分消息,需要先启动两个消费者,再启动生产者.

生产者: ![](https://user-gold-cdn.xitu.io/2018/9/16/165e2362ba0677b1?w=783&h=602&f=png&s=93321)

消费者1 :

![](https://user-gold-cdn.xitu.io/2018/9/16/165e2366d2305344?w=697&h=591&f=png&s=62852)

消费者2：

![](https://user-gold-cdn.xitu.io/2018/9/16/165e236c6143d88a?w=695&h=581&f=png&s=72641)

#### 2.6 总结

`可以发现消费者1的数字全是偶数,消费者2的数字全是奇数,证明轮询分发,不管消费者性能的强弱都能接受均发的消费.`

### 3. 公平分发

- 从 `轮询分发` 可以看出消费者2处理时长1s ,消费者2处理时长2s ,很可能出现当消费者1才处理几条时,消费者2就已经处理完了,这样消费者2就处于空闲状态,而消费者1却忙的跟狗似的 .为了解决这种现象,让性能好的消费者干完了帮助性能差的消费者分担点任务,采用`公平分发` .

![](https://user-gold-cdn.xitu.io/2018/9/16/165e24904a13e3fb?w=411&h=136&f=png&s=13506)

在默认轮询分发的基础上,要实现公平分发,需要两点 : 

1. 限制发给同一消费者不得超过1条消息,在这个消费者确认消息之前,不会发送下一条消息给这个消费者 .

   > int prefetchCount = 1;
   > channel.basicQos(prefetchCount);

2. 默认自动应答改为手动应答

   >关闭自动应答
   >boolean autoAck = false;
   >channel.basicConsume(QUEUE_NAME, autoAck, consumer);
   >
   >手动应答
   >channel.basicAck(envelope.getDeliveryTag(), false);
   >
   >DeliveryTag 用来标识信道中投递的消息， RabbitMQ 推送消息给 Consumer 时，会附带一个 Delivery Tag，以便 Consumer 可以在消息确认时告诉 RabbitMQ 到底是哪条消息被确认了。

#### 3.1 生产者

   ```java
   package com.makesailing.neo.fair.provider;

   import com.makesailing.neo.utils.ConnectionUtils;
   import com.rabbitmq.client.Channel;
   import com.rabbitmq.client.Connection;
   import java.io.IOException;
   import java.util.concurrent.TimeoutException;

   /**
    * #
    *
    * @Author: jamie.li
    * @Date: Created in  2018/9/16 16:31
    */
   public class Send {

     public static final String QUEUE_NAME = "test_work_fair_queue";

     public static void main(String[] args) throws IOException, TimeoutException {
       // 1.创建连接
       Connection connection = ConnectionUtils.getConnection();
       // 2.创建信道
       Channel channel = connection.createChannel();
       // 3.声明信道中的队列
       channel.queueDeclare(QUEUE_NAME, false, false, false, null);


       for (int i = 0; i < 20; i++) {
         String meassage = "Hello RabbitMQ " + i;
         //发送消息
         channel.basicPublish("", QUEUE_NAME, null, meassage.getBytes());
         System.out.println("Send 发送消息" + meassage);

       }

       // 关闭信道与连接
       channel.close();
       connection.close();
     }
   }

   ```

#### 3.2 消费者1

   ```java
   package com.makesailing.neo.fair.consumer;

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
    * # 轮询分发 消费者1
    *
    * @Author: jamie.li
    * @Date: Created in  2018/9/16 16:41
    */
   public class Recvice1 {

     public static final String QUEUE_NAME = "test_work_fair_queue";

     public static void main(String[] args) throws IOException, TimeoutException {
       // 1.创建连接
       Connection connection = ConnectionUtils.getConnection();
       // 2.创建信道
       Channel channel = connection.createChannel();
       // 3.声明信道中的队列
       channel.queueDeclare(QUEUE_NAME, false, false, false, null);
       // 设置每次从队列获取消息的数量
       int prefetchCount = 1 ;
       channel.basicQos(prefetchCount);

       // 4. 创建一个回调类
       Consumer consumer = new DefaultConsumer(channel) {
         @Override
         public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
             throws IOException {
           super.handleDelivery(consumerTag, envelope, properties, body);
           String message = new String(body, "UTF-8");
           // 工作
           doWork(message);

           // 手动应答
           channel.basicAck(envelope.getDeliveryTag(), false);
         }
       };

       // 消费消息 公平分发,关闭自动应答
       channel.basicConsume(QUEUE_NAME, false, consumer);

     }

     private static void doWork(String message) {
       System.out.println(" [1] Received '" + message + "', 处理业务中...");
       // 模仿消费者处理业务的时间，也让其他消费者有机会获取到消息，实际开发中不需要，这里只是模拟
       try {
         Thread.sleep(1000);
       } catch (InterruptedException e) {
         e.printStackTrace();
       } finally {
         System.out.println("[1] done");
       }
     }
   }

   ```

#### 3.3消费者2

   ```java
   package com.makesailing.neo.fair.consumer;

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
    * # 轮询分发 消费者2
    *
    * @Author: jamie.li
    * @Date: Created in  2018/9/16 16:54
    */
   public class Recvice2 {
     public static final String QUEUE_NAME = "test_work_fair_queue";

     public static void main(String[] args) throws IOException, TimeoutException {
       // 1.创建连接
       Connection connection = ConnectionUtils.getConnection();
       // 2.创建信道
       Channel channel = connection.createChannel();
       // 3.声明信道中的队列
       channel.queueDeclare(QUEUE_NAME, false, false, false, null);

       // 设置每次从队列获取消息的数量
       // 换句话讲 在接收该 Consumer的 ack之前,他不会将新的message发给它
       int prefetchCount = 1 ;
       channel.basicQos(prefetchCount);
       // 4. 创建一个回调类
       Consumer consumer = new DefaultConsumer(channel) {
         @Override
         public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
             throws IOException {
           super.handleDelivery(consumerTag, envelope, properties, body);
           String message = new String(body, "UTF-8");
           // 工作
           doWork(message);

           channel.basicAck(envelope.getDeliveryTag(), false);
         }
       };

       // 消费消息
       channel.basicConsume(QUEUE_NAME, false, consumer);

     }

     private static void doWork(String message) {
       System.out.println(" [2] Received '" + message + "', 处理业务中...");
       // 模仿消费者处理业务的时间，也让其他消费者有机会获取到消息，实际开发中不需要，这里只是模拟
       try {
         Thread.sleep(2000);
       } catch (InterruptedException e) {
         e.printStackTrace();
       } finally {
         System.out.println("[2] done");
       }
     }
   }

   ```

#### 3.4测试 

   ![](https://user-gold-cdn.xitu.io/2018/9/16/165e25267be3a2d6?w=729&h=580&f=png&s=87195)

![](https://user-gold-cdn.xitu.io/2018/9/16/165e252f6b2786f6?w=512&h=605&f=png&s=66155)


![](https://user-gold-cdn.xitu.io/2018/9/16/165e2532dfbbbf2d?w=628&h=547&f=png&s=73988)

#### 3.5 总结

显然消费者1处理只要1s ,所以消费的记录数比消费者2要多很多 .表示确实是公平分发

注意点 : 

```java
当关闭自动应答autoAck=false之后，在消费者处理消费数据之后一定要对消息进行手动反馈处理，可以是basicAck，也可以是basicNack， basicReject

BasicReject一次只能拒绝接收一个消息，而BasicNack方法可以支持一次0个或多个消息的拒收，并且也可以设置是否requeue。

// 拒绝当前消息，并使这条消息重新返回到队列中
channel.basicNack(envelope.getDeliveryTag(), false, true);
相当于
channel.basicReject(envelope.getDeliveryTag(), true);
```




