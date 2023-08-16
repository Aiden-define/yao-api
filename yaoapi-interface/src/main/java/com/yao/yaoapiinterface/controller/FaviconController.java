package com.yao.yaoapiinterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.yao.yaoapiclientsdk.model.City;
import com.yao.yaoapiclientsdk.model.UrlApi;
import com.yao.yaoapiinterface.entity.Lives;
import com.yao.yaoapiinterface.entity.WeatherResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;

/**
 * @author DH
 * @version 1.0
 * @description 获取网站的favicon
 * @date 2023/8/16 18:27
 */
@RestController
@RequestMapping("/favicon")
public class FaviconController {
    private static final String PNG = ".png";
    @PostMapping("/post")
    public String getFaviconByPost(@RequestBody UrlApi urlApi) {
        String url = "https://api.iowen.cn/favicon/";
        String urlApiUrl = urlApi.getUrl();
        String split = urlApiUrl.split("://")[1];
        String newUrl = url + split + PNG;
        HttpResponse execute = HttpRequest.get(newUrl).execute();
        int status = execute.getStatus();
        if(status!=200){
            return "Error request: 70003";//该页面无法获取图标，换个页面试试
        }
        return newUrl;
    }

}
