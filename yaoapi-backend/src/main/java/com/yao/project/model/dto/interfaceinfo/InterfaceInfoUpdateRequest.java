package com.yao.project.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 更新请求
 *
 * @TableName product
 */
@Data
public class InterfaceInfoUpdateRequest implements Serializable {

    private Long id;

    private String name;

    private String description;

    private String url;

    private String requestHeader;

    /*
    请求参数
     */
    private String requestParams;

    private String responseHeader;

    private Integer status;

    private String method;

    // private Long userId;

}
