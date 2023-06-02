package com.yao.project.model.enums;


/**
 * @author DH
 * @version 1.0
 * @description 接口状态的枚举
 * @date 2023/5/2 19:37
 */
public enum InterfaceInfoStatusEnum {

    OFFLINE(0),
    ONLINE(1);

    private final int value;

    InterfaceInfoStatusEnum( int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
