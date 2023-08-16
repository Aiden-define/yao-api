package com.yao.yaoapiinterface.entity;

import lombok.Data;

/**
 * @author DH
 * @version 1.0
 * @description 每日一语
 * @date 2023/8/16 19:58
 */
@Data
public class DailyContentResponse {
    private int code;
    private DailyContent data;

}
