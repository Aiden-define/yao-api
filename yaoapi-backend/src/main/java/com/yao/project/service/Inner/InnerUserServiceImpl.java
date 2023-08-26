package com.yao.project.service.Inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yao.common.model.entity.User;
import com.yao.common.service.InnerUserService;
import com.yao.project.mapper.UserMapper;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @author DH
 * @version 1.0
 * @description 提供User相关公共方法
 * @date 2023/5/14 21:24
 */
@DubboService
public class InnerUserServiceImpl implements InnerUserService {
    @Resource
    private UserMapper userMapper;

    /**
     * 是否已分配给用户ak sk
     *
     * @param accessKey
     * @return
     */
    @Override
    public User getInvokeUser(String accessKey) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("accessKey", accessKey);
        //为null由网关抛状态码，由接口调用方法判断抛异常
        return userMapper.selectOne(userQueryWrapper);
    }
}

