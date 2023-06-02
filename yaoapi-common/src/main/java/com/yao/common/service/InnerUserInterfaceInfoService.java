package com.yao.common.service;

/**
* @author DH
* @description 提供UserInterfaceInfo相关公共方法
* @createDate 2023-05-08 19:42:49
*/
public interface InnerUserInterfaceInfoService{

    /**
     * 调用成功调用次数+1
     * @param interfaceInfoId 接口id
     * @param userId 用户id
     * @return true/false
     */
    boolean invokeCount(long interfaceInfoId,long userId);

    /**
     * 用户是否还有调用次数
     * @param interfaceInfoId  接口id
     * @param userId 用户id
     * @return true/false
     */
    boolean callTimes(long interfaceInfoId,long userId);



}
