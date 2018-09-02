# RabbitMQ消息确认机制之confirm串行

####  一 .Confirm模式	

生产者将信道设置成confirm模式,一旦信道进入confirm模式,所有在该信道上面发布的消息都会被指派一个唯一的ID(众1开始),一旦消息被投递到所有匹配的队列之后,broker就会发送一个确认给生产者(包括消息的唯一ID),这就使得生产者知道消息已经正确到达目的的队列了,如果消息和队列是可持久化的,那么确认消息会将消息写入磁盘之后发出,broker回传给生产者的确认消息中deliver-tag域包含了确认消息的序列号,此外broker也可以设置basic.ack的muitiple域,表示到这个序列号之前的所有消息都已经得到了处理.

confirm模式最大的好处在于他是异步的,一旦发布一条消息,生产者应用程序就可以在等信道返回确认的同时继续发送下一条消息,当消息最终得到确认之后,生产者应用便可以通过回调方法来处理该确认消息,如果RabbitMQ因为自身内部错误导致消息丢失,就会发送一条nack消息.生产者应用程序同样可以在回调方法中处理该nack消息.

在channel 被设置成confirm模式之后,所有被pubilsh的后续消息都将被confirm(即ack)或者被nack一次 . 但是没有对消息confirm的快慢做任何保证,并且同一条消息不会既被confirm又被nack.

> ```java
> 已经在transaction事务模式中的channel是不能再设置confirm模式的,即这两中模式是不能共存的
> ```



#### 二 . 编程模式

客户端实现生产者confirm有三种编程方式：

> ```java
> 普通confirm模式：每发送一条消息后，调用waitForConfirms()方法，等待服务器端confirm。实际上是一种串行confirm了。
> 批量confirm模式：每发送一批消息后，调用waitForConfirms()方法，等待服务器端confirm。
> 异步confirm模式：提供一个回调方法，服务端confirm了一条或者多条消息后Client端会回调这个方法
> ```

#### 三 . 代码演示

##### 1. 普通confirm模式

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

  private static final String QUEUE_NAME = "test_queue_confirm1";

  public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
    // 1. 创建连接
    Connection connection = ConnectionUtils.getConnection();
    // 2. 创建通道
    Channel channel = connection.createChannel();
    // 3. 申明一个一个队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // 开启confirm模式
    channel.confirmSelect();
    // 发给info路由键消息
    String message = "Hello ,confirm message";
    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
    if (channel.waitForConfirms()) {
      System.out.println(" [ x ] Send  message '" + message + "' ok");
    } else {
      System.out.println(" [ x ] Send  message '" + message + "' fail");
    }

    // 关闭连接与通道
    channel.close();
    connection.close();

  }
}

```

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
public class ConfirmRecv {

  // 短信队列
  private static final String QUEUE_NAME = "test_queue_confirm1";

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

##### 2. 批量confirm模式

```java
package com.makesailing.neo.provider;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ConfirmSend2 {

    private static final String QUEUE_NAME = "test_queue_confirm1";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();
        // 从连接开一个通道
        Channel channel = connection.createChannel();
        // 声明一个队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        channel.confirmSelect(); // 开始confirm模式

        for (int i = 0; i < 20; i++) {
            // 发送消息
            String message = "hello, confirm message " + i;
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        }

        if(channel.waitForConfirms()){
            System.out.println(" [x] Sent message ok ");
        } else {
            System.out.println(" [x] Sent message fail ");
        }

        channel.close();
        connection.close();
    }

}
```

##### 3. 异步confirm模式

```java
package com.makesailing.neo.provider;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

public class ConfirmSend3 {

    private static final String QUEUE_NAME = "test_queue_confirm1";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();
        // 从连接开一个通道
        Channel channel = connection.createChannel();
        // 声明一个队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        channel.confirmSelect(); // 开始confirm模式

        // 未确认的消息列表
        SortedSet<Long> confirmSet = Collections.synchronizedSortedSet(new TreeSet<Long>());

        channel.addConfirmListener(new ConfirmListener() {

            // 有效的应答
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                if (multiple) {
                    confirmSet.headSet(deliveryTag + 1).clear();
                } else {
                    confirmSet.remove(deliveryTag);
                }
            }
            // 有问题的应答
            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("Nack, SeqNo: " + deliveryTag + ", multiple: " + multiple);
                // TODO 处理未确认的应答
                if (multiple) {
                    confirmSet.headSet(deliveryTag + 1).clear();
                } else {
                    confirmSet.remove(deliveryTag);
                }
            }
        });

        while (true) {
            long nextSeqNo = channel.getNextPublishSeqNo();
            String message = "hello, confirm message ";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            confirmSet.add(nextSeqNo);
        }

    }

}
```

异步confirm模式处理起来比较复杂。