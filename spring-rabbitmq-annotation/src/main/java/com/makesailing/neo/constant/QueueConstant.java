package com.makesailing.neo.constant;

/**
 * #
 *
 * @author jamie.li
 * @date 2018/9/26 17:37
 */
public class QueueConstant {

	/**
	 * direct queue
	 */
	public static final String TEST_DIRECT_QUEUE = "test.direct.queue";

	/**
	 * DLX QUEUE
	 */
	public static final String DIRECT_DEAD_LETTER_QUEUE_NAME = "kshop.dead.letter.queue";

	/**
	 * DLX repeat QUEUE 死信转发队列
	 */
	public static final String DIRECT_REPEAT_TRADE_QUEUE_NAME = "kshop.repeat.trade.queue";
	/**
	 * 邮件队列queue
	 */
	public static final String TEST_MAIL_QUEUE = "test.mail.queue";
}


