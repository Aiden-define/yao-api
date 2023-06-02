package com.yao.common.service;

import com.yao.common.model.entity.User;


/**
 * @author DH
 * @version 1.0
 * @description 提供User相关公共方法
 * @date 2023/5/14 21:13
 */
public interface InnerUserService {
    /**
     * 是否已分配给用户ak sk
     * @return user
     */
    User getInvokeUser(String accessKey);
}
