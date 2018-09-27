package com.makesailing.neo.controller;

import com.makesailing.neo.constant.ExchangeConstant;
import com.makesailing.neo.constant.QueueConstant;
import com.makesailing.neo.constant.RoutingKeyConstant;
import com.makesailing.neo.domain.User;
import com.makesailing.neo.queue.service.MessageQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * #
 *
 * @author jamie.li
 * @date 2018/9/27 11:15
 */
@RestController
@RequestMapping("/rabbit/direct")
public class MessageQueueController {

	@Autowired
	private MessageQueueService messageQueueService;

	@GetMapping("/sendMsg")
	public String sendMsg(@RequestParam(value = "msg", defaultValue = "Hello , quick.orange.rabbit") String msg) {
		messageQueueService.send(ExchangeConstant.DIRECT_EXCHAGE, RoutingKeyConstant.DIRECT_ROUTING_KEY, msg);
		return "success";
	}

	@PostMapping("/sendUserMsg")
	public String sendUserMsg(@RequestBody User user) {
		messageQueueService.send(ExchangeConstant.DIRECT_EXCHAGE, RoutingKeyConstant.DIRECT_ROUTING_KEY, user);
		return "success";
	}

	@GetMapping("/sendDeadLetterMsg")
	public String sendDeadLetterMsg(@RequestParam(value = "msg", defaultValue = "Hello , quick.orange.rabbit") String msg) {
		messageQueueService.sendDeadLetterMsg(QueueConstant.DIRECT_DEAD_LETTER_QUEUE_NAME, msg, 60000);
		return "success";
	}
}


