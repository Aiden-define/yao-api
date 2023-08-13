package com.yao.project.model.vo;

import lombok.Data;

/**
 * @author DH
 * @version 1.0
 * @description 分配的ak/sk
 * @date 2023/8/13 15:14
 */
@Data
public class UserKeyVO {
    public String accessKey;
    private String secretKey;
}
