package com.yao.yaoapiinterface.entity;

import lombok.Data;

/**
 * @author DH
 * @version 1.0
 * @description data
 * @date 2023/8/16 20:01
 */
@Data
public class DailyContent {
    private int id;
    private String tag;
    private String name;
    private String origin;
    private String content;
}
