package com.makesailing.neo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alibaba.fastjson.JSON;
import com.makesailing.neo.BaseControllerTest;
import com.makesailing.neo.constant.Urls;
import com.makesailing.neo.domain.User;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;

/**
 * UserController Tester.
 *
 * @author jamie.li
 * @since <pre>09/20/2018</pre>
 */
public class UserControllerTest extends BaseControllerTest {


	/**
	 * Method: userRegister(@RequestBody UserEntity userEntity)
	 */
	@Test
	public void testUserRegister() throws Exception {
		User user = new User();
		user.setEmail("1234@qq.com");
		user.setUsername("jack");
		user.setPassword("123456");
		user.setRole("root");
		user.setStatus(1);
		user.setRegtime(new Date());
		//user.setRegip("127.0.0.1");

		String content = JSON.toJSONString(user);
		String response = mockMvc.perform(
			post(Urls.User.SAVE_USER).contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		Assert.assertNotNull(response);
		Assert.assertNotEquals(response, "");
		System.out.println(response);

	}


}
