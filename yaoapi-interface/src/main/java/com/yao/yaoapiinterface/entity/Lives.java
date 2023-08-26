package com.yao.yaoapiinterface.entity;

import lombok.Data;

/**
 * @author DH
 * @version 1.0
 * @description 天气详情封装类
 * @date 2023/8/15 14:10
 */
@Data
public class Lives {
    private String province;
    private String city;
    private String adcode;
    private String weather;
    private String temperature;
    private String winddirection;
    private String windpower;
    private String humidity;
    private String reporttime;
    private String temperature_float;
    private String humidity_float;

}
