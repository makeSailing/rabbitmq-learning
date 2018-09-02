####  工作队列

[官方文档]: http://www.rabbitmq.com/tutorials/tutorial-two-java.html

简单队列不足 : 不支持多个消费者

即一个生产者可以对应多个消费者同时消费,相比简单队列支持多消费者 . 因为实际工作中,生产者服务一般都是很简单的业务逻辑处理之后就发送队列,消费者接收到队列的消息之后,进行复杂的逻辑处理,所以一般都是多个消费者进行处理 . 如是是一个消费者进行处理,那么队列就会积压消息.

![](https://user-gold-cdn.xitu.io/2018/9/2/16597d5f8e361da6?w=314&h=135&f=png&s=9802)

工作队列分两种情况 : 

- 1. 轮询分发

  > 不管消费都处理速度性能快慢,每个消费者都是按顺序分别每个拿一个的原则.比如3个消费者,消费者1拿1个,然后消费者2拿1个,然后消费者3拿1个.然后消费者1开始拿. 即使中间有消费者已经处理完了,也必须等待其他消费者都拿完了一个才能消费到

- 2. 公平分发

  > 根据消费者的处理性能,性能好的消费的数据量多,性能差的消费的数据量少 .

#### 1. 轮询分发 

> 一个生产者,两个消费者. 其中消费者1处理只要1s,消费者2处理需要2s.

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
  package com.makesailing.neo.polling.distribution.provider;

  import com.makesailing.neo.utils.ConnectionUtils;
  import com.rabbitmq.client.Channel;
  import com.rabbitmq.client.Connection;
  import java.io.IOException;
  import java.util.concurrent.TimeoutException;

  /**
   * #
   *
   * @Author: jamie.li
   * @Date: Created in  2018/9/1 22:42
   */
  public class Send {

    private static final String QUEUE_NAME = "test_work_poling_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
      // 1. 获取连接
      Connection connection = ConnectionUtils.getConnection();
      // 2. 创建通道
      Channel channel = connection.createChannel();
      // 3.申明这个通道的队列
      /**
       * queue: 队列名称
       * durable: 是否持久化，true表示RabbitMQ重启后，队列仍然存在
       * exclusive: true表示当前连接的专用队列，在连接断开后，会自动删除该队列
       * autoDelete: true 表示当没有任何消费者使用时，自动删除该队列
       * arguments: 该队列其他配置参数
       */
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);

      for (int i = 0; i < 20; i++) {
        String message = "Hello RabbitMQ " + i;
        /** 4. 发送消息
         * exchange: 指定交换机，这里为工作队列，不需要使用，默认""即可，不能为null
         * routingKey: 路由key，这里为队列名称
         * props: 其他消息属性，路由头信息等等
         * body: 消息byte内容
         */
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Send '" + message + "'");

      }
      // 5. 关闭通道和连接
      channel.close();
      connection.close();
    }
  }
  ```

- 消费者1 

  ```java
  package com.makesailing.neo.polling.distribution.consumer;

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
   * @Author: jamie.li
   * @Date: Created in  2018/9/1 22:51
   */
  public class Recv {

    private static final String QUEUE_NAME = "test_work_poling_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
      // 1.获取连接
      Connection connection = ConnectionUtils.getConnection();
      // 2.创建通道
      Channel channel = connection.createChannel();

      // 3.申明要消息的队列
      channel.queueDeclare( QUEUE_NAME, false, false, false, null);
      // 4.创建一个回调的消费者处理类
      Consumer consumer = new DefaultConsumer(channel){
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
            throws IOException {
          // 接收到的消息
          String message = new String(body);
          System.out.println(" [1] Received '" + message + "'");

          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          } finally {
            System.out.println(" [1] done ");
          }

        }
      };

      // 消费消息
      /**
       * queue 队列名称
       * autoAck : 是否自动应答,即生产者发送消息即认为该消息被消费
       * callback: 回调处理类,即消息被消费时被回调处理
       */
      channel.basicConsume(QUEUE_NAME, true, consumer);
    }
  }
  ```

- 消费者2

  ```java
  package com.makesailing.neo.polling.distribution.consumer;

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
   * @Author: jamie.li
   * @Date: Created in  2018/9/1 23:02
   */
  public class Recv2 {

    private static final String QUEUE_NAME = "test_work_poling_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
      // 1.创建连接
      Connection connection = ConnectionUtils.getConnection();
      // 2.创建通道
      Channel channel = connection.createChannel();
      // 3.申明要消息的队列
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
      // 4.创建一个回调消息的处理类
      Consumer consumer = new DefaultConsumer(channel){
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
            throws IOException {
          String message = new String(body);
          System.out.println(" [2] Recevied '" + message + "'");
          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          } finally {
            System.out.println(" [2] done ");
          }

        }
      };

      // 消费消息
      channel.basicConsume(QUEUE_NAME, true, consumer);
    }
  }
  ```

- 测试 : 

  1. 先启动两个消费者 Recv ,Recv2
  2. 再启动生产者 Send

- 生产者控制台 : 

  ```java
  Connected to the target VM, address: 'javadebug', transport: 'shared memory'
   [x] Send 'Hello RabbitMQ 0'
   [x] Send 'Hello RabbitMQ 1'
   [x] Send 'Hello RabbitMQ 2'
   [x] Send 'Hello RabbitMQ 3'
   [x] Send 'Hello RabbitMQ 4'
   [x] Send 'Hello RabbitMQ 5'
   [x] Send 'Hello RabbitMQ 6'
   [x] Send 'Hello RabbitMQ 7'
   [x] Send 'Hello RabbitMQ 8'
   [x] Send 'Hello RabbitMQ 9'
   [x] Send 'Hello RabbitMQ 10'
   [x] Send 'Hello RabbitMQ 11'
   [x] Send 'Hello RabbitMQ 12'
   [x] Send 'Hello RabbitMQ 13'
   [x] Send 'Hello RabbitMQ 14'
   [x] Send 'Hello RabbitMQ 15'
   [x] Send 'Hello RabbitMQ 16'
   [x] Send 'Hello RabbitMQ 17'
   [x] Send 'Hello RabbitMQ 18'
   [x] Send 'Hello RabbitMQ 19'
  Disconnected from the target VM, address: 'javadebug', transport: 'shared memory'

  Process finished with exit code 0
  ```

- 消费者1 控制台 : 

  ```java
   [1] Received 'Hello RabbitMQ 0'
   [1] done 
   [1] Received 'Hello RabbitMQ 2'
   [1] done 
   [1] Received 'Hello RabbitMQ 4'
   [1] done 
   [1] Received 'Hello RabbitMQ 6'
   [1] done 
   [1] Received 'Hello RabbitMQ 8'
   [1] done 
   [1] Received 'Hello RabbitMQ 10'
   [1] done 
   [1] Received 'Hello RabbitMQ 12'
   [1] done 
   [1] Received 'Hello RabbitMQ 14'
   [1] done 
   [1] Received 'Hello RabbitMQ 16'
   [1] done 
   [1] Received 'Hello RabbitMQ 18'
   [1] done 

  ```

- 消费者2 控制台:

  ```java
  "C:\Program Files\Java\jdk1.8.0_102\bin\java.exe" -agentlib:jdwp=transport=dt_shmem,address=javadebug,suspend=y,server=n -javaagent:C:\Users\Administrator\AppData\Local\Temp\capture167jars\debugger-agent.jar=file:/C:/Users/Administrator/AppData/Local/Temp/capture.props -Dfile.encoding=UTF-8 -classpath "C:\Program Files\Java\jdk1.8.0_102\jre\lib\charsets.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\deploy.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\ext\access-bridge-64.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\ext\cldrdata.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\ext\dnsns.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\ext\jaccess.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\ext\jfxrt.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\ext\localedata.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\ext\nashorn.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\ext\sunec.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\ext\sunjce_provider.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\ext\sunmscapi.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\ext\sunpkcs11.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\ext\zipfs.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\javaws.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\jce.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\jfr.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\jfxswt.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\jsse.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\management-agent.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\plugin.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\resources.jar;C:\Program Files\Java\jdk1.8.0_102\jre\lib\rt.jar;C:\java_work\jamie.li_review\rabbitmq-learning\rabbitmq-work-queue\target\classes;E:\maven\repository\org\springframework\boot\spring-boot-starter-web\1.5.14.RELEASE\spring-boot-starter-web-1.5.14.RELEASE.jar;E:\maven\repository\org\springframework\boot\spring-boot-starter\1.5.14.RELEASE\spring-boot-starter-1.5.14.RELEASE.jar;E:\maven\repository\org\springframework\boot\spring-boot\1.5.14.RELEASE\spring-boot-1.5.14.RELEASE.jar;E:\maven\repository\org\springframework\boot\spring-boot-autoconfigure\1.5.14.RELEASE\spring-boot-autoconfigure-1.5.14.RELEASE.jar;E:\maven\repository\org\springframework\boot\spring-boot-starter-logging\1.5.14.RELEASE\spring-boot-starter-logging-1.5.14.RELEASE.jar;E:\maven\repository\ch\qos\logback\logback-classic\1.1.11\logback-classic-1.1.11.jar;E:\maven\repository\ch\qos\logback\logback-core\1.1.11\logback-core-1.1.11.jar;E:\maven\repository\org\slf4j\jcl-over-slf4j\1.7.25\jcl-over-slf4j-1.7.25.jar;E:\maven\repository\org\slf4j\jul-to-slf4j\1.7.25\jul-to-slf4j-1.7.25.jar;E:\maven\repository\org\slf4j\log4j-over-slf4j\1.7.25\log4j-over-slf4j-1.7.25.jar;E:\maven\repository\org\yaml\snakeyaml\1.17\snakeyaml-1.17.jar;E:\maven\repository\org\springframework\boot\spring-boot-starter-tomcat\1.5.14.RELEASE\spring-boot-starter-tomcat-1.5.14.RELEASE.jar;E:\maven\repository\org\apache\tomcat\embed\tomcat-embed-core\8.5.31\tomcat-embed-core-8.5.31.jar;E:\maven\repository\org\apache\tomcat\tomcat-annotations-api\8.5.31\tomcat-annotations-api-8.5.31.jar;E:\maven\repository\org\apache\tomcat\embed\tomcat-embed-el\8.5.31\tomcat-embed-el-8.5.31.jar;E:\maven\repository\org\apache\tomcat\embed\tomcat-embed-websocket\8.5.31\tomcat-embed-websocket-8.5.31.jar;E:\maven\repository\org\hibernate\hibernate-validator\5.3.6.Final\hibernate-validator-5.3.6.Final.jar;E:\maven\repository\javax\validation\validation-api\1.1.0.Final\validation-api-1.1.0.Final.jar;E:\maven\repository\org\jboss\logging\jboss-logging\3.3.2.Final\jboss-logging-3.3.2.Final.jar;E:\maven\repository\com\fasterxml\classmate\1.3.4\classmate-1.3.4.jar;E:\maven\repository\com\fasterxml\jackson\core\jackson-databind\2.8.11.2\jackson-databind-2.8.11.2.jar;E:\maven\repository\com\fasterxml\jackson\core\jackson-annotations\2.8.0\jackson-annotations-2.8.0.jar;E:\maven\repository\com\fasterxml\jackson\core\jackson-core\2.8.11\jackson-core-2.8.11.jar;E:\maven\repository\org\springframework\spring-web\4.3.18.RELEASE\spring-web-4.3.18.RELEASE.jar;E:\maven\repository\org\springframework\spring-aop\4.3.18.RELEASE\spring-aop-4.3.18.RELEASE.jar;E:\maven\repository\org\springframework\spring-beans\4.3.18.RELEASE\spring-beans-4.3.18.RELEASE.jar;E:\maven\repository\org\springframework\spring-context\4.3.18.RELEASE\spring-context-4.3.18.RELEASE.jar;E:\maven\repository\org\springframework\spring-webmvc\4.3.18.RELEASE\spring-webmvc-4.3.18.RELEASE.jar;E:\maven\repository\org\springframework\spring-expression\4.3.18.RELEASE\spring-expression-4.3.18.RELEASE.jar;E:\maven\repository\org\springframework\spring-core\4.3.18.RELEASE\spring-core-4.3.18.RELEASE.jar;E:\maven\repository\com\rabbitmq\amqp-client\5.0.0\amqp-client-5.0.0.jar;E:\maven\repository\org\slf4j\slf4j-api\1.7.25\slf4j-api-1.7.25.jar;C:\Program Files\JetBrains\IntelliJ IDEA 2018.1.5\lib\idea_rt.jar" com.makesailing.neo.polling.distribution.consumer.Recv2
  Connected to the target VM, address: 'javadebug', transport: 'shared memory'
   [2] Recevied 'Hello RabbitMQ 1'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 3'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 5'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 7'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 9'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 11'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 13'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 15'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 17'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 19'
   [2] done 

  ```

> 可以发现 : 消费者1 全都是奇数, 消费者2都是偶数

- 那么如是我事先启动三个消费者,那么结果如何能 ? 请思考 ?

  > 因为20/3 除不了，如果消费者1,2,3按顺序启动，那么消费者1, 2会消费7条数据，消费者3消费6条，其中
  >
  > 消费者1 消费 0, 3, 6, 9, 12, 15, 18
  > 消费者2 消费 1, 4, 7, 10, 13, 16, 19
  > 消费者3 消费 2, 5, 8, 11, 14, 17

- 请注意 : 

  > 如果生产者一次性发送完消息之后，再依次启动消费者1, 2, 3， 之后只有消费者1 能消费到数据，消费者都启动之后，再生产的消息就会轮询分发到消费者1, 2, 3

#### 2. 公平分发

官方示例图 : 

![](https://user-gold-cdn.xitu.io/2018/9/2/16597ec5e8600abe?w=377&h=150&f=png&s=13399)

因为生产者发送消息到队列之后,队列不知道消费者有没有处理完,所以多个消费者同时订阅同一个Queue中的消息,Queue的消息会被平分给多个消费者 . 为了实现公平分发,我们需要告诉队列,每次发一个给我,然后我再反馈给你我有没有处理完,处理完了你再发一条给我.

在默认轮询分发的基础上,要实现公平分发,需要两点 : 

- 1. 限制发送给同一个消费者不得超过1条消息,在这个消费者确认消息之前,不会发送下一条消息给这个消费者

     ```java
     int prefetchCount = 1;
     channel.basicQos(prefetchCount);
     ```

- 2. 默认自动应答改成手动应答

     ```java
     关闭自动应答
     boolean autoAck = false;
     channel.basicConsume(QUEUE_NAME, autoAck, consumer);

     手动应答
     channel.basicAck(envelope.getDeliveryTag(), false);

     DeliveryTag 用来标识信道中投递的消息， RabbitMQ 推送消息给 Consumer 时，会附带一个 Delivery Tag，以便 Consumer 可以在消息确认时告诉 RabbitMQ 到底是哪条消息被确认了。
     ```

- 生产者

  ```java
  package com.makesailing.neo.fair.distribution.provider;

  import com.makesailing.neo.utils.ConnectionUtils;
  import com.rabbitmq.client.Channel;
  import com.rabbitmq.client.Connection;
  import java.io.IOException;
  import java.util.concurrent.TimeoutException;

  /**
   * # 公平发送
   *
   * @Author: jamie.li
   * @Date: Created in  2018/9/2 8:51
   */
  public class Send {
    private static final String QUEUE_NAME = "test_work_fair_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
      // 1. 获取连接
      Connection connection = ConnectionUtils.getConnection();
      // 2. 创建通道
      Channel channel = connection.createChannel();
      // 3.申明这个通道的队列
      /**
       * queue: 队列名称
       * durable: 是否持久化，true表示RabbitMQ重启后，队列仍然存在
       * exclusive: true表示当前连接的专用队列，在连接断开后，会自动删除该队列
       * autoDelete: true 表示当没有任何消费者使用时，自动删除该队列
       * arguments: 该队列其他配置参数
       */
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);

      for (int i = 0; i < 20; i++) {
        String message = "Hello RabbitMQ " + i;
        /** 4. 发送消息
         * exchange: 指定交换机，这里为工作队列，不需要使用，默认""即可，不能为null
         * routingKey: 路由key，这里为队列名称
         * props: 其他消息属性，路由头信息等等
         * body: 消息byte内容
         */
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Send '" + message + "'");

        try {
          Thread.sleep(i * 100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

      }
      // 5. 关闭通道和连接
      channel.close();
      connection.close();
    }
  }

  ```

- 消费者1 

  ```java
  package com.makesailing.neo.fair.distribution.consumer;

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
   * # 消费者1 公平分发
   *
   * @Author: jamie.li
   * @Date: Created in  2018/9/1 22:51
   */
  public class Recv {

    private static final String QUEUE_NAME = "test_work_fair_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
      // 1.获取连接
      Connection connection = ConnectionUtils.getConnection();
      // 2.创建通道
      Channel channel = connection.createChannel();

      // 3.申明要消息的队列
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);

      // 公平分发确定发给消费者只有1 个
      // 这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。
      // 换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它。
      int prefetchCount = 1;
      channel.basicQos(prefetchCount);

      // 4.创建一个回调的消费者处理类
      Consumer consumer = new DefaultConsumer(channel){
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
            throws IOException {
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
      /**
       * queue 队列名称
       * autoAck : 是否自动应答,即生产者发送消息即认为该消息被消费
       * callback: 回调处理类,即消息被消费时被回调处理
       */
      channel.basicConsume(QUEUE_NAME, false, consumer);
    }
  }

  ```

- 消费者2 

  ```java
  package com.makesailing.neo.fair.distribution.consumer;

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
   * @Author: jamie.li
   * @Date: Created in  2018/9/1 23:02
   */
  public class Recv2 {

    private static final String QUEUE_NAME = "test_work_fair_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
      // 1.创建连接
      Connection connection = ConnectionUtils.getConnection();
      // 2.创建通道
      Channel channel = connection.createChannel();
      // 3.申明要消息的队列
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);

      // 公平分发确定发给消费者只有1 个
      // 这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。
      // 换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它。
      int prefetchCount = 1;
      channel.basicQos(prefetchCount);
      // 4.创建一个回调消息的处理类
      Consumer consumer = new DefaultConsumer(channel){
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
            throws IOException {
          String message = new String(body);
          System.out.println(" [2] Recevied '" + message + "'");
          try {
            Thread.sleep(2000);
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

- 测试 :  先启动消费者1,消费者2 ,再启动发送者

  1. 消费者1 :

  ``` java
   [1] Received 'Hello RabbitMQ 0'
   [1] done 
   [1] Received 'Hello RabbitMQ 2'
   [1] done 
   [1] Received 'Hello RabbitMQ 4'
   [1] done 
   [1] Received 'Hello RabbitMQ 5'
   [1] done 
   [1] Received 'Hello RabbitMQ 7'
   [1] done 
   [1] Received 'Hello RabbitMQ 8'
   [1] done 
   [1] Received 'Hello RabbitMQ 10'
   [1] done 
   [1] Received 'Hello RabbitMQ 11'
   [1] done 
   [1] Received 'Hello RabbitMQ 13'
   [1] done 
   [1] Received 'Hello RabbitMQ 14'
   [1] done 
   [1] Received 'Hello RabbitMQ 15'
   [1] done 
   [1] Received 'Hello RabbitMQ 17'
   [1] done 
   [1] Received 'Hello RabbitMQ 19'
   [1] done 
  ```

  消费者2 

  ```java
  [2] Recevied 'Hello RabbitMQ 1'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 3'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 6'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 9'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 12'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 16'
   [2] done 
   [2] Recevied 'Hello RabbitMQ 18'
   [2] done 
  ```

- > 显然消费者1处理速度只要1s,所以消费的记录数要比消费者2要多很多 . 表示确实是公平分发

- 注意点 : 

  ```java
  当关闭自动应答autoAck=false之后，在消费者处理消费数据之后一定要对消息进行手动反馈处理，可以是basicAck，也可以是basicNack， basicReject


  BasicReject一次只能拒绝接收一个消息，而BasicNack方法可以支持一次0个或多个消息的拒收，并且也可以设置是否requeue。


  // 拒绝当前消息，并使这条消息重新返回到队列中
  channel.basicNack(envelope.getDeliveryTag(), false, true);
  相当于
  channel.basicReject(envelope.getDeliveryTag(), true);
  ```

  ​

  ​

























































