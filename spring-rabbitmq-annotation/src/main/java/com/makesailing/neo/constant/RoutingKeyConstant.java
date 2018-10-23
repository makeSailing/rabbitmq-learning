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
	public static final String DIRECT_REPEAT_TRADE_ROUTING_KEY = "test.direct.repeat.trade.routing.key";

	/**
	 * 邮件死信队列 routingKey
	 */
	public static final String DIRECT_DEAD_MAIL_QUEUE_FAIL = "test.direct.dead.mail.queue.fail";


	public static final String MAIL_QUEUE_ROUTING_KEY = "test.mail.queue.routing.key";

	/**
	 * 广播队列 routingkey
	 */
	public static final String FANOUT_ROUTING_KEY = "test.fanout.routing.key";

	/**
	 * 主题队列
	 */
	public static final String TOPIC_ROUTING_KEY = "topic.orange.rabbit";

	public static final String TOPIC_ORANGE_ROUTING_KEY = "*.orange.*";

	public static final String TOPIC_RABBIT_ROUTING_KEY = "*.*.rabbit";

	public static final String TOPIC_LAZY_ROUTING_KEY = "lazy.#";


	// =================== 死信队列 start ==============================

	public static final String DEAD_LETTER_ROUTING_KEY = "test.dead.letter.routing.key";

	public static final String ORANGE_ROUTING_KEY = "test.orange.routing.key";

	public static final String PEACH_ROUTING_KEY = "test.peach.routing.key";

	// =================== 死信队列 end ==============================



}


