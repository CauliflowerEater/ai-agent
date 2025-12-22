package com.shawn.aiagent.api.exception;

import com.shawn.aiagent.api.error.ErrorCode;
import com.shawn.aiagent.support.response.ApiResponse;
import com.shawn.aiagent.support.response.ApiResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 处理API层的异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Intent: 处理业务异常
     * Input: BusinessException
     * Output: ApiResponse<?>
     * SideEffects: 记录错误日志
     * Failure: 无
     * Idempotency: 幂等
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException: code={}, message={}", e.getCode(), e.getMessage(), e);
        return ApiResponseBuilder.error(e.getCode(), e.getMessage());
    }

    /**
     * Intent: 处理运行时异常
     * Input: RuntimeException
     * Output: ApiResponse<?>
     * SideEffects: 记录错误日志
     * Failure: 无
     * Idempotency: 幂等
     */
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ApiResponseBuilder.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}

