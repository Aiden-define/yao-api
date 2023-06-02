package com.yao.common.service;

import com.yao.common.model.entity.InterfaceInfo;

/**
 * @author DH
 * @version 1.0
 * @description 提供接口相关公共方法
 * @date 2023/5/14 21:13
 */
public interface InnerInterfaceInfoService {
    /**
     * 查询接口是否存在以及method是否匹配
     * @param path 请求路径
     * @param method 请求方法
     * @return 返回接口信息
     */
    InterfaceInfo getInterfaceInfo(String path, String method);

}
