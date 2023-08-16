package com.yao.yaoapiinterface.controller;



import com.yao.yaoapiclientsdk.model.User;
import org.springframework.web.bind.annotation.*;

/**
 * @author DH
 * @version 1.0
 * @description API名称
 * @date 2023/4/27 12:43
 */
@RestController
@RequestMapping("/name")
public class NameController {

    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user){
       return "你的名字是" + user.getUsername();
    }


}
