package com.yao.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName interface_info
 */
@TableName(value ="interface_info")
@Data
public class InterfaceInfo implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String url;

    private String requestHeader;

    private String responseHeader;

    /*
    请求参数
     */
    private String requestParams;

    private Integer status;

    private String method;

    private Long userId;

    private Date createTime;

    private Date updateTime;

    private Integer isDelete;



    private static final long serialVersionUID = 1L;
}
