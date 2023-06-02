package com.yao.yaoapiclientsdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.yao.yaoapiclientsdk.model.User;
import com.yao.yaoapiclientsdk.utils.SignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author DH
 * @version 1.0
 * @description 调用第三方接口的API
 * @date 2023/5/1 15:58
 */
public class YaoApiClient {
    private static final String GATEWAY_HOST = "http://localhost:8099";
    //密钥
    String accessKey;
    String sercetKey;

    public YaoApiClient(String accessKey, String sercetKey) {
        this.accessKey = accessKey;
        this.sercetKey = sercetKey;
    }

    public String getNameByGet(String name){
        //单独传入http参数，这样参数会自动做URL编码，拼接在URL中
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        return HttpUtil.get(GATEWAY_HOST+"/api/name/", paramMap);

    }
    public String getNameByPost(String name){
        //单独传入http参数，这样参数会自动做URL编码，拼接在URL中
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        return HttpUtil.post(GATEWAY_HOST+"/api/name/", paramMap);
    }
    public String getUserNameByPost(User user){
        String json = JSONUtil.toJsonStr(user);
        HttpResponse execute = HttpRequest.post(GATEWAY_HOST+"/api/name/user")
                .addHeaders(getHeaders(user))
                .body(json)
                .execute();
        System.out.println(execute.getStatus());
        return execute.body();
    }

    /*
    通过getHeaders添加请求头数据
     */
    private Map<String, String> getHeaders(User user) {
        Map<String,String> hashmap = new HashMap<>();
        hashmap.put("accessKey",accessKey);
        hashmap.put("body",user.toString());
        hashmap.put("nonce", RandomUtil.randomNumbers(4));
        hashmap.put("timestamp",String.valueOf(System.currentTimeMillis()/1000));
        hashmap.put("sign", SignUtils.getsign(accessKey,sercetKey,user.toString()));
        return hashmap;
    }
}
