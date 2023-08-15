package com.yao.yaoapiclientsdk.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.yao.yaoapiclientsdk.model.City;
import com.yao.yaoapiclientsdk.model.User;

/**
 * @author DH
 * @version 1.0
 * @description 调用第三方接口的API
 * @date 2023/5/1 15:58
 */
public class YaoApiClient extends CommonApiClient implements ApiClient{

    public YaoApiClient(String accessKey, String secretKey) {
        super(accessKey, secretKey);
    }

    public String getNameByPost(User user){
        String json = JSONUtil.toJsonStr(user);
        HttpResponse execute = HttpRequest.post(GATEWAY_HOST+"/api/name/user")
                .addHeaders(CommonApiClient.getHeaders(user.toString(),accessKey,secretKey))
                .body(json)
                .execute();
        System.out.println(execute.getStatus());
        return execute.body();
    }

    public String getWeatherByPost(City city){
        String jsonStr = JSONUtil.toJsonStr(city);
        HttpResponse execute = HttpRequest.post(GATEWAY_HOST+"/api/weather/post")
                .addHeaders(CommonApiClient.getHeaders(jsonStr,accessKey,secretKey))
                .body(jsonStr)
                .execute();

        System.out.println(execute.getStatus());
        return execute.body();

    }
}
