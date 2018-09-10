import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.alibaba.fastjson.JSON;
import com.makesailing.neo.domain.UserEntity;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
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

     mockMvc.perform( post("/user/save")
        .contentType(MediaType.APPLICATION_JSON)
        .content(JSON.toJSONString(userEntity)))
        .andDo(MockMvcResultHandlers.log())
        .andReturn();
  }

  /**
   * 测试用户批量添加
   * @throws Exception
   */
  @Test
  public void testBatchUserAdd() throws Exception
  {
    for (int i = 0 ; i < 100 ; i++) {
      //创建用户注册线程
      Thread thread = new Thread(new BatchRabbitTester(i));
      //启动线程
      thread.start();
    }
    //等待线程执行完成
    Thread.sleep(2000);
  }


  /**
   * 批量添加用户线程测试类
   * run方法发送用户注册请求
   */
  class BatchRabbitTester implements Runnable
  {
    private int index;
    public BatchRabbitTester() { }

    public BatchRabbitTester(int index) {
      this.index = index;
    }


    @Override
    public void run() {
      try {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserName("makesailing");
        userEntity.setName("jamie");
        userEntity.setAge(18);

        mockMvc.perform( post("/user/save")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON.toJSONString(userEntity)))
            .andDo(MockMvcResultHandlers.log())
            .andReturn();
      }catch (Exception e){
        e.printStackTrace();
      }

    }
  }

}
