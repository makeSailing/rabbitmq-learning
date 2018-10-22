package com.makesailing.neo;
import com.alibaba.fastjson.JSON;
import com.makesailing.neo.domain.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

/**
 * #
 *
 * @author jamie.li
 * @date 2018/10/11 14:30
 */
@Slf4j
@EnableRabbit
@ComponentScan
@ImportResource({"classpath:/applicationContext.xml"})
public class Application {

	private static final String MESSAGE_INFO = "hello world rabbitmq";

	public static void main(String[] args) throws InterruptedException {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Application.class);

		//RabbitAdmin rabbitAdmin = context.getBean(RabbitAdmin.class);
		//log.info("rabbitAdmin info [{}]", rabbitAdmin);
		//
		//// 创建4种类型的Exchange,可重复执行
		//rabbitAdmin.declareExchange(new DirectExchange(ExchangeConstant.DIRECT_EXCHAGE, true, false));

		RabbitTemplate rabbitTemplate = context.getBean(RabbitTemplate.class);

		// direct exchange send

		//rabbitTemplate.convertAndSend(ExchangeConstant.DIRECT_EXCHAGE, RoutingKeyConstant.DIRECT_ROUTING_KEY, MESSAGE_INFO);


		//发送消息的后置处理器，MessagePostProcessor类的postProcessMessage方法得到的Message就是将参数Object内容转换成Message对象
	/*	rabbitTemplate
			.convertAndSend(ExchangeConstant.DIRECT_EXCHAGE, RoutingKeyConstant.DIRECT_ROUTING_KEY,MESSAGE, new MessagePostProcessor() {
				//在后置处理器上加上order和count属性
				@Override
				public Message postProcessMessage(Message message) throws AmqpException {
					System.out.println("-------处理前message-------------");
					System.out.println(message);
					message.getMessageProperties().getHeaders().put("order",10);
					message.getMessageProperties().getHeaders().put("count",1);
					return message;
				}
			});*/


		/*rabbitTemplate
			.convertAndSend(ExchangeConstant.DIRECT_EXCHAGE, RoutingKeyConstant.DIRECT_ROUTING_KEY,"message before", message ->{
				// lamdba 语法
				MessageProperties messageProperties = new MessageProperties();
				messageProperties.getHeaders().put("desc", "消息发送");
				messageProperties.getHeaders().put("type", 10);

				Message messageAfter = new Message("messge after".getBytes(), messageProperties);
				return messageAfter;
			});*/

		// 在spring容器中启动SimpleMessageListenerContainer
		//context.getBean(SimpleMessageListenerContainer.class).start();


		//rabbitTemplate.convertAndSend("logger.info.direct.exchange","logger.info.routing.key", "hello logger info");
		//rabbitTemplate.convertAndSend("test.order.direct.exchange","test.order.routing.key", MESSAGE_INFO);



		// －－－－－－－－－－－－－－－－　　发送 JSON 类型数据　－－－－－－－－－－－－－－－

		// 如果消息生产端没有指定ContentType类型,那么Jackson2JsonMessageConverter消息处理器还是当作 byte[]处理
		//sendJsonMessage(rabbitTemplate);

		//  指定消息 ContentType 类型为 application/json ,消费者需使用 Map进行消息接收
		//sendApplicationJsonMessage(rabbitTemplate);

		// 发送 List JSON 类型数据 消费者需要使用 List 进行接收
		//sendListJsonMessage(rabbitTemplate);


		/**
		 * 总结
		 使用Jackson2JsonMessageConverter处理器，客户端发送JSON类型数据，但是没有指定消息的contentType类型，那么Jackson2JsonMessageConverter就会将消息转换成byte[]类型的消息进行消费。
		 如果指定了contentType为application/json，那么消费端就会将消息转换成Map类型的消息进行消费。
		 如果指定了contentType为application/json，并且生产端是List类型的JSON格式，那么消费端就会将消息转换成List类型的消息进行消费。

		 */

		// －－－－－－－－－－－－－－－－　上面我们提到的是将实体类型转换成Map或者List类型，这样转换没有多大意义，
		// 我们需要消费者将生产者的消息对象格式转换成对应的消息格式，而不是Map或者List对象　－－－－－－－－－－－－－－－

		// 发送User JSON数据,并指定消费者使用User对象进行接收
		//sendUserMessage(rabbitTemplate);

		// －－－－－－－－－－－－－－－－　　@RabbitListener 注解使用　－－－－－－－－－－－－－－－

		/**
		 * @RabbitListener和@RabbitHandler搭配使用
		 @RabbitListener可以标注在类上面，当使用在类上面的时候，需要配合@RabbitHandler注解一起使用，
		 @RabbitListener标注在类上面表示当有收到消息的时候，就交给带有@RabbitHandler的方法处理，具体找哪个方法处理，需要跟进MessageConverter转换后的

		 */

		//rabbitTemplate.convertAndSend("test.order.direct.exchange","test.order.routing.key", "hello rabbitmq order");

		//sendMessageTTL(rabbitTemplate,"30000");

		sendMessageLength(rabbitTemplate, 10);

		TimeUnit.SECONDS.sleep(30);
		context.close();

	}

	/**
	 * 限制消息发送长度
	 * @param rabbitTemplate
	 * @param length
	 */
	private static void sendMessageLength(RabbitTemplate rabbitTemplate, int length) {
		MessageProperties properties = new MessageProperties();
		properties.setContentType("application/json");
		Message message = new Message("hello".getBytes(), properties);
		// apple ququq 最多只能有3个队列,多了会从最早的队列开始删除
		for (int i = 0; i < 10; i++) {
			rabbitTemplate.convertAndSend("", "apple", message);
			rabbitTemplate.convertAndSend("", "banana", message);
		}
	}

	/**
	 * 发送消息时设置过期时间
	 * @param rabbitTemplate
	 * @param expiration 毫秒
	 */
	private static void sendMessageTTL(RabbitTemplate rabbitTemplate,String expiration) {
		// 设置Message TTL
		MessageProperties properties = new MessageProperties();
		properties.setContentType("application/json");
		// 设置消息的过期时间
		properties.setExpiration(expiration);
		Message message = new Message("hello rabbitmq message ttl".getBytes(), properties);
		rabbitTemplate.convertAndSend("", "weixin", message);

		// 在代码里定义 Queue TTL
		rabbitTemplate.convertAndSend("", "duanxin", "hello rabbitmq queue ttl");

		//如果同时制定了Message TTL，Queue TTL，则小的那个时间生效。

	}

	/**
	 * 发送User JSON数据,并指定消费者使用User对象进行接收
	 * @param rabbitTemplate
	 */
	private static void sendUserMessage(RabbitTemplate rabbitTemplate) {
		User user = createUser();
		String userJson = JSON.toJSONString(user);
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
		// 指定 _TypeId_ 属性值必须是消费端User全类名,如果不匹配会报错
		messageProperties.getHeaders().put("__TypeId__","com.makesailing.neo.domain.User");
		/**
		 * 如果不配置全类名,需在 消费者配置文件中定义如下配置
		 Jackson2JsonMessageConverter jackson2JsonMessageConverter =new Jackson2JsonMessageConverter();

		 //消费端配置映射
		 Map<String, Class<?>> idClassMapping = new HashMap<>();
		 idClassMapping.put("order",Order.class);
		 idClassMapping.put("user",User.class);

		 DefaultJackson2JavaTypeMapper jackson2JavaTypeMapper = new DefaultJackson2JavaTypeMapper();
		 jackson2JavaTypeMapper.setIdClassMapping(idClassMapping);

		 System.out.println("在jackson2JsonMessageConverter转换器中指定映射配置");
		 jackson2JsonMessageConverter.setJavaTypeMapper(jackson2JavaTypeMapper);
		 adapter.setMessageConverter(jackson2JsonMessageConverter);
		 */
		Message userMessage2 = new Message(userJson.getBytes(), messageProperties);
		rabbitTemplate.convertAndSend("logger.info.direct.exchange","logger.info.routing.key", userMessage2);
	}


	private static User createUser() {
		User user = new User();
		user.setEmail("123@qq.com");
		user.setUsername("tom");
		user.setPassword("123456");
		user.setRegtime(new Date());
		return user;
	}

	/**
	 * 发送 JSON类型数据
	 * @param rabbitTemplate
	 */
	private static void sendJsonMessage(RabbitTemplate rabbitTemplate) {
		User user = createUser();
		// 如果消息生产端没有指定ContentType类型,那么Jackson2JsonMessageConverter消息处理器还是当作 byte[]处理
		String userJson = JSON.toJSONString(user);
		rabbitTemplate.convertAndSend("logger.info.direct.exchange","logger.info.routing.key", userJson);
	}

	/**
	 * 发送 JSON格式数据并指定其 MessageProperties ContentType 为 application/json
	 * @param rabbitTemplate
	 */
	private static void sendApplicationJsonMessage(RabbitTemplate rabbitTemplate) {
		User user = createUser();
		String userJson = JSON.toJSONString(user);
		// 指定消息 ContentType 类型为 application/json ,消费者需使用 Map进行消息接收
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setContentType("application/json");
		Message userMessage = new Message(userJson.getBytes(), messageProperties);
		rabbitTemplate.convertAndSend("logger.info.direct.exchange","logger.info.routing.key", userMessage);
	}

	/**
	 * 发送 List JSON 数据
	 * @param rabbitTemplate
	 */
	private static void sendListJsonMessage(RabbitTemplate rabbitTemplate) {
		User user = createUser();

		User user2 = new User();
		user2.setEmail("456@qq.com");
		user2.setUsername("jack");
		user2.setPassword("123456");
		user2.setRegtime(new Date());

		List<User> userList = new ArrayList<>();
		userList.add(user);
		userList.add(user2);

		String userListJson = JSON.toJSONString(userList);
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setContentType("application/json");
		Message userListMessage = new Message(userListJson.getBytes(), messageProperties);

		rabbitTemplate.convertAndSend("logger.info.direct.exchange","logger.info.routing.key", userListMessage);
	}

}


