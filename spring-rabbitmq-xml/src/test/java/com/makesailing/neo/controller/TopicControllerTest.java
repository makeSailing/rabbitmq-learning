package com.makesailing.neo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.makesailing.neo.BaseControllerTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TopicController Tester.
 *
 * @author jamie
 * @since <pre>09/25/2018</pre>
 */
public class TopicControllerTest extends BaseControllerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Method: sendAmqbMsg(@RequestParam(value = "msg", defaultValue = "Hello , quick.orange.rabbit") String msg)
	 */
	@Test
	public void testSendAmqbMsg() throws Exception {
		String response = mockMvc.perform(get("/topic/sendMsg").param("msg", "Hello Worlds RabbitMQ"))
			.andExpect(status().isOk())
			.andDo(print()).andReturn().getResponse().getContentAsString();
		Assert.assertNotNull(response);
		System.out.println(response);
	}


}
