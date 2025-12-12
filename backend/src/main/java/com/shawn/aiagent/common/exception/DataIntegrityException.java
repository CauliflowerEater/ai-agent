package com.shawn.aiagent.common.exception;

/**
 * 数据完整性异常
 * 用于在开发/测试阶段检测静态资源数据的完整性问题
 * 
 * 特点：
 * 1. 不应该在生产环境触发（数据应在测试阶段验证）
 * 2. 主要用于启动时或初始化时的数据校验
 * 3. 表示数据质量问题，而非运行时业务异常
 */
public class DataIntegrityException extends RuntimeException {

    public DataIntegrityException(String message) {
        super(message);
    }

    public DataIntegrityException(String message, Throwable cause) {
        super(message, cause);
    }
}
