package com.makesailing.neo.constant;

/**
 * #
 *
 * @author jamie.li
 * @date 2018/9/26 18:14
 */
public class RoutingKeyConstant {

	public static final String DIRECT_ROUTING_KEY = "test.direct.routing.key";

	/**
	 * 死信队列 routingKey
	 */
	public static final String DIRECT_DEAD_LETTER_ROUTING_KEY = "test.direct.dead.letter.routing.key";
	/**
	 * 死信队列后重发 routingKey
	 */
	public static final String DIRECT_REPEAT_TRADE_ROUTING_KEy= "test.direct.repeat.trade.routing.key";
}


