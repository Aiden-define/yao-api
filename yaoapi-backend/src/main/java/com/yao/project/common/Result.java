package com.yao.project.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 返回通用类封装
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public  class  Result<T> implements Serializable {
    private Integer code;
    private T data;
    private String message;
    private String description;

    public Result(T data, Integer code,String message) {
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public static<T> Result<T> success(){
        return new Result<>(null,null,"ok","");
    }

   /* public static<T> Result<T> success(T date){
        return new Result<>(date,0,"ok");
    }*/

    public static<T> Result<T> success(T data){
        return new Result<>(0,data,"ok","");
    }

    public static<T> Result<T> success(int code,String message){
        return new Result<>(code,null,message,"");
    }


    public static<T> Result<T> fail(ErrorCode errorCode){
        return new Result<>(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDescription());
    }
    public static<T> Result<T> fail(int code,String message,String description) {
        return new Result<>(code,null, message, description);
    }
    public static<T> Result<T> fail(ErrorCode errorCode,String message,String description){
        return new Result<>(errorCode.getCode(),null, message,description);
    }








}
