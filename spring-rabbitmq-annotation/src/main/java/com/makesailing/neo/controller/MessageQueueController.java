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
@RequestMapping("/rabbit")
public class MessageQueueController {

	public static final String DIRECT_EXCHANGE = "/direct";
	public static final String FANOUT_EXCHANGE = "/fanout";
	public static final String TOPIC_EXCHANGE = "/topic";

	@Autowired
	private MessageQueueService messageQueueService;

	@GetMapping(DIRECT_EXCHANGE + "/sendMsg")
	public String sendDirectMsg(@RequestParam(value = "msg", defaultValue = "Hello , quick.orange.rabbit") String msg) {
		String[] routingKeys = {RoutingKeyConstant.DIRECT_ROUTING_KEY, RoutingKeyConstant.DIRECT_DEAD_MAIL_QUEUE_FAIL,
			RoutingKeyConstant.MAIL_QUEUE_ROUTING_KEY};
		for (int i = 0; i < 10; i++) {
			messageQueueService.send(ExchangeConstant.DIRECT_EXCHAGE, routingKeys[i % 3], msg + " >>> " + i);
		}
		// 休眠 3s,方便查看日志,实际情况不用
		try {
			Thread.sleep(1000 * 3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "success";
	}

	@PostMapping(DIRECT_EXCHANGE + "/sendUserMsg")
	public String sendDirectUserMsg(@RequestBody User user) {
		messageQueueService.send(ExchangeConstant.DIRECT_EXCHAGE, RoutingKeyConstant.DIRECT_ROUTING_KEY, user);
		return "success";
	}

	@GetMapping(DIRECT_EXCHANGE + "/sendDeadLetterMsg")
	public String sendDeadLetterMsg(
		@RequestParam(value = "msg", defaultValue = "Hello , quick.orange.rabbit") String msg) {
		messageQueueService.sendDeadLetterMsg(QueueConstant.DIRECT_DEAD_LETTER_QUEUE_NAME, msg, 6000);
		return "success";
	}


	@GetMapping(FANOUT_EXCHANGE + "/sendMsg")
	public String sendFanoutMsg(@RequestParam(value = "msg", defaultValue = "Hello , quick.orange.rabbit") String msg) {
		String[] routingKeys = {RoutingKeyConstant.DIRECT_ROUTING_KEY,RoutingKeyConstant.FANOUT_ROUTING_KEY, RoutingKeyConstant.DIRECT_DEAD_MAIL_QUEUE_FAIL,
			RoutingKeyConstant.MAIL_QUEUE_ROUTING_KEY};
		for (int i = 0; i < 30; i++) {
			messageQueueService.send(ExchangeConstant.FANOUT_EXCHAGE, routingKeys[i % 3], msg + " >>> " + i);
		}
		// 休眠 3s,方便查看日志,实际情况不用
		try {
			Thread.sleep(1000 * 3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "success";
	}
}


