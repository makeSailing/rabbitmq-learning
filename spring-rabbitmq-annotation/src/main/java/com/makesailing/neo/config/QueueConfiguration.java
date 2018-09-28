package com.makesailing.neo.config;

import com.makesailing.neo.constant.ExchangeConstant;
import com.makesailing.neo.constant.QueueConstant;
import com.makesailing.neo.constant.RoutingKeyConstant;
import com.makesailing.neo.queue.consumer.DeadLetterMessageConsumer;
import com.makesailing.neo.queue.consumer.DirectMessageConsumer;
import com.makesailing.neo.queue.consumer.FanoutMessageConsumer;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
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
		Queue queue = new Queue(QueueConstant.TEST_DIRECT_QUEUE, true, false, false);
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
		Queue queue = new Queue(QueueConstant.TEST_FANOUT_QUEUE, true, false, false);
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

	// ########################   direct queue 死信队列 配置  ####################################

	@Bean
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
	}
}



