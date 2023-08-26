package com.yao.yaoapiinterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.yao.yaoapiinterface.entity.DailyContent;
import com.yao.yaoapiinterface.entity.DailyContentResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author DH
 * @version 1.0
 * @description 每日一言
 * @date 2023/8/16 19:51
 */
@RestController
@RequestMapping("/dailyContent")
public class DailContentController {
    @PostMapping("/post")
    public String getDailyContentByPost() {
        String url = "https://api.xygeng.cn/one";
        HttpResponse execute = HttpRequest.get(url).execute();
        int status = execute.getStatus();
        if (status != 200) {
            return "Error request: 70001";
        }
        String body = execute.body();
        DailyContentResponse bean = JSONUtil.toBean(body, DailyContentResponse.class);
        DailyContent dailyContent = bean.getData();
        //System.out.println(body);
        return dailyContent.getContent();
    }
}

