package com.yao.yaoapiclientsdk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author DH
 * @version 1.0
 * @description 城市对象
 * @date 2023/8/15 20:52
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {
    private String cityName;
    private String adcode;
}
