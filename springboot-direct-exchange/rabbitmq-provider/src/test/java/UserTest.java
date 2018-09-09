import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alibaba.druid.support.json.JSONUtils;
import com.makesailing.neo.domain.UserEntity;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 19:07
 */
public class UserTest extends BaseTest {
  /**
   * 模拟mvc测试对象
   */
  private MockMvc mockMvc;

  /**
   * web项目上下文
   */
  @Autowired
  private WebApplicationContext webApplicationContext;

  /**
   * 所有测试方法执行之前执行该方法
   */
  @Before
  public void before() {
    //获取mockmvc对象实例
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  /**
   * 测试添加用户
   */
  @Test
  public void testUserAdd() throws Exception {
    UserEntity userEntity = new UserEntity();
    userEntity.setUserName("makesailing");
    userEntity.setName("jamie");
    userEntity.setAge(18);

    String responseString = mockMvc.perform( post("/user/save")
        .contentType(MediaType.APPLICATION_JSON)
        .content(JSONUtils.toJSONString(userEntity)))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
  }
}
