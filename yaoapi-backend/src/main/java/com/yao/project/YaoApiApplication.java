package com.yao.project;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yao.project.mapper")
@EnableDubbo
public class YaoApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(YaoApiApplication.class, args);
        System.out.println("dubbo Service Start");
    }

}
