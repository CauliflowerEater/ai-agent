package com.shawn.aiagent.support.response;

import com.shawn.aiagent.domain.common.error.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * API响应封装
 * 统一的API响应格式
 */
@Data
public class ApiResponse<T> implements Serializable {

    private int code;
    private T data;
    private String message;

    public ApiResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public ApiResponse(int code, T data) {
        this(code, data, "");
    }

    public ApiResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}

