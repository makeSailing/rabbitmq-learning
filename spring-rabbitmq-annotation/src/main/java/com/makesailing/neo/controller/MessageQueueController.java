package com.makesailing.neo.controller;

import com.makesailing.neo.constant.ExchangeConstant;
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
@RequestMapping("/rabbit")
public class MessageQueueController {

	@Autowired
	private MessageQueueService messageQueueService;

	@GetMapping("/direct/sendMsg")
	public String sendMsg(@RequestParam(value = "msg", defaultValue = "Hello , quick.orange.rabbit") String msg) {
		messageQueueService.send(ExchangeConstant.DIRECT_EXCHAGE, RoutingKeyConstant.DIRECT_ROUTING_KEY, msg);
		return "success";
	}

	@PostMapping("/direct/sendUserMsg")
	public String sendUserMsg(@RequestBody User user) {
		messageQueueService.send(ExchangeConstant.DIRECT_EXCHAGE, RoutingKeyConstant.DIRECT_ROUTING_KEY, user);
		return "success";
	}
}


