package com.makesailing.neo.queue.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * #
 *
 *jamie.li
 * @date 2018/10/12 17:07
 */
@Slf4j
@Component
@RabbitListener(queues = "order")
public class AnnotationMessageHandler {

	/**
	 * 不能同时监听同一个队列,会报错
	 */

	/**
	 * @RabbitListener和@RabbitHandler搭配使用
	 @RabbitListener可以标注在类上面，当使用在类上面的时候，需要配合@RabbitHandler注解一起使用，
	 @RabbitListener标注在类上面表示当有收到消息的时候，就交给带有@RabbitHandler的方法处理，具体找哪个方法处理，需要跟进MessageConverter转换后的
	 */

	// 如果消费属性 content_type 为 text/pain,那么会将消息转换成每个字符的int类型,会报 NumberFormatException 异常
	//@RabbitListener(queues = "order")
	@RabbitHandler
	public void handlerMessage(byte[] message) {
		log.info("----- handleMessage byte[] ----- >>> [{}]", new String(message));
	}
	//
	//@RabbitListener(queues = "order")
	@RabbitHandler
	public void handlerMessage(String message) {
		log.info("----- handleMessage String ----- >>> [{}]", message);
	}

	// 此时不管属性中有没有 content_type属性都能进行接收
	//@RabbitListener(queues = "order")
	@RabbitHandler
	public void handlerMessage(Message message) {
		log.info("----- handleMessage Message ----- >>> [{}]", new String(message.getBody()));

	}


}


