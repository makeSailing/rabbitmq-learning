## RabbitMQ实战教程(一) : 安装及相关概念介绍

- 由于本人只在`Windows`安装 `RabbitMQ` 服务 ,其他系统安装暂时没有涉及,如果有需要请自行搜索安装教程. . .

### 1 . Windows 安装

`Windows`安装需要先安装`Erlang`，再安装`RabbitMQ`

- 第一步：安装`Erlang OTP`，由于`RabbitMQ`是用`Erlang`编写的，所以在安装`RabbitMQ`之前要先安装`Erlang` 下载地址：<http://www.erlang.org/downloads> 下载最新版本即可，例如`OTP 21.0.1 Windows 64-bit Binary File`, 下载完成后解压，双击`otp_win64_21.0.1.exe` 一路next即可，安装成功后需要配置环境变量，可以新建一个变量，例如`ERLANG_HOME=D:\erl9.2`，最后将环境变量追加到Path中去Path中追加：`%ERLANG_HOME\bin%`

- 第二步：安装`RabbitMQ`，在官网上下载最新的`RabbitMQ`(<http://www.rabbitmq.com/>) 双击`rabbitmq-server-3.7.6` 一路next即可，安装成功后同样需要配置环境变量，将`D:\RabbitMQ\rabbitmq_server-3.6.10\sbin`追加到Path中 . `rabbitmq-server-3.7.6` 版本是不需要安装 `RabbitMQ Management`插件 ,好像 3.7 之前的版本是需要安装 `RabbitMQ Management`插件的 .

- 第三步 : 安装成功后打开浏览器输入以下地址 `http://localhost:15672/`  就可以看下如下页面 : 

  ![](https://user-gold-cdn.xitu.io/2018/9/15/165dde6546622cf4?w=359&h=160&f=png&s=10015)

  使用访客账号(username : guest , password : guest) 就可登录进去 ,页面如下所示 : 

  ![](https://user-gold-cdn.xitu.io/2018/9/15/165ddea3006e5242?w=1338&h=612&f=png&s=57458)

- 至此 `RabbitMQ` 服务安装完成


### 2 . 相关概念介绍

####  2.1  简介

`RabbitM`Q是一个开源的`AMQP`实现，服务器端用Erlang语言编写，支持多种客户端。用于在分布式系统中存储转发消息，在易用性、扩展性、高可用性等方面表现不俗，消息队列是一种应用系统之间的通信方法，是通过读写出入队列的消息来通信（RPC则是通过直接调用彼此来通信的）

`AMQP`(Advanced Message Queuing Protocol)高级消息队列协议是应用层协议的一个开放标准，为面向消息的中间件设计。消息中间件主要用于组件之间的解耦，消息的发送者无需知道消息使用者的存在，反之亦然。`AMQP`的主要特征是面向消息、队列、路由（包括点对点和发布/订阅）、可靠性、安全。

#### 2.2 基本概念 

- `ConnectionFactory`(连接工厂) : 生产`Connection`的工厂 . 
- `Connection`(连接) : 是`RabbitMQ`的`socket`的长连接,它封装了`socket`的协议相关部分逻辑.
- `Channel`(信道) : 是建立在`Connection`连接之上的一种轻量级的连接,我们大部分的业务操作是`Channel`这个接口中完成的 ,包括定义队列的声明`queueDirect` 、交换机声明`exchangeDeclare` 、队列的绑定`queueBind`、发布消息的`basicPublish`、消费消息的`basicConsume`等 . 如果把`Connection`比作一条光纤电缆的话,那么`Channel`信道就比作成光纤电缆中其中的一束光纤 . 一个`Connection`可以创建任意数量的`Channel` 信道 .
- `Producet`(生产者) : 生产者用于发布消息 .
- `Exchange`(交换机): 生产者会将消息发送到交换机,然后交换机通过路由路由规则将消息路由到匹配到队列中 .
- `RoutingKey`(路由键) : 一个`String`,用于定义路由规则,在队列绑定的时候需要指定路由键,在生产者发布消息时需要指定路由键,当消息的路由键和队列绑定的路由键相匹配时,消息就会发送到该队列.
- `Queue`(队列) : 用于存储消息容器,可以看成一个有序的数组,生产者生产的消息会发送到交换机中,最终交换机将消息存储到某个或某些队列中,队列可被消费者订阅,消费者从订阅的队列中获取消息 .
- `Binding`(绑定) : `Binding`并不是一个概念,而是一种操作,`RabbitMQ`中通过绑定,以路由键作为桥梁将`Exchange`与`Queue`关联起来(`Exchange` --> `RoutingKey` -->`Queue`) ,这样`RabbitMQ`就知道如何正确地将消息路由到指定的队列了,通过`queueBind`方法`Exchange`、`RoutingKey`、`Queue`绑定起来 .
- `Consumer`(消费者) : 用于从队列中获取消息,消费者只需要关注队列即可,不需要关注交换机和路由键,消费者可以通过`basicConsume`(订阅模式可以从队列中一直持续的自动接收消息)或者`basicGet`(先订阅消息,然后获取单条消息,再然后取消订阅,也就是说`basicGet`一次只能获取一条消息,如果还想获取下一条还要再次调用`basicGet`来从队列中获取消息) . 
- `vhost`(虚拟主机) : `RabbitMQ` 通过虚拟主机(`virtual host`)来分发消息 ,拥有自己独立的权限控制, 不同`vhost`之间是隔离的、单独的 ,`vhost`是权限控制的基本单位,用户只能访问与之绑定的vhost ,默认`vhost` : "/" ,默认用户 "guest" ,默认密码 "guest" , 来访问默认的`vhost` .

下面说明了生产将消息发送到交换机中,然后又路由到指定的队列中. 消息者从指定的队列中消费消息

![](https://user-gold-cdn.xitu.io/2018/9/16/165e02e13a34add4?w=659&h=359&f=png&s=276876)

#### 
