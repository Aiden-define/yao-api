package com.yao.project.model.vo;

import com.yao.common.model.entity.InterfaceInfo;
import com.yao.common.model.entity.UserInterfaceInfo;
import lombok.Data;

/**
 * @author DH
 * @version 1.0
 * @description 接口文档显示
 * @date 2023/8/16 16:10
 */
@Data
public class InterfaceInfoUserVO extends InterfaceInfo {
    /**
     * 某个用户调用该接口的总次数
     */
    private Long totalNum;

    /**
     * 某个用户调用该接口的剩余次数
     */
    private Integer leftNum;
}
