package com.yao.yaoapiclientsdk;


import com.yao.yaoapiclientsdk.client.CommonApiClient;
import com.yao.yaoapiclientsdk.client.YaoApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author DH
 * @version 1.0
 * @description 启动类
 * @date 2023/5/1 20:57
 */
@Configuration
@ConfigurationProperties("yao.api")
@Data
@ComponentScan
public class YaoApiClientConfig {
    String accessKey;
    String secretKey;
    @Bean
    public YaoApiClient yaoApiClient(){
        return new YaoApiClient(accessKey,secretKey);
    }
}
