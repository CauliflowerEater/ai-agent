package com.shawn.aiagent.support.response;

import com.shawn.aiagent.domain.common.error.ErrorCode;

/**
 * API响应构建器
 * 提供构建API响应的工具方法
 */
public class ApiResponseBuilder {

    /**
     * Intent: 构建成功响应
     * Input: data (响应数据)
     * Output: ApiResponse<T>
     * SideEffects: 无
     * Failure: 无
     * Idempotency: 幂等
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, data, "ok");
    }

    /**
     * Intent: 构建失败响应
     * Input: errorCode (错误码)
     * Output: ApiResponse<?>
     * SideEffects: 无
     * Failure: 无
     * Idempotency: 幂等
     */
    public static ApiResponse<?> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode);
    }

    /**
     * Intent: 构建失败响应
     * Input: code (错误码), message (错误信息)
     * Output: ApiResponse<?>
     * SideEffects: 无
     * Failure: 无
     * Idempotency: 幂等
     */
    public static ApiResponse<?> error(int code, String message) {
        return new ApiResponse<>(code, null, message);
    }

    /**
     * Intent: 构建失败响应
     * Input: errorCode (错误码), message (错误信息)
     * Output: ApiResponse<?>
     * SideEffects: 无
     * Failure: 无
     * Idempotency: 幂等
     */
    public static ApiResponse<?> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(errorCode.getCode(), null, message);
    }
}

