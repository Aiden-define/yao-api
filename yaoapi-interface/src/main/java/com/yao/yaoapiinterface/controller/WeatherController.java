package com.yao.yaoapiinterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.yao.yaoapiclientsdk.model.City;
import com.yao.yaoapiinterface.entity.Lives;
import com.yao.yaoapiinterface.entity.WeatherResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * @author DH
 * @version 1.0
 * @description 获取各地天气的接口
 * @date 2023/8/15 11:26
 */
@RestController
@RequestMapping("/weather")
public class WeatherController {
    public static String key = "cb1e334a12547cd5c38dc61659c9a895";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/post")
    public String getWeatherByPost(@RequestBody City city) {
        String code = stringRedisTemplate.opsForValue().get("city:cache:" + city.getCityName());
        if(code==null){
            return "Error request: 70001";//请输入完整的参数

        }
        String url = "https://restapi.amap.com/v3/weather/weatherInfo";
        String param = "?key="+key+"&city="+code;
        HttpRequest httpRequest = HttpRequest.get(url+param);
        //System.out.println(httpRequest);
        HttpResponse execute = httpRequest.execute();
        String body = execute.body();
        WeatherResponse weatherResponse = JSONUtil.toBean(body, WeatherResponse.class);
        if(Integer.parseInt(weatherResponse.getStatus())==0){
            return "Error request: 70002";//获取数据失败
        }
        String firstString = body.split("\\[")[1];
        String substring = firstString.substring(0, firstString.length() - 2);
        Lives lives = JSONUtil.toBean(substring, Lives.class);
        String message  = "你所在的："+lives.getCity()+ "，今天的天气："+lives.getWeather()+
                "，当前气温："+lives.getTemperature()+"°C，"+"风向：" +lives.getWinddirection()
                +"，风级："+lives.getWindpower()+"，空气湿度："+lives.getHumidity();
        return message;
    }


}
