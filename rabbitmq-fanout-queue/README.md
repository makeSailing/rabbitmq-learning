##RabbitMQ实战教程(四)_扇形交换机发布订阅(Publish-Subscribe)

### 1. 订阅模式

即一个生产者发送消息给多个消费者,且每个消费者都收到一次,也即是一个消息能够被多个消费者消费.

发布订阅类似观察者设计模式,一般适用于当接收到某条消息时同时作多种类似的任务处理, 如果用户注册,注册成功需要发送短信、发送邮件到注册邮箱.

![](https://user-gold-cdn.xitu.io/2018/9/16/165e299a477c6e28?w=324&h=156&f=png&s=12420)

### 2. fanout 扇形交换机

之前我们直接发送消息到队列,这是指的交换机名称是 `""` ,为默认交换机

> ```java
> channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
> ```

#### 2.1 交换机作用与类型

交换机的作用是接收生产者的消息,并将消息路由到已经绑定到该交换机且符合路由规则的队列中去 . `RabbitMQ`定义了如下4种交换机[直连交换机、扇形交换机、主题交换机、首部交换机]，交换机有如下属性: 

![](https://user-gold-cdn.xitu.io/2018/9/16/165e2a0094e29e1e?w=640&h=291&f=png&s=15359)

|    属性名    |                           属性描述                           |
| :----------: | :----------------------------------------------------------: |
| Virtual host |                           虚拟主机                           |
|     Name     |       交换机名称，同一个Virtual host下不能有相同的Name       |
|     Type     |                          交换机类型                          |
|  Durability  |             是否持久化，Durable:是 Transient:否              |
| Auto delete  |           当最后一个绑定被删除后，该交换机将被删除           |
|   Internal   | 是否是内部专用exchange，是的话就意味着我们不能往exchange里面发送消息 |
|  Arguments   |           参数，是AMQP协议留给AMQP实现做扩展使用的           |

**RabbitMQ内置一个名称为空字符串的默认交换机，它根据Routing key将消息路由到与队列名与Routing key完全相等的队列中**

#### 2.2扇形交换机

![](https://user-gold-cdn.xitu.io/2018/9/16/165e29a642b3e95a?w=433&h=268&f=png&s=28862)

扇形交换机是最基本的交换机类型，它所能做的事情非常简单———广播消息。扇形交换机会把能接收到的消息`全部发送给绑定在自己身上的队列`,在路由转发的时候**忽略Routing Key**。因为广播不需要“思考”，所以扇形交换机处理消息的速度也是所有的交换机类型里面最快的。

`在发布消息时可以只先指定交换机的名称,交换机的声明的代码可以放到消费者端进行声明,队列的声明也放 在消费者端来声明`

### 3. 代码演示

> 实际情况一般是队列有可能对应多个消费者，比如一个注册事件，既要发送邮件，又要发送短信，所以先把消息发送到交换机，然后分发到两个队列， 一个是邮件队列，一个是短信队列， 服务器可能有多台，也即是每个队列可能有多个消费者，为了提高性能，也就是订阅模式与工作队列结合的一种形式。下面演示这个情况。

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

#### 3.2 用户注册事件

```java
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

```

#### 3.3 短信消费者

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
 * # 短信消费者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 21:02
 */
public class SMSReceive {

  // 短信队列
  private static final String QUEUE_NAME = "test_fanout_queue_sms";
  private static final String EXCHANGE_NAME = "test_exchange_fanout";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 获取信道
    Channel channel = connection.createChannel();
    // 申明要消费的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    // 绑定队列到交换机
    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

    // 限制消费的队列数 ,并设置自动应答为 false
    channel.basicQos(1);

    //创建一个回调的消费者处理类
    Consumer consumer = new DefaultConsumer(channel){
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
          throws IOException {
        super.handleDelivery(consumerTag, envelope, properties, body);
        // 接收到的消息
        String message = new String(body,"utf-8");
        System.out.println("SMSReceive " + message);

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }finally {
          System.out.println("SMSReceive done"  );
          // 关闭自动应答后, 在消费者处理消费数据之后一定要对消息进行手动反馈处理
          //可以是以下方式的一种
          channel.basicAck(envelope.getDeliveryTag(), false);
          // 拒绝当前消息,并使这条消息重新返回队列中
          //channel.basicNack(envelope.getDeliveryTag(), false, true);
          // 相当于
          //channel.basicReject(envelope.getDeliveryTag(), true);
        }
      }
    };
    channel.basicConsume(QUEUE_NAME, false, consumer);
  }
}

```

#### 3.4邮件消费者

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
 * # 邮件消费者
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/16 21:25
 */
public class MailReceive {

  // 邮件队列
  private static final String QUEUE_NAME = "test_fanout_queue_mail";
  private static final String EXCHANGE_NAME = "test_exchange_fanout";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 获取信道
    Channel channel = connection.createChannel();
    // 申明要消费的队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    // 绑定队列到交换机
    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

    // 限制消费的队列数 ,并设置自动应答为 false
    channel.basicQos(1);

    //创建一个回调的消费者处理类
    Consumer consumer = new DefaultConsumer(channel){
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
          throws IOException {
        super.handleDelivery(consumerTag, envelope, properties, body);
        // 接收到的消息
        String message = new String(body,"utf-8");
        System.out.println("MailReceive " + message);

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }finally {
          System.out.println("MailReceive done"  );
          // 关闭自动应答后, 在消费者处理消费数据之后一定要对消息进行手动反馈处理
          //可以是以下方式的一种
          channel.basicAck(envelope.getDeliveryTag(), false);
          // 拒绝当前消息,并使这条消息重新返回队列中
          //channel.basicNack(envelope.getDeliveryTag(), false, true);
          // 相当于
          //channel.basicReject(envelope.getDeliveryTag(), true);
        }
      }
    };
    channel.basicConsume(QUEUE_NAME, false, consumer);
  }
}

```

#### 3.5 测试

提前在管理控制台创建一个direct交换机，或者先执行一次生产者（执行时会判断交换机是否存在，不存在则创建交换机），这样保证交换机存在，不然直接启动消费者会提示交换机不存在。

**注意点**

> 如果在没有队列绑定在交换机上面时，往交换机发送消息会丢失，之后绑定在交换机上面的队列接收不到之前的消息，也就是先执行第一次发送，创建了交换机，但是还没有队列绑定在交换机上面，如果这次发送的消息就会丢失。

然后启动两个消费者，再执行生产者。

![](https://user-gold-cdn.xitu.io/2018/9/16/165e2af69a16ce25?w=726&h=309&f=png&s=49221)


![](https://user-gold-cdn.xitu.io/2018/9/16/165e2afea400c302?w=762&h=262&f=png&s=45127)

![](https://user-gold-cdn.xitu.io/2018/9/16/165e2afa44a29f06?w=736&h=261&f=png&s=43767)

#### 思考 

> ```java
> void basicPublish(String exchange, String routingKey, BasicProperties props, byte[] body) throws IOException;
>
> 在订阅模式下不需要routingKey, 可以写成""， 如果随意写个名字，在消费者也随便写一个，生产者和消费者的routingKey的不一样,看看是否能成功接收 
> ```
>
> 