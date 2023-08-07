package com.yao.project.config;

import com.yao.project.aop.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author DH
 * @version 1.0
 * @description 登陆相关拦截器配置
 * @date 2023/6/3 13:50
 */
@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CorsConfig config;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
                .excludePathPatterns(
                        "/user/login",
                        "/v3/api-docs"
                ).order(1);
        registry.addInterceptor(config).addPathPatterns("/**").order(0);

    }
}
