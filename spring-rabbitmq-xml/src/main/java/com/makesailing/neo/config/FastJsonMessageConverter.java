package com.makesailing.neo.config;

import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractMessageConverter;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.MessageConversionException;

/**
 * #
 *
 * @author jamie
 * @date 2018/9/26 10:55
 */
public class FastJsonMessageConverter extends AbstractMessageConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(FastJsonMessageConverter.class);

	private static ClassMapper classMapper =  new DefaultClassMapper();

	public static final String DEFAULT_CHARSET = "UTF-8";

	private volatile String defaultCharset = DEFAULT_CHARSET;

	public FastJsonMessageConverter() {
		super();
	}

	@Override
	protected Message createMessage(Object object, MessageProperties messageProperties) {
		byte[] bytes = null;
		try {
			String jsonString = JSONObject.toJSONString(object);
			bytes = jsonString.getBytes(defaultCharset);
		}
		catch (IOException e) {
			throw new MessageConversionException(
				"Failed to convert Message content", e);
		}
		messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
		messageProperties.setContentEncoding(defaultCharset);
		if (bytes != null) {
			messageProperties.setContentLength(bytes.length);
		}
		classMapper.fromClass(object.getClass(),messageProperties);
		return new Message(bytes, messageProperties);
	}

	@Override
	public Object fromMessage(Message message) throws MessageConversionException {
		Object content = null;
		MessageProperties properties = message.getMessageProperties();
		if (properties != null) {
			String contentType = properties.getContentType();
			if (contentType != null && contentType.contains("json")) {
				String encoding = properties.getContentEncoding();
				if (encoding == null) {
					encoding = defaultCharset;
				}
				try {
					Class<?> targetClass = classMapper.toClass(
						message.getMessageProperties());
					content = convertBytesToObject(message.getBody(),
						encoding, targetClass);
				}
				catch (IOException e) {
					throw new MessageConversionException(
						"Failed to convert Message content", e);
				}
			}
			else {
				LOGGER.warn("Could not convert incoming message with content-type ["
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


