package com.yao.project.service;


import com.yao.common.model.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 用户服务测试
 *
 * @author DH
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void testAddUser() {
        User user = new User();
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        boolean result = userService.updateById(user);
        Assertions.assertTrue(result);
    }

    @Test
    void testDeleteUser() {
        boolean result = userService.removeById(1L);
        Assertions.assertTrue(result);
    }

    @Test
    void testGetUser() {
        User user = userService.getById(1L);
        Assertions.assertNotNull(user);
    }

    @Test
    void userRegister() {
        String userAccount = "18270122455";
        String userPassword = "yjh12345678";
        String checkPassword = "yjh12345678";
        try {
           // long result = userService.userRegister(userAccount, userPassword, checkPassword);
           /* Assertions.assertEquals(-1, result);
            userAccount = "yu";
            result = userService.userRegister(userAccount, userPassword, checkPassword);
            Assertions.assertEquals(-1, result);
            userAccount = "DH";
            userPassword = "123456";
            result = userService.userRegister(userAccount, userPassword, checkPassword);
            Assertions.assertEquals(-1, result);
            userAccount = "ydani";
            userPassword = "12345678";
            result = userService.userRegister(userAccount, userPassword, checkPassword);
            Assertions.assertEquals(-1, result);
            checkPassword = "123456789";
            result = userService.userRegister(userAccount, userPassword, checkPassword);
            Assertions.assertEquals(-1, result);
            userAccount = "DH1";
            checkPassword = "12345678";
            result = userService.userRegister(userAccount, userPassword, checkPassword);
            Assertions.assertEquals(-1, result);
            userAccount = "DH";
            result = userService.userRegister(userAccount, userPassword, checkPassword);
            Assertions.assertEquals(-1, result);*/
        } catch (Exception e) {

        }
    }
}
