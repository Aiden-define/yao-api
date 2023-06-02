package com.yao.project;

import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author DH
 * @version 1.0
 * @description dubbo服务
 * @date 2023/5/14 17:32
 */
@DubboService
public class DubboServiceImpl implements DemoService {

    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
