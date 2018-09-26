package com.makesailing.neo.controller; 

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.makesailing.neo.BaseControllerTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After; 

/** 
* FanoutController Tester. 
* 
* @author <a href="mailto:jamie.li@wolaidai.com">jamie.li</a> 
* @since <pre>09/26/2018</pre> 
*/ 
public class FanoutControllerTest extends BaseControllerTest {

    @Before
    public void setUp() throws Exception { 
    } 
    
    @After
    public void tearDown() throws Exception { 
    } 
    
    /** 
    * 
    * Method: sendAmqbMsg(@RequestParam(value = "user", defaultValue = "Hello , user register success") String msg) 
    * 
    */ 
    @Test
    public void testSendAmqbMsg() throws Exception {
        String response = mockMvc.perform(get("/fanout/register").param("user", "Hello World User Register Success"))
            .andExpect(status().isOk())
            .andDo(print()).andReturn().getResponse().getContentAsString();
        Assert.assertNotNull(response);
        System.out.println(response);
    } 
    
        
    } 
