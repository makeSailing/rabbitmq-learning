### SpringBoot & RabbitMQ完成DirectExchange分布式消息消费

消息队列目前流行的有KafKa 、RabbitMQ、ActiveMQ, 它们的诞生无非不是为了解决消息的分布式消费,完成项目、服务之间的解耦动作. 消息队列提供者与消费者之间完成采用异步通信方式,极大的提高了系统的响应能力,从而提高系统的网络请求吞吐量.

每一种的消息队列都有它在设计上的独一无二的优势,在实际的项目技术造型时根据项目的需求来确定.

#### 目标

基于 **SpringBoot** 项目整合**RabbitMQ** 消息队列,完成 **DirectExchange(路由键)**分布式消息消费

**Exchange**

在 **RabbitMQ**中有三种常用的转发方式,分别是 : 

- DirectExchange  : 路由键方式转发消息
- FanoutExchange : 广播方式转发消息
- TopicExchange : 主题匹配方式转发消息


`DirectExchange`路由键方式，根据设置的路由键的值进行完全匹配时转发，下面我们来看一张图，形象的介绍了转发消息匹配流程，如下图所示：

![](https://user-gold-cdn.xitu.io/2018/9/9/165be69660f878ae?w=463&h=307&f=png&s=44682)

我们可以看到上图,当消息被提供者发送到RabbitMQ后,会根据配置队列的交换以及绑定实例进行转发消息,上图只会将消息转发路由键为**Key**的队列消费者对应的实现方法逻辑中,从而完成消息的消费过程

##### 构建项目

构建一个maven项目,包含 rabbitmq-common , rabbitmq-consumer ,rabbitmq-provider 等模块.

父类的 pom.xml 结构如下:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.makesailing.neo</groupId>
  <artifactId>springboot-direct-exchange</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <modules>
    <module>rabbitmq-provider</module>
    <module>rabbitmq-common</module>
    <module>rabbitmq-consumer</module>
  </modules>
  <packaging>pom</packaging>

  <name>springboot-direct-exchange</name>
  <description>Spring Boot DirectExchange</description>

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
    <!--rabbitmq依赖-->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
    <!--web依赖-->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!--lombok依赖-->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>
    <!--fastjson依赖-->
    <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>fastjson</artifactId>
      <version>1.2.40</version>
    </dependency>
    <!--测试依赖-->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
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

我们来模拟用户注册完成后,将注册用户的编号通过**rabbmq-provider**模块发送到**RabbitMQ** , 然后 **RabbitMQ** 根据配置的 **DirectExchange**的路由键进行异步转发.

**初始化用户表**

```mysql
CREATE TABLE `user_info` (
  `id` BIGINT(20)  PRIMARY KEY not null AUTO_INCREMENT COMMENT '用户编号',
  `user_name` varchar(20) DEFAULT NULL COMMENT '用户名称',
  `name` varchar(20) DEFAULT NULL COMMENT '真实姓名',
  `age` SMALLINT(4) DEFAULT NULL COMMENT '用户年龄',
  `balance` decimal(10,0) DEFAULT NULL COMMENT '用户余额'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户基本信息表';
```

### 构建 rabbitmq-provider 项目

基于我们上述的项目创建一个`Maven`子模块，命名为：`rabbitmq-provider`，因为是直接创建的`Module`项目，`IDEA`并没有给我创建`SpringApplication`启用类。

pom.xml 如下

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <parent>
    <artifactId>springboot-direct-exchange</artifactId>
    <groupId>com.makesailing.neo</groupId>
    <version>0.0.1-SNAPSHOT</version>
  </parent>
  <modelVersion>4.0.0</modelVersion>

  <artifactId>rabbitmq-provider</artifactId>

  <dependencies>
    <!--mysql依赖-->
    <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
    </dependency>
    <!--druid数据源依赖-->
    <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>druid</artifactId>
      <version>1.0.29</version>
    </dependency>
    <!--data jpa依赖-->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
  </dependencies>

</project>
```



##### 创建入口类

下面我们自行创建一个`Provider`项目启动入口程序，如下所示：

```java
package com.makesailing.neo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 17:32
 */
@Slf4j
@SpringBootApplication
public class RabbitmqProviderApplication {

  public static void main(String[] args) {
    SpringApplication.run(RabbitmqProviderApplication.class, args);
    log.info("【【【【【消息队列-消息提供者启动成功.】】】】】");
  }
}

```

##### application.yml配置文件

下面我们在`src/main/resource`目录下创建`application.yml`并将对应`RabbitMQ`以及`Druid`的配置加入，如下所示：

```yaml
spring:
    datasource:
        driver-class-name: com.mysql.jdbc.Driver
        password: abc
        url: jdbc:mysql://localhost:3306/springboot?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true
        username: root
    rabbitmq:
        host: localhost
        password: guest
        port: 5672
        publisher-confirms: true
        username: guest
        virtual-host: /
	
```

在`RabbitMQ`内有个`virtual-host`即虚拟主机的概念，一个`RabbitMQ`服务可以配置多个虚拟主机，每一个虚拟机主机之间是相互隔离，相互独立的，授权用户到指定的`virtual-host`就可以发送消息到指定队列。

##### 用户实体

本章数据库操作采用`spring-data-jpa`，我们基于`user_info`数据表对应创建实体，如下所示：

```java
package com.makesailing.neo.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 17:56
 */
@Data
@Entity
@Table(name = "user_info")
public class UserEntity implements Serializable {

  private static final long serialVersionUID = -2703945921213927662L;

  /**
   * 用户编号
   */
  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;
  /**
   * 用户名称
   */
  @Column(name = "user_name")
  private String userName;
  /**
   * 姓名
   */
  @Column(name = "name")
  private String name;
  /**
   * 年龄
   */
  @Column(name = "age")
  private int age;
  /**
   * 余额
   */
  @Column(name = "balance")
  private BigDecimal balance;

}

```

##### 用户数据接口

创建`UserRepository`用户数据操作接口，并继承`JpaRepository`获得`spring-data-jpa`相关的接口定义方法。如下所示：

```java
package com.makesailing.neo.repository;

import com.makesailing.neo.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 17:59
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {


}
```

##### 用户业务逻辑实现

本章只是简单完成了数据的添加，代码如下所示：

```java
package com.makesailing.neo.service;

import com.makesailing.neo.domain.UserEntity;
import com.makesailing.neo.enums.ExchangeEnum;
import com.makesailing.neo.enums.QueueEnum;
import com.makesailing.neo.queue.service.QueueMessageService;
import com.makesailing.neo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 18:00
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

  @Autowired
  private UserRepository userRepository;

  /**
   * 消息队列业务逻辑实现
   */
  @Autowired
  private QueueMessageService queueMessageService;

  /**
   * 保存用户信息
   * @param userEntity
   * @return
   */
  public Long save(UserEntity userEntity) throws Exception {
    userRepository.save(userEntity);

    /**
     * 将消息写入消息队列
     */
    queueMessageService.send(userEntity.getId(), ExchangeEnum.USER_REGISTER, QueueEnum.USER_REGISTER);

    return userEntity.getId();
  }
}

```

在上面业务逻辑实现类内出现了一个名为`QueueMessageService`消息队列实现类，该类是我们定义的用于发送消息到消息队列的统一入口，在下面我们会详细讲解。

##### 用户控制器

创建一个名为`UserController`的控制器类，对应编写一个添加用户的请求方法，如下所示

```java
package com.makesailing.neo.controller;

import com.makesailing.neo.domain.UserEntity;
import com.makesailing.neo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 18:06
 */
@RestController
@RequestMapping("/user")
public class UserController {

  @Autowired
  private UserService userService;

  /**
   * 保存用户
   */
  @PostMapping("/save")
  public UserEntity save(@RequestBody UserEntity userEntity) throws Exception {
    userService.save(userEntity);
    return userEntity;
  }

}

```

到这我们添加用户的流程已经编写完成了，那么我们就来看下消息队列`QueueMessageService`接口的定义以及实现类的定义。

**消息队列方法定义接口**

创建一个名为**QueueMessageService** 的接口并且继承了 **RabbitTemplaate.ConfirmCallback**接口, 而RabbitTemplate.ConfirmCallback接口是用来回调消息发送成功后的方法,当一个消息被成功写入到RabbitMQ服务端时,就会自动回调RabbitTemlate.ConfirmCallback接口内的confirm方法完成通知, QueueMessageService 代码如下

```java
package com.makesailing.neo.queue.service;

import com.makesailing.neo.enums.ExchangeEnum;
import com.makesailing.neo.enums.QueueEnum;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 18:29
 */
public interface QueueMessageService extends RabbitTemplate.ConfirmCallback {

  /**
      * 发送消息到rabbitmq消息队列
     * @param message 消息内容
     * @param exchangeEnum 交换配置枚举
     * @param queueEnum 队列配置枚举
     * @throws Exception
     */
  public void send(Object message, ExchangeEnum exchangeEnum, QueueEnum queueEnum) throws Exception;


}

```

接下来我们需要实现该接口内的所有方法，并做出一些业务逻辑的处理。

##### 消息队列业务实现

创建名为`QueueMessageServiceSupport`实体类实现`QueueMessageService`接口，并实现接口内的所有方法，如下所示：

```java
package com.makesailing.neo.queue.service.impl;

import com.makesailing.neo.enums.ExchangeEnum;
import com.makesailing.neo.enums.QueueEnum;
import com.makesailing.neo.queue.service.QueueMessageService;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Component
public class QueueMessageServiceSupportImpl implements QueueMessageService{
    /**
     * 消息队列模板
     */
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void send(Object message, ExchangeEnum exchangeEnum, QueueEnum queueEnum) throws Exception {
        //设置回调为当前类对象
        rabbitTemplate.setConfirmCallback(this);
        //构建回调id为uuid
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        //发送消息到消息队列
        rabbitTemplate.convertAndSend(exchangeEnum.getValue(),queueEnum.getRoutingKey(),message,correlationId);
    }

    /**
     * 消息回调确认方法
     * @param correlationData 请求数据对象
     * @param ack 是否发送成功
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        System.out.println(" 回调id:" + correlationData.getId());
        if (ack) {
            System.out.println("消息发送成功");
        } else {
            System.out.println("消息发送失败:" + cause);
        }
    }
}
```

`convertAndSend`方法用于将`Object`类型的消息转换后发送到`RabbitMQ`服务端，发送是的消息类型要与消息消费者方法参数保持一致。

在`confirm`方法内，我们仅仅打印了消息发送时的`id`，根据`ack`参数输出消息发送状态。

>在上面代码中我们注入了`RabbitTemplate`消息队列模板实例，而通过该实例我们可以将消息发送到`RabbitMQ`服务端。那么这个实例具体在什么地方定义的呢？我们带着这个疑问来创建下面的模块，我们需要将`RabbitMQ`相关的配置抽取出来作为一个单独的`Module`存在。

### 构建 rabbitmq-common 项目

该模块项目很简单，只是添加`RabbitMQ`相关的配置信息，由于`Module`是一个子模块所以继承了`parent`所有的依赖，当然我们用到的`RabbitMQ`相关依赖也不例外。

##### 配置rabbitmq

在创建配置类之前，我们先来定义两个枚举，分别存放了队列的交换信息、队列路由信息，

- ExchangeEnum (存放了队列交换配置信息)

  ```java
  package com.makesailing.neo.enums;


  /**
   * # rabbit 交换配置枚举
   *
   * @Author: jamie.li
   * @Date: Created in  2018/9/9 18:33
   */

  public enum ExchangeEnum {

    USER_REGISTER("user.register.topic.exchange");

    private String value;

    ExchangeEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  ```

  ​

- QueueEnum (存放了队列信息以及队列的路由配置信息)

  ```java
  package com.makesailing.neo.enums;

  /**
   * # 队列配置枚举
   *
   * @Author: jamie.li
   * @Date: Created in  2018/9/9 18:36
   */
  public enum QueueEnum {

    USER_REGISTER("user.register.queue", "user.register")
    ;
    private String name;

    private String routingKey;

    QueueEnum(String name, String routingKey) {
      this.name = name;
      this.routingKey = routingKey;
    }

    public String getName() {
      return name;
    }

    public String getRoutingKey() {
      return routingKey;
    }
  }

  ```

创建名为`UserRegisterQueueConfiguration`的实体类用于配置本章用到的用户注册队列信息，如果你得项目中使用多个队列，建议每一个业务逻辑创建一个配置类，分开维护，这样不容易出错。配置信息如下：

```java
package com.makesailing.neo.config;

import com.makesailing.neo.enums.ExchangeEnum;
import com.makesailing.neo.enums.QueueEnum;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * # 用户注册消息队列配置
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 18:40
 */
@Configuration
public class UserRegisterQueueConfiguration {

  /**
   * 配置路由交换对象实例
   * @return
   */
  @Bean
  public DirectExchange userRegisterDirectExchange() {
    return new DirectExchange(ExchangeEnum.USER_REGISTER.getValue());
  }

  /**
   * 配置用户注册队列实例并设置持久化队列
   * @return
   */
  @Bean
  public Queue userRegisterQueue() {
    return new Queue(QueueEnum.USER_REGISTER.getName(), true);
  }

  /**
   *
   * @return
   */
  @Bean
  public Binding userRegisterBinging() {
    return BindingBuilder.bind(userRegisterQueue()).to(userRegisterDirectExchange())
        .with(QueueEnum.USER_REGISTER.getRoutingKey());
  }
}

```

该配置类大致分为如下三部分：

- 配置交换实例
  配置`DirectExchange`实例对象，为交换设置一个名称，引用`ExchangeEnum`枚举配置的交换名称，消息提供者与消息消费者的交换名称必须一致才具备的第一步的通讯基础。
- 配置队列实例
  配置`Queue`实例对象，为消息队列设置一个名称，引用`QueueEnum`枚举配置的队列名称，当然队列的名称同样也是提供者与消费者之间的通讯基础。
- 绑定队列实例到交换实例
  配置`Binding`实例对象，消息绑定的目的就是将`Queue`实例绑定到`Exchange`上，并且通过设置的路由`Key`进行消息转发，配置了路由`Key`后，只有符合该路由配置的消息才会被转发到绑定交换上的消息队列。

我们的`rabbitmq-common`模块已经编写完成。

##### 添加 rabbitmq-provider 依赖 rabbitmq-common

```xml
 <!--添加common模块依赖-->
    <dependency>
      <groupId>com.makesailing.neo</groupId>
      <artifactId>rabbitmq-common</artifactId>
      <version>1.0.0-SNAPSHOT</version>
    </dependency>
```

可以看到我们将`rabbitmq-common`模块添加到了`rabbitmq-provider`模块的`pom`配置文件内，完成了模块之间的相互依赖，这样我们`rabbitmq-provider`就自动添加了对应的消息队列配置。

### 构建rabbitmq-consumer

我们再来创建一个`rabbitmq-consumer`队列消息消费者模块，用于接受消费用户注册消息。

##### 创建入口类

同样我们先来创建一个`SpringApplication`入口启动类，如下所示：

```java
package com.makesailing.neo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 18:57
 */
@Slf4j
@SpringBootApplication
public class RabbitmqConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(RabbitmqConsumerApplication.class, args);
    log.info("【【【【【消息队列-消息消费者启动成功.】】】】");
  }

}
```

##### pplication.yml 配置文件

配置文件的消息队列配置信息要与`rabbitmq-provider`配置文件一致，如下所示：

```yaml

spring:
  rabbitmq:
    username: guest
    password: guest
    host: 127.0.0.1
    virtual-host: /
    port: 5672
    publisher-confirms: true
server:
  port: 8085

```

> 如果`RabbitMQ`配置信息与`rabbitmq-provider`不一致，就不会收到消费消息。

##### 用户注册消息消费者

创建名为`UserConsumer`类，用于完成消息监听，并且实现消息消费，如下所示：

```java
package com.makesailing.neo.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 19:02
 */
@Component
@RabbitListener(queues ="user.register.queue")
public class UserConsumer {

  @RabbitHandler
  public void execute(Long userId) {
    // 省略业务逻辑处理

    System.out.println("用户: " + userId + "完成了注册");

  }
}

```

在消息消费者类内，有两个陌生的注解：

- @RabbitListener
  `RabbitMQ`队列消息监听注解，该注解配置监听`queues`内的队列名称列表，可以配置多个。队列名称对应本章`rabbitmq-common`模块内`QueueEnum`枚举`name`属性。
- @RabbitHandler
  `RabbitMQ`消息处理方法，该方法的参数要与`rabbitmq-provider`发送消息时的类型保持一致，否则无法自动调用消费方法，也就无法完成消息的消费。

##### 运行测试

我们接下来在`rabbitmq-provider`模块`src/test/java`下创建一个测试用例，访问用户注册控制器请求路径，如下所示：

```java
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.alibaba.fastjson.JSON;
import com.makesailing.neo.domain.UserEntity;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 19:07
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RabbitmqProviderApplication.class)
public class UserTest {
  /**
   * 模拟mvc测试对象
   */
  private MockMvc mockMvc;

  /**
   * web项目上下文
   */
  @Autowired
  private WebApplicationContext webApplicationContext;

  /**
   * 所有测试方法执行之前执行该方法
   */
  @Before
  public void before() {
    //获取mockmvc对象实例
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  /**
   * 测试添加用户
   */
  @Test
  public void testUserAdd() throws Exception {
    UserEntity userEntity = new UserEntity();
    userEntity.setUserName("makesailing");
    userEntity.setName("jamie");
    userEntity.setAge(18);

     mockMvc.perform( post("/user/save")
        .contentType(MediaType.APPLICATION_JSON)
        .content(JSON.toJSONString(userEntity)))
        .andDo(MockMvcResultHandlers.log())
        .andReturn();
  }
}

```

##### 启动 rabbitmq-consumer

我们先来把`rabbitmq-consumer`项目启动，控制台输出启动日志如下所示：

```java
018-09-09 22:07:29.342  INFO 11500 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Located managed bean 'rabbitConnectionFactory': registering with JMX server as MBean [org.springframework.amqp.rabbit.connection:name=rabbitConnectionFactory,type=CachingConnectionFactory]
2018-09-09 22:07:29.466  INFO 11500 --- [           main] o.s.c.support.DefaultLifecycleProcessor  : Starting beans in phase 2147483647
2018-09-09 22:07:29.499  INFO 11500 --- [cTaskExecutor-1] o.s.a.r.c.CachingConnectionFactory       : Attempting to connect to: [127.0.0.1:5672]
2018-09-09 22:07:29.869  INFO 11500 --- [cTaskExecutor-1] o.s.a.r.c.CachingConnectionFactory       : Created new connection: rabbitConnectionFactory#70c83633:0/SimpleConnection@7191b045 [delegate=amqp://guest@127.0.0.1:5672/, localPort= 3393]
2018-09-09 22:07:30.037  INFO 11500 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 8085 (http)
2018-09-09 22:07:30.048  INFO 11500 --- [           main] c.m.neo.RabbitmqConsumerApplication      : Started RabbitmqConsumerApplication in 8.782 seconds (JVM running for 9.838)
2018-09-09 22:07:30.048  INFO 11500 --- [           main] c.m.neo.RabbitmqConsumerApplication      : 【【【【【消息队列-消息消费者启动成功.】】】】
```

该部分启动日志就是我们配置的`RabbitMQ`初始化信息，我们可以看到项目启动时会自动与配置的`RabbitMQ`进行关联：

```java
[delegate=amqp://guest@127.0.0.1:5672/, localPort= 3393]
```

##### 运行测试用例

接下来我们执行`rabbitmq-provider`项目的测试用例，来查看控制台的输出内容如下所示：

```java
 回调id:c9b6717d-b9eb-41a2-ac17-cbdb19437e30
消息发送成功
```

已经可以正常的将消息发送到`RabbitMQ`服务端，并且接收到了回调通知，那么我们的`rabbitmq-consumer`项目是不是已经执行了消息的消费呢？我们打开`rabbitmq-consumer`控制台查看输出内容如下所示:

```java
用户: 2完成了注册
```

看以看到已经可以成功的执行`UserConsumer`消息监听类内的监听方法逻辑，到这里消息队列路由一对一的方式已经讲解完了。

##### 总结

本章主要讲解了`RabbitMQ`在不同操作系统下的安装方式，以及通过三个子模块形象的展示了消息的分布式处理，整体流程：rabbitmq-provider -> `RabbitMQ`服务端 -> rabbitmq-consumer，消息的转发是非常快的，`RabbitMQ`在收到消息后就会检索当前服务端是否存在该消息的消费者，如果存在将会马上将消息转发。
























































