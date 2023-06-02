package com.yao.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.common.model.entity.InterfaceInfo;

/**
* @author DH
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2023-04-25 19:55:32
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    /**
     * 校验
     *
     * @param interfaceInfo
     * @param add 是否为创建校验
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
}
