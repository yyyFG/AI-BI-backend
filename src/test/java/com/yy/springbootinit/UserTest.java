package com.yy.springbootinit;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


/**
 *  用户服务测试
 *
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserTest {


//    @Resource
//    private UserService userService;
//
//    @Test
//    public void registerUser(){
//        String userAccount = "yyyy";
//        String userPassword = "";
//        HttpServletRequest request = null;
//        LoginUserVO result = userService.userLogin(userAccount, userPassword,request);
//        Assertions.assertEquals(-1,result);
//
//        userAccount = "yy";
//        result = userService.userLogin(userAccount, userPassword,request);
//        Assertions.assertEquals(-1,result);
//
//        userAccount = "yyyy";
//        userPassword = "123456";
//        result = userService.userLogin(userAccount, userPassword,request);
//        Assertions.assertEquals(-1,result);
//
//        userAccount = "yy yy";
//        userPassword = "12345678";
//        result = userService.userLogin(userAccount, userPassword,request);
//        Assertions.assertEquals(-1,result);
//
//        userAccount = "ysensei";
//        result = userService.userLogin(userAccount, userPassword,request);
//        Assertions.assertEquals(-1,result);
//
//        userAccount = "yyyyy";
//        result = userService.userLogin(userAccount, userPassword,request);
//        Assertions.assertTrue(result.getId() > 0);
//
//    }
}
