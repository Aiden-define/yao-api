package com.yao.yaoapiclientsdk.client;

import cn.hutool.core.util.RandomUtil;
import com.yao.yaoapiclientsdk.utils.SignUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author DH
 * @version 1.0
 * @description 父类client，用于封装ak/sk等基本数据到请求头
 * @date 2023/8/15 16:18
 */
public class CommonApiClient {
    protected static final String GATEWAY_HOST = "http://localhost:8099";
    //密钥
    protected String accessKey;
    protected String secretKey;

    public CommonApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    /**
     * 请求头数据填充
     * @param body 请求体
     * @param accessKey ak
     * @param secretKey sk
     * @return Map<String, String>
     */
    protected static Map<String, String> getHeaders(String body,String accessKey,String secretKey) {
        Map<String, String> headMap = new HashMap<>();
        headMap.put("accessKey", accessKey);
        try {
            headMap.put("body", URLEncoder.encode(body,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        headMap.put("sign", SignUtils.getSign(body,accessKey, secretKey));
        headMap.put("nonce", RandomUtil.randomNumbers(4));
        headMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        return headMap;
    }
}
