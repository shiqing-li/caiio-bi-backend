package com.yupi.springbootinit.exception;

import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

   //Handle BusinessException
 @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        //Log the exception
        log.error("BusinessException", e);
        //Return an error response with the exception's code and message

//        if(e.getCode() == ErrorCode.NOT_LOGIN_ERROR.getCode()){
//            return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR);
//        }

        return ResultUtils.error(e.getCode(), e.getMessage());
    }

//Handle RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        //Log the exception
        log.error("RuntimeException", e);
        //Return an error response with the system error code and message
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

}
