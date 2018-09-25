package com.makesailing.neo;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:spring/spring-mvc.xml", "classpath:applicationContext.xml"})
public class BaseControllerTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	/**
	 * 模拟mvc测试对象
	 */
	protected MockMvc mockMvc;


	/**
	 * 所有测试方法执行之前执行该方法
	 */
	@Before
	public void before() {
		//获取mockmvc对象实例
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
}