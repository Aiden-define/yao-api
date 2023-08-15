package com.yao.yaoapiinterface.entity;

import lombok.Data;

/**
 * @author DH
 * @version 1.0
 * @description 天气返回对象
 * @date 2023/8/15 15:34
 */
@Data
public class WeatherResponse {
    private String status;
    private String count;
    private String info;
    private String infocode; //返回状态说明，10000说明正确

}
