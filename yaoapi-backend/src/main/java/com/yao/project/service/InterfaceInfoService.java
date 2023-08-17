package com.yao.project.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.common.model.entity.InterfaceInfo;
import com.yao.project.common.IdRequest;
import com.yao.project.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.yao.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    /**
     * 下载SDK
     * @return
     */
    void downloadSdk(HttpServletResponse res);

    /**
     * 遍历展示接口
     * @param interfaceInfoQueryRequest
     * @return
     */
    Page<InterfaceInfo> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    /**
     * 添加搜索的相关条件
     * @param interfaceInfoQuery 前端的适配条件
     * @return queryWrapper
     */
    QueryWrapper<InterfaceInfo> addCondition(InterfaceInfo interfaceInfoQuery);

    /**
     * 调用接口
     * @param interfaceInfoInvokeRequest
     * @param request
     * @return
     */
    String invokeInterface(InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request);

    /**
     * 接口上线
     * @param idRequest
     * @return
     */
    Boolean interfaceOnLine(IdRequest idRequest,HttpServletRequest request);

}
