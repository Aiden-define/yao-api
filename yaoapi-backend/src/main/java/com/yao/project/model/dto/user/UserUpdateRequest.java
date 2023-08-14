package com.yao.project.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 *
 * @author DH
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;


    /**
     * 性别
     */
    private Integer gender;

    /**
     * 用户邮箱
     */
   // private String email;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
