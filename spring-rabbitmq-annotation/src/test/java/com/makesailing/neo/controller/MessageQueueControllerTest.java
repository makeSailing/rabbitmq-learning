package com.makesailing.neo.controller;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alibaba.fastjson.JSON;
import com.makesailing.neo.BaseControllerTest;
import com.makesailing.neo.domain.User;
import java.util.Date;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

/**
 * DirectController Tester.
 *
 * @author jamie.li
 * @since <pre>09/27/2018</pre>
 */
public class MessageQueueControllerTest extends BaseControllerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	public static final String ROOT = "/rabbit";

	public static final String DIRECT_EXCHANGE = ROOT+ "/direct";

	public static final String FANOUT_EXCHANGE = ROOT+ "/fanout";

	public static final String TOPIC_EXCHANGE = ROOT+ "/topic";

	/**
	 * Method: sendMsg(@RequestParam(value = "msg", defaultValue = "Hello , quick.orange.rabbit") String msg)
	 */
	@Test
	public void testSendMsg() throws Exception {
		String response = mockMvc.perform(get(DIRECT_EXCHANGE + "/sendMsg").param("msg", "Hello Worlds RabbitMQ"))
			.andExpect(status().isOk())
			.andDo(print()).andReturn().getResponse().getContentAsString();
		Assert.assertNotNull(response);
		System.out.println(response);
	}

	/**
	 *
	 * Method: sendUserMsg(@RequestBody User user)
	 *
	 */
	@Test
	public void testSendUserMsg() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setEmail("345@qq.com");
		user.setUsername("jamie");
		user.setPassword("123456");
		user.setRole("root");
		user.setStatus(1);
		user.setRegtime(new Date());
		user.setRegip("127.0.0.1");

		String userInfo = JSON.toJSONString(user);

		String response = mockMvc.perform(post(DIRECT_EXCHANGE + "/direct/sendUserMsg").contentType(MediaType.APPLICATION_JSON_UTF8).content(userInfo))
			.andExpect(status().isOk())
			.andDo(print()).andReturn().getResponse().getContentAsString();
		Assert.assertNotNull(response);
		System.out.println(response);
	}


	/**
	 *
	 * Method: sendDeadLetterMsg(@RequestParam(value = "msg", defaultValue = "Hello , quick.orange.rabbit") String msg)
	 *
	 */
	@Test
	public void testSendDeadLetterMsg() throws Exception {
		String response = mockMvc.perform(get(DIRECT_EXCHANGE + "/direct/sendDeadLetterMsg").param("msg", "测试延迟发送消息"))
			.andExpect(status().isOk())
			.andDo(print()).andReturn().getResponse().getContentAsString();
		Assert.assertNotNull(response);
		System.out.println(response);
	}




}
