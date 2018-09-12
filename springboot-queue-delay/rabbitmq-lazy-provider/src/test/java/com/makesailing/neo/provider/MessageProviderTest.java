package com.makesailing.neo.provider;

import com.makesailing.neo.RabbitMqLazyProviderApplication;
import com.makesailing.neo.enums.QueueEnum;
import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * MessageProvider Tester.
 *
 * @author jamie
 * @since <pre>09/12/2018</pre>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RabbitMqLazyProviderApplication.class)
public class MessageProviderTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Autowired
	private MessageProvider messageProvider;

	/**
	 * Method: sendMessage(Object messageContent, String exchange, String routingKey, final Long dalayTimes)
	 */
	@Test
	public void testSendMessage() throws Exception {
		messageProvider.sendMessage("测试延迟消费,写入时间" + LocalDateTime.now(), QueueEnum.MESSAGE_TTL_QUEUE.getExchange(),
			QueueEnum.MESSAGE_TTL_QUEUE.getRoutingKey(), 10000L);
	}


}
