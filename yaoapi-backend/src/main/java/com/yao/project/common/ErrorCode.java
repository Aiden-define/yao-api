package com.yao.project.common;

public enum ErrorCode {

    SUCCESS(200,"ok",""),
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求参数为空",""),
    NOT_LOGIN(40002,"未登录",""),
    NO_AUTH(40003,"无权限",""),
    NOT_FOUND_ERROR(40400, "请求数据不存在",""),
    SYSTEM_ERROR(50000,"系统错误",""),
    OPERATION_ERROR(50001, "操作失败",""),
    INTERFACE_CLOSE(50002, "接口未开启",""),
    REDIS_ERROR(99999, "Redis错误","");


    private final int code;
    /**
     * 状态码描述
     */
    private final String message;
    /**
     * 状态码描述（详细）
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
