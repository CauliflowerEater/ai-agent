package com.shawn.aiagent.domain.common.error;

import lombok.Getter;

/**
 * 错误码枚举
 * 定义API层的错误码
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),

    // RAG 检索相关错误码
    INVALID_QUERY(40001, "查询参数无效"),
    MODEL_CONFIG_ERROR(50002, "模型配置错误"),
    VECTOR_SCHEMA_ERROR(50003, "向量库结构错误"),
    RETRIEVAL_NOT_FOUND(40401, "未找到匹配的文档块"),
    EMBEDDING_TIMEOUT(50004, "向量化超时"),
    VECTOR_SEARCH_TIMEOUT(50005, "向量检索超时"),
    EMBEDDING_API_ERROR(50006, "向量化API错误"),
    VECTOR_STORE_ERROR(50007, "向量数据库错误"),
    TOTAL_TIMEOUT(50008, "检索总超时");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

