package com.yao.project.model.dto.interfaceinfo;

import com.yao.common.commonUtils.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author DH
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoQueryRequest extends PageRequest implements Serializable {

    private String name;

    private String description;

    private String url;

    private String requestHeader;

    private String responseHeader;

    private Integer status;

    private String method;

    /*
    请求参数
     */
    private String requestParams;

    private Long userId;

}
