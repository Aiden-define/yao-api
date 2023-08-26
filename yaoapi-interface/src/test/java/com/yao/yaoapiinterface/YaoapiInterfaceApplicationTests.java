package com.yao.yaoapiinterface;

import com.yao.yaoapiclientsdk.client.YaoApiClient;
import com.yao.yaoapiclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class YaoapiInterfaceApplicationTests {

    @Resource
    public YaoApiClient yaoApiClient;
    /*@Test
    void test() {
        User user = new User();
        user.setUsername("yjh");
        String post = yaoApiClient.getNameByPost(user);
        System.out.println(post);
    }*/

}
