### 1. 主题模式

> 跟路由模式类似,只不过路由械是指定固定的路由键 , 而主题模式是可以模糊匹配路由键 ,类似于 `SQL`中 `=` 和`like`的关系

![](https://user-gold-cdn.xitu.io/2018/9/18/165ebc722e57f71e?w=439&h=195&f=png&s=14946)

### 2. topic 交换机

> 直连接类型direct必须是生产者发布消息指定的`routingKey`和消费者在队列绑定时指定的`routingKey`完全相等时才能匹配到队列上，与direct不同,topic可以进行模糊匹配，可以使用星号`*`和井号`#`这两个通配符来进行模糊匹配，其中星号可以代替一个单词；主题类型的转发器的消息不能随意的设置选择键（`routing_key`），必须是由点隔开的一系列的标识符组成。标识符可以是任何东西，但是一般都与消息的某些特性相关。一些合法的选择键的例子：”quick.orange.rabbit”,你可以定义任何数量的标识符，上限为255个字节。 `#`井号可以替代零个或更多的单词，只要能模糊匹配上就能将消息映射到队列中。当一个队列的绑定键为#的时候，这个队列将会无视消息的路由键，接收所有的消息.

举例：如上图的主题模式中， Q1绑定*.orange.*路由键，Q2绑定两个路由键，分别是*.*.rabbit以及lazy.#

> 1. 如果生产者发送路由键为quick.orange.rabbit消息，C1和C2都可以接收到。
> 2. 如果为lazy.orange.elephant， C1和C2都可以接收到
> 3. 如果为quick.orange.fox， 只要C1可以接收到
> 4. 如果为lazy.brown.fox， 只有C2可以接收到
> 5. 如果为lazy.pink.rabbit， C1，C2都可以
> 6. 如果为quick.brown.fox， 都不会接收到

### 3. 代码演示

> 只是路由键可以模糊匹配了，先设定为生产者发送quick.orange.rabbit，消费者1绑定到*.orange.*，消费者2绑定到*.*.rabbit以及lazy.#

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

#### 3.2 生产者 

```java
package com.makesailing.neo.provider;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * # 生产者
 *
 * @author jamie.li
 * @date 2018/9/18 11:36
 */
public class Send {

	private static final String EXCHANGE_NAME = "test_exchange_topic";
	private static final String ROUTING_KEY = "quick.orange.rabbit";

	public static void main(String[] args) throws IOException, TimeoutException {
		// 创建连接
		Connection connection = ConnectionUtils.getConnection();
		// 获取信道
		Channel channel = connection.createChannel();

		// 声明一个 topic 交换机
		channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

		// 发送消息
		String message = "Hello , quick.orange.rabbit";

		channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, message.getBytes());

		System.out.println("[ Send ] " + message);

		// 关闭连接
		channel.close();
		connection.close();
	}
}


```

#### 3.3 消费者 1

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
 * # 消费者1
 *
 * @author jamie.li
 * @date 2018/9/18 14:51
 */
public class Receive1 {

	private static final String EXCHANGE_NAME = "test_exchange_topic";
	private static final String ROUTING_KEY = "*.orange.*";
	private static final String QUEUE_NAME = "test_queue_topic_1";

	public static void main(String[] args) throws IOException, TimeoutException {
		// 创建连接
		Connection connection = ConnectionUtils.getConnection();
		// 获取信道
		final Channel channel = connection.createChannel();

		//声明要消费的队列
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		// 队列绑定交换机
		channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY, null);

		// 设置每次消费1条消息,消费完后再取队列中获取消息,需要手动应答
		channel.basicQos(1);

		Consumer consumer = new DefaultConsumer(channel){
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
				throws IOException {
				super.handleDelivery(consumerTag, envelope, properties, body);
				String message = new String(body, "UTF-8");
				System.out.println(" Receive1 : ' " + message + " ', 处理业务中...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					System.out.println("Receive1 Done");
					channel.basicAck(envelope.getDeliveryTag(),false);
				}
			}
		};

		// 手动应答
		channel.basicConsume(QUEUE_NAME, false, consumer);
	}
}


```

#### 3.4 消费者 2

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
 * # 消费者2
 *
 * @author jamie.li
 * @date 2018/9/18 15:04
 */
public class Receive2 {

	private static final String EXCHANGE_NAME = "test_exchange_topic";
	private static final String ROUTING_KEY_ONE = "*.*.rabbit";
	private static final String ROUTING_KEY_TWO = "lazy.#";
	private static final String QUEUE_NAME = "test_queue_topic_2";

	public static void main(String[] args) throws IOException, TimeoutException {
		// 创建连接
		Connection connection = ConnectionUtils.getConnection();
		// 获取信道
		final Channel channel = connection.createChannel();

		//声明要消费的队列
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		// 队列绑定交换机
		channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY_ONE, null);
		channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY_TWO, null);

		// 设置每次消费1条消息,消费完后再取队列中获取消息,需要手动应答
		channel.basicQos(1);

		Consumer consumer = new DefaultConsumer(channel){
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
				throws IOException {
				super.handleDelivery(consumerTag, envelope, properties, body);
				String message = new String(body, "UTF-8");
				System.out.println(" Receive2 : ' " + message + " ', 处理业务中...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					System.out.println("Receive2 Done");
					channel.basicAck(envelope.getDeliveryTag(),false);
				}
			}
		};

		// 手动应答
		channel.basicConsume(QUEUE_NAME, false, consumer);
	}
}


```

### 4 测试结果

提前在管理控制台创建一个topic交换机，或者先执行一次生产者（执行时会判断交换机是否存在，不存在则创建交换机），这样保证交换机存在，不然直接启动消费者会提示交换机不存在 .

![](https://user-gold-cdn.xitu.io/2018/9/18/165ebd1a1df34926?w=807&h=646&f=png&s=61911)

-  注意点

  > 如果在没有队列绑定在交换机上面时，往交换机发送消息会丢失，之后绑定在交换机上面的队列接收不到之前的消息，也就是先执行第一次发送，创建了交换机，但是还没有队列绑定在交换机上面，如果这次发送的消息就会丢失。

然后启动两个消费者，再执行生产者。

![](https://user-gold-cdn.xitu.io/2018/9/18/165ebd331b088ce2?w=775&h=300&f=png&s=50963)


![](https://user-gold-cdn.xitu.io/2018/9/18/165ebd3941ce6441?w=750&h=260&f=png&s=45273)


![](https://user-gold-cdn.xitu.io/2018/9/18/165ebd3603d21e09?w=776&h=244&f=png&s=44686)

- 最后以一张图总结 :

  ![](https://user-gold-cdn.xitu.io/2018/9/18/165ebd55d9d48953?w=567&h=298&f=png&s=59384)









