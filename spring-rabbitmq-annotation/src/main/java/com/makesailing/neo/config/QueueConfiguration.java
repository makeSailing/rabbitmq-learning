package com.makesailing.neo.config;

import com.makesailing.neo.constant.ExchangeConstant;
import com.makesailing.neo.constant.QueueConstant;
import com.makesailing.neo.constant.RoutingKeyConstant;
import com.makesailing.neo.queue.consumer.DirectMessageConsumer;
import com.makesailing.neo.queue.consumer.FanoutMessageConsumer;
import com.makesailing.neo.queue.consumer.MessageHandler;
import com.makesailing.neo.queue.consumer.TopicMessageConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * # 队列配置
 *
 * @author jamie.li
 * @date 2018/9/26 17:46
 */
@Configuration
public class QueueConfiguration extends RabbitMQConfiguration{

	// ########################   direct queue 配置  ####################################

	@Bean
	public DirectExchange directExchange() {
		DirectExchange directExchange = new DirectExchange(ExchangeConstant.DIRECT_EXCHAGE, true, false);
		return directExchange;
	}

	@Bean
	public Queue directQueue() {
		Queue queue = new Queue(QueueConstant.DIRECT_QUEUE, true, false, false);
		return queue;
	}

	@Bean
	public Binding directBinding() {
		Binding binding = BindingBuilder.bind(directQueue()).to(directExchange())
			.with(RoutingKeyConstant.DIRECT_ROUTING_KEY);
		return binding;
	}


	@Autowired
	private DirectMessageConsumer directMessageConsumer;

	@Bean(name = "directMessageListenerContainer")
	public SimpleMessageListenerContainer directMessageListenerContainer() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		//设置autoStartUp为false表示SimpleMessageListenerContainer没有启动 ,就不能进行消费.
		// 也可以在Spring容器中进行启动  SimpleMessageListenerContainer
		//container.setAutoStartup(false);
		container.setConnectionFactory(connectionFactory());
		container.setQueues(directQueue());
		container.setMessageListener(directMessageConsumer);
		// 如果设置了 MANUAL(手动),消费者那边需要手动答复,不能rabbit server 不会删除这个已经消费掉的消息 ,默认值是 AUTO
		//container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		return container;
	}


	// ########################   fanout queue 队列 配置  ####################################

	@Bean
	public FanoutExchange fanoutExchange() {
		FanoutExchange fanoutExchange = new FanoutExchange(ExchangeConstant.FANOUT_EXCHAGE, true, false);
		return fanoutExchange;
	}

	@Bean
	public Queue fanoutQueue() {
		Queue queue = new Queue(QueueConstant.FANOUT_QUEUE, true, false, false);
		return queue;
	}

	@Bean
	public Binding fanoutBinding() {
		Binding binding = BindingBuilder.bind(fanoutQueue()).to(fanoutExchange());
		return binding;
	}

	@Autowired
	private FanoutMessageConsumer fanoutMessageConsumer;

	/**
	 * 如果不指定 bean name,默认采用方法名
	 * @return
	 */
	@Bean(name = "fanoutMessageListenerContainer")
	public SimpleMessageListenerContainer fanoutMessageListenerContainer() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory());
		container.setQueues(fanoutQueue());
		container.setMessageListener(fanoutMessageConsumer);
		// 如果设置了 MANUAL(手动),消费者那边需要手动答复,不能rabbit server 不会删除这个已经消费掉的消息 ,默认值是 AUTO
		//container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		return container;
	}

	// ########################   topic queue 主题队列 配置  ####################################

	@Bean
	public TopicExchange topicExchange() {
		TopicExchange topicExchange = new TopicExchange(ExchangeConstant.TOPIC_EXCHAGE, true, false);
		return topicExchange;
	}

	@Bean
	public Queue topicQueue() {
		Queue queue = new Queue(QueueConstant.TOPIC_QUEUE, true, false, false);
		return queue;
	}

	@Bean
	public Binding topicBinding() {
		Binding binding = BindingBuilder.bind(topicQueue()).to(topicExchange())
			.with(RoutingKeyConstant.TOPIC_LAZY_ROUTING_KEY);
		return binding;
	}

	@Autowired
	private TopicMessageConsumer topicMessageConsumer;

	/**
	 * 如果不指定 bean name,默认采用方法名
	 * @return
	 */
	@Bean(name = "topicMessageListenerContainer")
	public SimpleMessageListenerContainer topicMessageListenerContainer() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory());
		container.setQueues(topicQueue());
		container.setMessageListener(topicMessageConsumer);
		// 如果设置了 MANUAL(手动),消费者那边需要手动答复,不能rabbit server 不会删除这个已经消费掉的消息 ,默认值是 AUTO
		//container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		return container;
	}


	// ########################   headers queue 头部队列 配置  ####################################

	@Bean
	public HeadersExchange headersExchange() {
		HeadersExchange headersExchange = new HeadersExchange(ExchangeConstant.HEADERS_EXCHAGE, true, false);
		return headersExchange;
	}

	@Bean
	public Queue headerQueue() {
		Queue queue = new Queue(QueueConstant.HEADERS_QUEUE, true, false, false);
		return queue;
	}
	/*

	public Binding headerBinding() {
		//BindingBuilder.bind(headerQueue()).to(headersExchange()).where()
	}
	*/


	// ########################   direct queue 一次性生成多个 配置  ####################################

	@Bean
	public List<Queue> queueList() {
		List<Queue> queueList = new ArrayList<>();
		queueList.add(new Queue("order", true, false, false));
		queueList.add(new Queue("goods", true, false, false));
		queueList.add(new Queue("count", true, false, false));
		queueList.add(new Queue("logger.info", true, false, false));
		queueList.add(new Queue("logger.warn", true, false, false));
		queueList.add(new Queue("logger.error", true, false, false));
		return queueList;
	}

	@Bean
	public List<DirectExchange> directExchangeList() {
		List<DirectExchange> exchangeList = new ArrayList<>();
		exchangeList.add(new DirectExchange("test.order.direct.exchange", true, false));
		exchangeList.add(new DirectExchange("test.goods.direct.exchange", true, false));
		exchangeList.add(new DirectExchange("test.count.direct.exchange", true, false));
		exchangeList.add(new DirectExchange("logger.info.direct.exchange", true, false));
		exchangeList.add(new DirectExchange("logger.warn.direct.exchange", true, false));
		exchangeList.add(new DirectExchange("logger.error.direct.exchange", true, false));
		return exchangeList;
	}

	@Bean
	public List<Binding> bindingList() {
		List<Binding> bindingList = new ArrayList<>();
		bindingList.add(BindingBuilder.bind(queueList().get(0)).to(directExchangeList().get(0))
			.with("test.order.routing.key"));
		bindingList.add(BindingBuilder.bind(queueList().get(3)).to(directExchangeList().get(3))
			.with("logger.info.routing.key"));
		bindingList.add(BindingBuilder.bind(queueList().get(4)).to(directExchangeList().get(4))
			.with("logger.warn.routing.key"));
		bindingList.add(BindingBuilder.bind(queueList().get(5)).to(directExchangeList().get(5))
			.with("logger.error.routing.key"));
		return bindingList;
	}


	@Autowired
	public TestMessageConverter testMessageConverter;

	/**
	 * MessageListenerAdapter
	 1.可以把一个没有实现MessageListener和ChannelAwareMessageListener接口的类适配成一个可以处理消息的处理器
	 2.默认的方法名称为：handleMessage，可以通过setDefaultListenerMethod设置新的消息处理方法
	 3.MessageListenerAdapter支持不同的队列交给不同的方法去执行。使用setQueueOrTagToMethodName方法设置，当根据queue名称没有找到匹配的方法的时候，就会交给默认的方法去处理。
	 * @return
	 */

	//@Bean
	public SimpleMessageListenerContainer messageListenerContainer() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory());
		container.setQueueNames("order","logger.info", "logger.warn", "logger.error");

		MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageHandler());
		// 设置处理器的消费消息的默认方法,如果没有设置,那么默认处理器的默认方式是handlerMessage方法
		adapter.setDefaultListenerMethod("onMessage");
		Map<String, String> queueOrTagToMethodName = new HashMap<>();
		//queueOrTagToMethodName.put("logger.info","onInfo");
		//queueOrTagToMethodName.put("logger.warn","onWarn");
		//queueOrTagToMethodName.put("logger.error","onError");
		adapter.setQueueOrTagToMethodName(queueOrTagToMethodName);

		// 指定消息处理器
		//adapter.setMessageConverter(testMessageConverter);

		// RabbitMQ自带的Jackson2JsonMessageConverter转换器
		// 如果 contentType 是json类型,那么转成json,消费端需使用Map进行接收. 否则都转换成 byte[]数组
		adapter.setMessageConverter(new Jackson2JsonMessageConverter());

		container.setMessageListener(adapter);
		return container;
	}

	/**
	 * 定义过期消费 30s queue ttl
	 * @return
	 */
	@Bean
	public Queue duanXieQueue() {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("x-message-ttl", 30000);
		Queue duanxin = new Queue("duanxin", true, false, false, arguments);
		return duanxin;
	}

	/**
	 * 设置 消息长度 x-max-length
	 * eg : 测试队列中最多只有5个消息，当第六条消息发送过来的时候，会删除最早的那条消息。队列中永远只有5条消息
	 * @return
	 */
	@Bean
	public Queue appleQueue() {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("x-max-length", 3);
		Queue apple = new Queue("apple", true, false, false, arguments);
		return apple;
	}

	/**
	 * 设置 消息字节长度 x-max-length-bytes
	 * eg : 往这个队列发送消息，第一条消息为11，第二条为2222，第三条市3333，
	 *      然后再发送的话就会将最先入队列的第一条消息删除，如果删除之后还是不够存储新的消息，依次删除第二个消息，循环如此
	 * @return
	 */
	@Bean
	public Queue bananaQueue() {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("x-max-length-bytes", 10);
		Queue banana = new Queue("banana", true, false, false, arguments);
		return banana;
	}







	// ########################   direct queue 死信队列 配置  ####################################

	/*@Bean
	public Queue repeatTradeQueue() {
		Queue queue = new Queue(QueueConstant.DIRECT_REPEAT_TRADE_QUEUE_NAME,true,false,false);
		return queue;
	}

	@Bean
	public Binding  drepeatTradeBinding() {
		return BindingBuilder.bind(repeatTradeQueue()).to(directExchange()).with(RoutingKeyConstant.DIRECT_REPEAT_TRADE_ROUTING_KEY);
	}

	@Bean
	public Queue deadLetterQueue() {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("x-dead-letter-exchange", ExchangeConstant.DIRECT_EXCHAGE);
		arguments.put("x-dead-letter-routing-key", RoutingKeyConstant.DIRECT_DEAD_LETTER_ROUTING_KEY);
		Queue queue = new Queue(QueueConstant.DIRECT_DEAD_LETTER_QUEUE_NAME,true,false,false,arguments);
		System.out.println("arguments :" + queue.getArguments());
		return queue;
	}

	@Bean
	public Binding  deadLetterBinding() {
		return BindingBuilder.bind(deadLetterQueue()).to(directExchange()).with(RoutingKeyConstant.DIRECT_DEAD_LETTER_ROUTING_KEY);
	}


	// ########################   direct queue 死信队列 配置  ####################################
	// 创建业务队列
	@Bean
	public Queue mailQueue() {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("x-dead-letter-exchange", ExchangeConstant.DEAD_LETTER_EXCHANGE); //设置死信交换机
		arguments.put("x-dead-letter-routing-key", RoutingKeyConstant.DIRECT_DEAD_MAIL_QUEUE_FAIL) ;// 设置死信 routingKey
		Queue queue = new Queue(QueueConstant.TEST_MAIL_QUEUE, true, false, false, arguments);
		return queue;
	}
	// 业务交换机
	@Bean
	public DirectExchange mailExchange() {
		DirectExchange directExchange = new DirectExchange(ExchangeConstant.MAIL_EXCHANGE, true, false);
		return directExchange;
	}
	// 绑定业务队列和交换机,并绑定routingKey
	@Bean
	public Binding mailBinding() {
		Binding binding = BindingBuilder.bind(mailQueue()).to(mailExchange())
			.with(RoutingKeyConstant.MAIL_QUEUE_ROUTING_KEY);
		return binding;
	}

	@Bean
	public DirectExchange deadExchange() {
		return new DirectExchange("dead_letter_exchange", true, false);
	}

	@Bean
	public Queue deadQueue(){
		Queue queue = new Queue("dead", true);
		return queue;
	}

	@Bean
	public Binding deadBinding() {
		return BindingBuilder.bind(deadQueue()).to(deadExchange())
			.with("mail_queue_fail");
	}


	@Autowired
	private DeadLetterMessageConsumer deadLetterMessageConsumer;

	@Bean(name = "deadMessageListenerContainer")
	public SimpleMessageListenerContainer deadMessageListenerContainer() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory());
		container.setQueues(deadLetterQueue());
		container.setMessageListener(deadLetterMessageConsumer);
		return container;
	}*/
}



