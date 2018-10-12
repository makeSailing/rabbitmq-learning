package com.makesailing.neo;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
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


		rabbitTemplate.convertAndSend("logger.info.direct.exchange","logger.info.routing.key", MESSAGE_INFO);
		rabbitTemplate.convertAndSend("test.order.direct.exchange","test.order.routing.key", MESSAGE_INFO);


		TimeUnit.SECONDS.sleep(30);

		context.close();

	}
}


