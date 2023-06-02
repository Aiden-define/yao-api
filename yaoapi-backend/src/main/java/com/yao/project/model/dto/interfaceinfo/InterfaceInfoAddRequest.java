package com.yao.project.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 创建请求
 *
 * @TableName product
 */
@Data
public class InterfaceInfoAddRequest implements Serializable {

    private String name;

    private String description;

    private String url;

    private String requestHeader;

    private String responseHeader;

    /*
    请求参数
     */
    private String requestParams;


    private String method;

}
