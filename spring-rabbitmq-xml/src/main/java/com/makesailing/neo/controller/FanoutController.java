package com.makesailing.neo.controller;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * #
 *
 * @author jamie
 * @date 2018/9/26 10:16
 */
@RestController
@RequestMapping("/fanout")
public class FanoutController {

	@Autowired
	private AmqpTemplate amqpTemplate;

	@Value("${test.fanout.exchange}")
	private String exchange;

	@GetMapping("/register")
	public String userRegister(
		@RequestParam(value = "user", defaultValue = "Hello , user register success") String msg) {
		// 第二个参数就是 routingKey 路由键,使用 "" 默认routingKey ,但是不能为null
		amqpTemplate.convertAndSend(exchange, "", msg);
		// fanout 扇形交换机会忽然其 routingKey,所以其指定routingKey也不生效
		//amqpTemplate.convertAndSend("test.fanout.exchange", "quick.orange.rabbit", msg);
		return "success";
	}
}


