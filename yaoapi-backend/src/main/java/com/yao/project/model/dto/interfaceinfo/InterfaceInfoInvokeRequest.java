package com.yao.project.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口测试调用参数
 *
 * @TableName InterfaceInfo
 */
@Data
public class InterfaceInfoInvokeRequest implements Serializable {

    private Long id;

    /*
    请求参数
     */
    private String userRequestParams;



}
