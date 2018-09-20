package com.makesailing.neo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.alibaba.fastjson.JSON;
import com.makesailing.neo.domain.UserEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** 
* UserController Tester. 
* 
* @author jamie.li
* @since <pre>09/20/2018</pre> 
*/
@RunWith(SpringJUnit4ClassRunner.class)  //此处调用Spring单元测试类
@WebAppConfiguration    //调用javaWEB的组件，比如自动注入ServletContext Bean等等
@ContextConfiguration(locations = {"classpath*:/spring/applicationContext.xml"})//加载Spring配置文件
public class UserControllerTest extends AbstractJUnit4SpringContextTests {

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

    @After
    public void tearDown() throws Exception { 
    } 
    
    /** 
    * 
    * Method: userRegister(@RequestBody UserEntity userEntity) 
    * 
    */ 
    @Test
    public void testUserRegister() throws Exception {
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
    
        
    } 
