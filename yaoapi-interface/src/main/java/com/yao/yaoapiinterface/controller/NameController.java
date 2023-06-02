package com.yao.yaoapiinterface.controller;



import com.yao.yaoapiclientsdk.model.User;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

/**
 * @author DH
 * @version 1.0
 * @description API名称
 * @date 2023/4/27 12:43
 */
@RestController
@RequestMapping("/name")
public class NameController {

    @GetMapping("/")
    public String getNameByGet(String name){
        return "Get 你的名字是" + name;
    }
    @PostMapping("/")
    public String getNameByPost(@RequestParam String name){
        return "Post 你的名字是" + name;
    }
    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user){
       return "Post 用户名名字是" + user.getUsername();
    }


}
