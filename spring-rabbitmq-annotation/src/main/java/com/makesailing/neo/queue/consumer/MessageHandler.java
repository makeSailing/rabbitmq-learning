package com.makesailing.neo.queue.consumer;

import com.alibaba.fastjson.JSON;
import com.makesailing.neo.common.MyBody;
import com.makesailing.neo.domain.User;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageHandler {

	/**
	 * 没有设置默认的处理方法的时候，方法名是handleMessage
	 */
	public void handleMessage(byte[] message) {
		log.info("----- handleMessage byte[] ----- >>> [{}]", new String(message));
	}

	/**
	 * 通过设置setDefaultListenerMethod时候指定的方法名
	 */
	public void onMessage(byte[] message) {
		log.info("----- onMessage byte[]----- >>> [{}]", new String(message));
	}

	public void onMessage(String message) {
		log.info("----- onMessage String----- >>> [{}]", message);
	}

	/**
	 * 使用自定义MyBody
	 * @param message
	 */
	public void onMessage(MyBody message) {
		log.info("----- onMessage MyBody----- >>> [{}]", message);
	}

	/**
	 * Json类型数据使用Map进行接收
	 * @param message
	 */
	public void onMessage(Map message) {
		log.info("----- onMessage Map----- >>> [{}]", message);
	}

	/**
	 * List Json类型数据使用List进行接收
	 * @param message
	 */
	public void onMessage(List message) {
		log.info("----- onMessage List----- >>> [{}]", message);
	}

	/**
	 * 转换成相对应的对象
	 * @param message
	 */
	public void onMessage(User message) {
		log.info("----- onMessage User----- >>> [{}]", JSON.toJSONString(message));
	}


	/**
	 * 以下指定不同的队列不同的处理方法名
	 */
	public void onInfo(byte[] message) {
		log.info("----- onInfo byte[] ----- >>> [{}]", new String(message));
	}

	public void onWarn(byte[] message) {
		log.info("----- onWarn byte[] ----- >>> [{}]", new String(message));
	}

	public void onError(byte[] message) {
		log.info("----- onError byte[] ----- >>> [{}]", new String(message));
	}

	public void onInfo(String message) {
		log.info("----- onInfo String ----- >>> [{}]", message);
	}

	public void onWarn(String message) {
		log.info("----- onWarn String ----- >>> [{}]", message);
	}

	public void onError(String message) {
		log.info("----- onError String ----- >>> [{}]", message);
	}

}
