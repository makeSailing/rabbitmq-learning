package com.makesailing.neo.enums;

/**
 * #
 *
 * @author jamie
 * @date 2018/9/12 13:50
 */
public enum QueueEnum {
	/**
	 * 消息通知队列
	 */
	MESSAGE_QUEUE("message.center.direct", "message.center.create", "message.center.create"),

	/**
	 * 消息通知 TTL 队列
	 */
	MESSAGE_TTL_QUEUE("message.center.topic.ttl","message.center.create.ttl","message.center.create.ttl")
	;

	/**
	 * 交换名称
	 */
	private String exchange;

	/**
	 * 队列名称
	 */
	private String name;
	/**
	 * 路由键
	 */
	private String routingKey;

	QueueEnum(String exchange, String name, String routingKey) {
		this.exchange = exchange;
		this.name = name;
		this.routingKey = routingKey;
	}

	public String getExchange() {
		return exchange;
	}

	public String getName() {
		return name;
	}

	public String getRoutingKey() {
		return routingKey;
	}
}
