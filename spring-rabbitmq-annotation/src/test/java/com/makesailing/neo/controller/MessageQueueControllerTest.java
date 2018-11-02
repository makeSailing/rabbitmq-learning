package com.makesailing.neo.controller;

import static java.util.stream.Collectors.toList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alibaba.fastjson.JSON;
import com.makesailing.neo.BaseControllerTest;
import com.makesailing.neo.domain.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
		String response = mockMvc.perform(get(DIRECT_EXCHANGE + "/sendMsg").param("msg", "Hello Direct RabbitMQ"))
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

	/**
	 *
	 * Method: sendFanoutMsg(@RequestParam(value = "msg", defaultValue = "Hello , quick.orange.rabbit") String msg)
	 *
	 */
	@Test
	public void testSendFanoutMsg() throws Exception {
		String response = mockMvc.perform(get(FANOUT_EXCHANGE + "/sendMsg").param("msg", "Hello Fanout RabbitMQ"))
			.andExpect(status().isOk())
			.andDo(print()).andReturn().getResponse().getContentAsString();
		Assert.assertNotNull(response);
		System.out.println(response);
	}

	/**
	 *
	 * Method: sendTopicMsg(@RequestParam(value = "msg", defaultValue = "Hello , quick.orange.rabbit") String msg)
	 *
	 */
	@Test
	public void testSendTopicMsg() throws Exception {
		String response = mockMvc.perform(get(TOPIC_EXCHANGE + "/sendMsg").param("msg", "Hello Topic RabbitMQ"))
			.andExpect(status().isOk())
			.andDo(print()).andReturn().getResponse().getContentAsString();
		Assert.assertNotNull(response);
		System.out.println(response);
	}


	@Test
	public void teset() {
			List<String> list1 = new ArrayList();
			list1.add("1111");
			list1.add("2222");
			list1.add("3333");

			List<String> list2 = new ArrayList();
			list2.add("3333");
			list2.add("4444");
			list2.add("5555");

			// 交集
			List<String> intersection = list1.stream().filter(item -> list2.contains(item)).collect(toList());
			System.out.println("---得到交集 intersection---");
			intersection.parallelStream().forEach(System.out :: println);

			// 差集 (list1 - list2)
			List<String> reduce1 = list1.stream().filter(item -> !list2.contains(item)).collect(toList());
			System.out.println("---得到差集 reduce1 (list1 - list2)---");
			reduce1.parallelStream().forEach(System.out :: println);

			// 差集 (list2 - list1)
			List<String> reduce2 = list2.stream().filter(item -> !list1.contains(item)).collect(toList());
			System.out.println("---得到差集 reduce2 (list2 - list1)---");
			reduce2.parallelStream().forEach(System.out :: println);

			// 并集
			List<String> listAll = list1.parallelStream().collect(toList());
			List<String> listAll2 = list2.parallelStream().collect(toList());
			listAll.addAll(listAll2);
			System.out.println("---得到并集 listAll---");
			listAll.parallelStream().forEach(System.out :: println);

			// 去重并集
			List<String> listAllDistinct = listAll.stream().distinct().collect(toList());
			System.out.println("---得到去重并集 listAllDistinct---");
			listAllDistinct.parallelStream().forEach(System.out :: println);

			System.out.println("---原来的List1---");
			list1.parallelStream().forEach(System.out :: println);
			System.out.println("---原来的List2---");
			list2.parallelStream().forEach(System.out :: println);

			// 一般有filter 操作时，不用并行流parallelStream ,如果用的话可能会导致线程安全问题

	}

	@Test
	public void test3() {
	/*	String[] strArray = new String[2];
		List list = Arrays.asList(strArray);
//对转换后的list插入一条数据
		list.add("1");
		System.out.println(list);*/

		String[] arrays = {"a", "b", "c"};
		List<String> listStrings = Stream.of(arrays).collect(Collectors.toList());
		listStrings.add("d");
		System.out.println(listStrings);

		//List<String> list = Arrays.asList(arrays);
		//list.add("d");

		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("c");
		//String[] strings = list.stream().toArray(String[]::new);

		String[] strings = list.toArray(new String[list.size()]);
		Object[] objects = list.toArray();
		System.out.println(strings);
		System.out.println(objects);

	}

	@Test
	public void test4(){
		List<Integer> list = new ArrayList<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		List<Integer> integerList = list.subList(1, 3);
		System.out.println(integerList);
	}




}
