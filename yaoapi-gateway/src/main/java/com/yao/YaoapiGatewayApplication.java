package com.yao;

import com.yao.project.DemoService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
@EnableDubbo
@Service
public class YaoapiGatewayApplication {
    @DubboReference
    private DemoService demoService;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(YaoapiGatewayApplication.class, args);
        YaoapiGatewayApplication bean = context.getBean(YaoapiGatewayApplication.class);
        bean.doSayHello("给我通");
    }
    public void doSayHello(String name){
        System.out.println(demoService.sayHello(name));
    }
   /* 网关配置
   @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("path_route", r -> r.path("/baidu")
                        .uri("http://www.baidu.com"))
                .route("host_route", r -> r.path("/yao")
                        .uri("http://yupi.icu"))
                .build();
    }*/

}
