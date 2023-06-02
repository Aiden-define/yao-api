package com.yao.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.beans.Transient;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户接口关系表
 * @TableName user_interface_info
 */
@TableName(value ="user_interface_info")
@Data
public class UserInterfaceInfo implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户主键
     */
    private Long userId;

    /**
     * 接口id
     */
    private Long interfaceInfoId;

    /**
     * 接口总调用次数
     */
    private Long totalNum;

    /**
     * 剩余调用次数
     */
    private Integer leftNum;
    //接口测试添加数据错误，原因：在实现接口调用统计的时候需要total数据，
    // 但是这在数据库中是不存在的，所以在添加时如果不排除该列会出现错误
    @TableField(exist = false)
    private int total;

    /**
     * 0-正常 1-禁止
     */
    private Integer status;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 是否删除（0-未删 1-删除）
     */
    private Integer isDelete;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
