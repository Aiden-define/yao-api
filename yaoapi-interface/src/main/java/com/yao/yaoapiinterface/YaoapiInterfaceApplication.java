package com.yao.yaoapiinterface;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class YaoapiInterfaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YaoapiInterfaceApplication.class, args);
    }

}
