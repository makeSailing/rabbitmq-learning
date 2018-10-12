package com.makesailing.neo.config;

import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractJsonMessageConverter;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.MessageConversionException;

/**
 * #
 *
 * @author jamie
 * @date 2018/9/26 10:55
 */
@Slf4j
public class FastJsonMessageConverter extends AbstractJsonMessageConverter {


	private static ClassMapper classMapper =  new DefaultClassMapper();


	public FastJsonMessageConverter() {
		super();
	}

	@Override
	protected Message createMessage(Object object, MessageProperties messageProperties) {
		log.info(" createMessage method info object [{}] , messageProperties [{}]", object, messageProperties);
		byte[] bytes = null;
		try {
			String jsonString = JSONObject.toJSONString(object);
			bytes = jsonString.getBytes(this.getDefaultCharset());
		}
		catch (IOException e) {
			throw new MessageConversionException(
				"Failed to convert Message content", e);
		}
		messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
		messageProperties.setContentEncoding(this.getDefaultCharset());
		if (bytes != null) {
			messageProperties.setContentLength(bytes.length);
		}
		classMapper.fromClass(object.getClass(),messageProperties);
		return new Message(bytes, messageProperties);
	}

	@Override
	public Object fromMessage(Message message) throws MessageConversionException {
		log.info(" fromMessage method message [{}]", message);
		Object content = null;
		MessageProperties properties = message.getMessageProperties();
		if (properties != null) {
			String contentType = properties.getContentType();
			if (contentType != null && contentType.contains("json")) {
				String encoding = properties.getContentEncoding();
				if (encoding == null) {
					encoding = this.getDefaultCharset();
				}
				try {
					Class<?> targetClass = classMapper.toClass(
						message.getMessageProperties());
					content = convertBytesToObject(message.getBody(),
						encoding, targetClass);
				} catch (IOException e) {
					throw new MessageConversionException(
						"Failed to convert Message content", e);
				}
			} else {
				log.warn("Could not convert incoming message with content-type ["
					+ contentType + "]");
			}
		}
		if (content == null) {
			content = message.getBody();
		}
		return content;
	}

	private Object convertBytesToObject(byte[] body, String encoding,
		Class<?> clazz) throws UnsupportedEncodingException {
		String contentAsString = new String(body, encoding);
		return JSONObject.parseObject(contentAsString, clazz);
	}
}


