package com.yao.project.model.vo;

import com.yao.common.model.entity.InterfaceInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图
 *
 * @TableName 接口信息封装口
 */
@Data
public class InterfaceInfoVo extends InterfaceInfo implements Serializable {
    /**
     *
     */
    private int total;

    private static final long serialVersionUID = 1L;
}
