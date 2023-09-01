package com.yao.project.exception;


import com.yao.common.commonUtils.ErrorCode;
import com.yao.common.commonUtils.Result;
import com.yao.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(com.yao.common.exception.BusinessException.class)
    public Result businessException(com.yao.common.exception.BusinessException e){
        log.error("businessException",e.getMessage(),e);
        return Result.fail(e.getCode(), e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result runtimeException(BusinessException e){
        log.error("runtimeException",e);
        return Result.fail(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }

}
