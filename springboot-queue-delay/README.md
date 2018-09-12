### SpringBoot & RabbitMQ完成消息延迟消费

#### 目标

- 基于 `SpringBoot` 整合`RabbitMQ`完成消息延迟消费

> 由于`SpringBoot`的内置扫描机制,我们如果不自动配置扫描路径,请保持下面`rabbitmq-commom`模块内的配置可以被`SpringBoot`扫描到,否则不会自动创建队列,控制台会输出404的错误信息

- 项目依赖 `pom.xml`如下 : 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.makesailing.neo</groupId>
	<artifactId>springboot-queue-delay</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<modules>
		<module>rabbitmq-common</module>
		<module>rabbitmq-lazy-provider</module>
		<module>rabbitmq-lazy-consumer</module>
	</modules>
	<packaging>pom</packaging>

	<name>springboot-queue-delay</name>
	<description> Spring Boot Rabbitmq 延迟队列</description>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>1.5.14.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-amqp</artifactId>
		</dependency>
		<dependency>
			<groupId>com.alibaba</groupId>
			<artifactId>fastjson</artifactId>
			<version>1.2.40</version>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>


</project>

```

#### 创建队列公共模块

- 我们先来创建一个名为`rabbitmq-common`的公共依赖模块,在公共模块内添加一个`QueueEnum`队列枚举配置,该枚举内配置队列的的`Exchange`、`QueueName`、`routingKey` 等相关内容,如下所示:

```java
package com.makesailing.neo.enums;

/**
 * #
 *
 * @author jamie
 * @date 2018/9/12 13:50
 */
public enum QueueEnum {
	/**
	 * 消息通知队列
	 */
	MESSAGE_QUEUE("message.center.direct", "message.center.create", "message.center.create"),

	/**
	 * 消息通知 TTL 队列
	 */
	MESSAGE_TTL_QUEUE("message.center.topic.ttl","message.center.create.ttl","message.center.create.ttl")
	;

	/**
	 * 交换名称
	 */
	private String exchange;

	/**
	 * 队列名称
	 */
	private String name;
	/**
	 * 路由键
	 */
	private String routingKey;

	QueueEnum(String exchange, String name, String routingKey) {
		this.exchange = exchange;
		this.name = name;
		this.routingKey = routingKey;
	}

	public String getExchange() {
		return exchange;
	}

	public String getName() {
		return name;
	}

	public String getRoutingKey() {
		return routingKey;
	}
}

```

可以看到`MESSAG_QUEUE`队列配置跟我们之前章节的配置一样,而我们另外创建了一个后辍为`ttl`的消息队列配置. 我们采用的这种方式是`RabbitMQ`消息队列其中一种延迟消息模块,通过配置队列消息过期后转发的形式.

> 这种模式比较简单,我们需要将消息先发送到`ttl`延迟队列内,当消息到达过期时间后会自动转发到`ttl`队列内配置的转发Exchange以及RoutingKey绑定的队列内完成消息消费

下面我们来模拟`消息通知`的延迟消费场景,先来创建一个名为`MessageRabbitMqConfiguration`的队列配置类,该配置类内添加`消息通知队列`配置以及`消息通过延迟队列`配置,如下所示:

```java
package com.makesailing.neo.config;

import com.makesailing.neo.enums.QueueEnum;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * #
 *
 * @author jamie
 * @date 2018/9/12 14:00
 */
@Configuration
public class MessageRabbitMqConfiguration {

	/**
	 * 消息中心实际消费队列交换配置
	 * @return
	 */
	@Bean
	public DirectExchange messageDirect() {
		DirectExchange directExchange = (DirectExchange) ExchangeBuilder
			.directExchange(QueueEnum.MESSAGE_QUEUE.getExchange()).durable(true).build();
		return directExchange;
	}

	/**
	 * 消息中心延迟消息队列配置
	 * @return
	 */
	@Bean
	public DirectExchange messageTtlDirect() {
		DirectExchange directExchange = (DirectExchange) ExchangeBuilder
			.directExchange(QueueEnum.MESSAGE_TTL_QUEUE.getExchange()).durable(true).build();
		return directExchange;
	}

	/**
	 * 消息中心实际消费队列
	 * @return
	 */
	@Bean
	public Queue messageQueue() {
		Queue queue = QueueBuilder.durable(QueueEnum.MESSAGE_QUEUE.getName()).build();
		return queue;
	}

	/**
	 * 消息中心 Ttl 队列
	 * @return
	 */
	@Bean
	public Queue messageTtlQueue() {
		Queue queue = QueueBuilder.durable(QueueEnum.MESSAGE_TTL_QUEUE.getName())
			.withArgument("x-dead-letter-exchange", QueueEnum.MESSAGE_QUEUE.getExchange())
			.withArgument("x-dead-letter-routing-key",QueueEnum.MESSAGE_QUEUE.getRoutingKey()).build();
		return queue;
	}

	/**
	 * 消息中心实际消息交换与队列绑定
	 * @param messageDirect
	 * @param messageQueue
	 * @return
	 */
	@Bean
	public Binding messageBinding(DirectExchange messageDirect, Queue messageQueue) {
		Binding binding = BindingBuilder.bind(messageQueue).to(messageDirect)
			.with(QueueEnum.MESSAGE_QUEUE.getRoutingKey());
		return binding;
	}

	/**
	 * 消息中心TTL绑定实际消息中心实际消费交换机
	 * @param messageTtlDirect
	 * @param messageTtlQueue
	 * @return
	 */
	@Bean
	public Binding messageTtlBinding(DirectExchange messageTtlDirect, Queue messageTtlQueue) {
		Binding binding = BindingBuilder.bind(messageTtlQueue).to(messageTtlDirect)
			.with(QueueEnum.MESSAGE_TTL_QUEUE.getRoutingKey());
		return binding;
	}

}

```

我们声明了`消息通知队列`的相关`Exchange`、`Queue`、`Binding`等配置,将`message.center.create`队列通过路由键`message.center.create`绑定到了`message.center.direct`交换机上 .

除此之外,我们还添加了`消息通知延迟队列`的`Exchange`、`Queue`、`Binding` 等配置,将`message.center.create.ttl` 队列通知`message.center.create.ttl`路由键绑定到了`message.center.topic.ttl`交换机上 .

我们仔细来看看`messageTtlQueue`延迟队列的配置，跟`messageQueue`队列配置不同的地方这里多出了`x-dead-letter-exchange`、`x-dead-letter-routing-key`两个参数，而这两个参数就是配置延迟队列过期后转发的`Exchange`、`RouteKey`，只要在创建队列时对应添加了这两个参数，在`RabbitMQ`管理平台看到的队列配置就不仅是单纯的`Direct`类型的队列类型，如下图所示：

![](https://user-gold-cdn.xitu.io/2018/9/12/165ce65ca08c6e7f?w=890&h=191&f=png&s=28001)

在上图内我们可以看到`message.center.create.ttl`队列多出了`DLX`、`DLK`的配置，这就是`RabbitMQ`内`死信交换`的标志。
满足`死信交换`的条件，在官方文档中表示：

Messages from a queue can be 'dead-lettered'; that is, republished to another exchange when any of the following events occur:

The message is rejected (basic.reject or basic.nack) with requeue=false,
The TTL for the message expires; or
The queue length limit is exceeded.

- 该消息被拒绝（basic.reject或  basic.nack），requeue = false
- 消息的TTL过期
- 队列长度限制已超出
  [官方文档地址](https://www.rabbitmq.com/dlx.html)

我们需要满足上面的其中一种方式就可以了，我们采用满足第二个条件，采用过期的方式。

#### 创建队列消息提供者

我们再来创建一个名为`rabbitmq-lazy-provider`的模块(Create New Maven Module)，并且在`pom.xml`配置文件内添加`rabbitmq-common`模块的依赖，如下所示：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <parent>
    <artifactId>springboot-queue-delay</artifactId>
    <groupId>com.makesailing.neo</groupId>
    <version>0.0.1-SNAPSHOT</version>
  </parent>
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.makesailing.neo</groupId>
  <artifactId>rabbitmq-lazy-provider</artifactId>
  <version>0.0.1-SNAPSHOT</version>

  <dependencies>
    <dependency>
      <groupId>com.makesailing.neo</groupId>
      <artifactId>rabbitmq-common</artifactId>
      <version>0.0.1-SNAPSHOT</version>
    </dependency>
  </dependencies>

</project>
```

- 配置队列

  在`resource`下创建一个名为`application.yml`的配置文件，在该配置文件内添加如下配置信息：

  ```yaml
  spring:
    rabbitmq:
      host: 127.0.0.1
      username: jamie
      password: 123456
      virtual-host: /simple
      port: 5672
      publisher-confirms: true
  ```

- 消息提供者类

  接下来我们来创建名为`MessageProvider`消息提供者类，用来发送消息内容到消息通知延迟队列，代码如下所示：

  ```java
  package com.makesailing.neo.provider;

  import com.alibaba.fastjson.JSON;
  import lombok.extern.slf4j.Slf4j;
  import org.springframework.amqp.core.AmqpTemplate;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.stereotype.Component;
  import org.springframework.util.StringUtils;

  /**
   * #
   *
   * @author jamie
   * @date 2018/9/12 15:01
   */
  @Slf4j
  @Component
  public class MessageProvider {

  	@Autowired
  	private AmqpTemplate rabbitMqTemplate;

  	/**
  	 * 发送延迟消息
  	 * @param messageContent 消息内容
  	 * @param exchange 交换机
  	 * @param routingKey 路由键
  	 * @param dalayTimes 过期时间
  	 */
  	public void sendMessage(Object messageContent,String exchange,String routingKey,final Long dalayTimes) {
  		if (StringUtils.isEmpty(exchange)) {
  			log.error("未找到消息队列 : [{}] ,所属的交换机", exchange);
  		}
  		log.info("延迟: [{}] 毫秒写入消息队列 : [{}] ,消息内容 : [{}]", dalayTimes, routingKey, JSON.toJSONString(messageContent));
  		// 执行发送消息到指定队列
  		rabbitMqTemplate.convertAndSend(exchange, routingKey, messageContent, message -> {
  			// 设置延迟毫秒值
  			message.getMessageProperties().setExpiration(String.valueOf(dalayTimes));
  			return message;
  		});

  	}
  }
  ```


  ```

  由于我们在  `pom.xml`配置文件内添加了`RabbitMQ`相关的依赖并且在上面`application.yml`文件内添加了对应的配置，`SpringBoot`为我们自动实例化了`AmqpTemplate`，该实例可以发送任何类型的消息到指定队列。

  我们采用`convertAndSend`方法，将消息内容发送到指定`Exchange`、`RouterKey`队列，并且通过`setExpiration`方法设置过期时间，单位：毫秒。

- 创建提供者模块入口类,如下所示:

  ```java
  package com.makesailing.neo;

  import lombok.extern.slf4j.Slf4j;
  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;

  /**
   * #
   *
   * @author jamie
   * @date 2018/9/12 15:41
   */
  @Slf4j
  @SpringBootApplication(scanBasePackages = "com.makesailing.neo")
  public class RabbitMqLazyProviderApplication {

  	public static void main(String[] args) {
  		SpringApplication.run(RabbitMqLazyProviderApplication.class, args);
  		log.info("RabbitMq 延迟提供者启动成功 ~~~");
  	}
  }


  ```

- 编写发送测试

  我们在`test`目录下创建一个测试类，如下所示：

  ```java
  package com.makesailing.neo.provider;

  import com.makesailing.neo.RabbitMqLazyProviderApplication;
  import com.makesailing.neo.enums.QueueEnum;
  import java.time.LocalDateTime;
  import org.junit.Test;
  import org.junit.Before;
  import org.junit.After;
  import org.junit.runner.RunWith;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.boot.test.context.SpringBootTest;
  import org.springframework.test.context.junit4.SpringRunner;

  /**
   * MessageProvider Tester.
   *
   * @author jamie
   * @since <pre>09/12/2018</pre>
   */
  @RunWith(SpringRunner.class)
  @SpringBootTest(classes = RabbitMqLazyProviderApplication.class)
  public class MessageProviderTest {

  	@Before
  	public void setUp() throws Exception {
  	}

  	@After
  	public void tearDown() throws Exception {
  	}

  	@Autowired
  	private MessageProvider messageProvider;

  	/**
  	 * Method: sendMessage(Object messageContent, String exchange, String routingKey, final Long dalayTimes)
  	 */
  	@Test
  	public void testSendMessage() throws Exception {
  		messageProvider.sendMessage("测试延迟消费,写入时间" + LocalDateTime.now(), QueueEnum.MESSAGE_TTL_QUEUE.getExchange(),
  			QueueEnum.MESSAGE_TTL_QUEUE.getRoutingKey(), 10000L);
  	}

  }
  ```

  > 注意：`@SpringBootTest`注解内添加了`classes`入口类的配置，因为我们是模块创建的项目并不是默认创建的`SpringBoot`项目，这里需要配置入口程序类才可以运行测试。

  在测试类我们注入了`MessageProvider`消息提供者，调用`sendMessage`方法发送消息到`消息通知延迟队列`，并且设置延迟的时间为`10秒`，这里衡量发送到指定队列的标准是要看`MessageRabbitMqConfiguration`配置类内的相关`Binding`配置，通过`Exchange`、`RouterKey`值进行发送到指定的队列。

  到目前为止我们的`rabbitmq-lazy-provider`消息提供模块已经编写完成了，下面我们来看看消息消费者模块。

  ​

#### 创建队列消息消费者

  我们再来创建一个名为`rabbitmq-lazy-consumer`的模块(Create New Maven Module)，同样需要在`pom.xml`配置文件内添加`rabbitmq-common`模块的依赖，如下所示：

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
      <artifactId>springboot-queue-delay</artifactId>
      <groupId>com.makesailing.neo</groupId>
      <version>0.0.1-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.makesailing.neo</groupId>
    <artifactId>rabbitmq-lazy-consumer</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <dependencies>
      <dependency>
        <groupId>com.makesailing.neo</groupId>
        <artifactId>rabbitmq-common</artifactId>
        <version>0.0.1-SNAPSHOT</version>
      </dependency>
    </dependencies>

  </project>

  ```

  在`resource`下创建一个名为`application.yml`的配置文件，在该配置文件内添加如下配置信息：

    ```yaml
    spring:
      rabbitmq:
        host: 127.0.0.1
        username: jamie
        password: 123456
        virtual-host: /simple
        port: 5672
        publisher-confirms: true
    ```

- 消息消费者类

  接下来创建一个名为`MessageConsumer`的消费者类，该类需要监听`消息通知队列`，代码如下所示：

  ```java
  package com.makesailing.neo.consumer;

  import java.time.LocalDateTime;
  import lombok.extern.slf4j.Slf4j;
  import org.springframework.amqp.rabbit.annotation.RabbitHandler;
  import org.springframework.amqp.rabbit.annotation.RabbitListener;
  import org.springframework.stereotype.Component;

  /**
   * #
   *
   * @author jamie
   * @date 2018/9/12 15:57
   */
  @Slf4j
  @Component
  @RabbitListener(queues = "message.center.create")
  public class MessageConsumer {

  	@RabbitHandler
  	public void handler(String content) {
  		log.info("消息内容 : {}", content);
  		log.info("消费结束时间 : [{}]", LocalDateTime.now());
  	}
  }

  ```

  在`@RabbitListener`注解内配置了监听的队列，这里配置内容是`QueueEnum`枚举内的`queueName`属性值，当然如果你采用常量的方式在注解属性上是直接可以使用的，枚举不支持这种配置，这里只能把`QueueName`字符串配置到`queues`属性上了。

  由于我们在消息发送时采用字符串的形式发送消息内容，这里在`@RabbitHandler`处理方法的参数内要保持数据类型一致！

- 消费者入口类

  我们为消费者模块添加一个入口程序类，用于启动消费者，代码如下所示：

  ```java
  package com.makesailing.neo;

  import lombok.extern.slf4j.Slf4j;
  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;

  /**
   * # 延迟消息 消费者
   *
   * @author jamie
   * @date 2018/9/12 15:54
   */
  @Slf4j
  @SpringBootApplication(scanBasePackages = "com.makesailing.neo")
  public class RabbitMqLazyConsumerApplication {

  	public static void main(String[] args) {
  		SpringApplication.run(RabbitMqLazyConsumerApplication.class, args);
  		log.info("延迟消息 消费者 启动成功 ~~~");
  	}
  }

  ```


#### 测试

我们的代码已经编写完毕，下面来测试下是否完成了我们预想的效果，步骤如下所示：

```tex
1. 启动消费者模块
2. 执行MessageProviderTest.testSendMessage()方法进行发送消息到通知延迟队列
3. 查看消费者模块控制台输出内容
```

我们可以在消费者模块控制台看到输出内容：

```java
2018-09-12 23:54:07.517  INFO 2712 --- [cTaskExecutor-1] c.m.neo.consumer.MessageConsumer         : 消息内容 : 测试延迟消费,写入时间2018-09-12T23:53:56.958
2018-09-12 23:54:07.517  INFO 2712 --- [cTaskExecutor-1] c.m.neo.consumer.MessageConsumer         : 消费结束时间 : [2018-09-12T23:54:07.517]
```

我们在提供者测试方法发送消息的时间为`23:54:07.517`，而真正消费的时间则为`23:54:07.517`，与我们预计的一样，消息延迟了`10秒`后去执行消费。

#### 总结

终上所述我们完成了消息队列的`延迟消费`，采用`死信`方式，通过消息过期方式触发，在实际项目研发过程中，延迟消费还是很有必要的，可以省去一些定时任务的配置。








































































