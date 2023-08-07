package com.yao.yaoapiclientsdk.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author DH
 * @version 1.0
 * @description 获取加密sign
 * @date 2023/5/1 18:17
 */
public  class SignUtils {
    public static String getsign(String accessKey,String secretKey,String body){
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        String data = accessKey + secretKey + body;
        return md5.digestHex(data);
    }
}
