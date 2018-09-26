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
 * @date 2018/9/25 17:30
 */
@RestController
@RequestMapping("/topic")
public class TopicController {

	@Autowired
	private AmqpTemplate amqpTemplate;

	@Value("${test.topic.exchange}")
	private String exchange;

	@GetMapping("/sendMsg")
	public String sendAmqbMsg(@RequestParam(value = "msg", defaultValue = "Hello , quick.orange.rabbit") String msg) {
		amqpTemplate.convertAndSend(exchange, "quick.orange.rabbit", msg);
		return "success";
	}

}


