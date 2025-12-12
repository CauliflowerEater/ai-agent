package com.shawn.aiagent.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * 日志使用示例
 * 
 * 本类展示了在 AI Agent 项目中如何正确使用 Logback + SLF4J
 */
public class LoggingExample {

    private static final Logger log = LoggerFactory.getLogger(LoggingExample.class);

    /**
     * 基本日志记录
     */
    public void basicLogging() {
        log.trace("TRACE 级别：最详细的信息，一般不开启");
        log.debug("DEBUG 级别：调试信息，开发环境使用");
        log.info("INFO 级别：重要的业务流程信息");
        log.warn("WARN 级别：警告信息，可能有问题但不影响运行");
        log.error("ERROR 级别：错误信息，需要关注");
    }

    /**
     * 参数化日志（推荐方式，避免字符串拼接）
     */
    public void parameterizedLogging(String userId, String message) {
        // ✅ 推荐：使用占位符
        log.info("用户 {} 发送消息: {}", userId, message);
        
        // ❌ 不推荐：字符串拼接（性能差）
        // log.info("用户 " + userId + " 发送消息: " + message);
    }

    /**
     * 异常日志记录
     */
    public void exceptionLogging() {
        try {
            // 模拟异常
            throw new RuntimeException("测试异常");
        } catch (Exception e) {
            // ✅ 推荐：记录完整的异常堆栈
            log.error("处理请求时发生错误", e);
            
            // ❌ 不推荐：只记录异常消息
            // log.error("处理请求时发生错误: {}", e.getMessage());
        }
    }

    /**
     * MDC（Mapped Diagnostic Context）使用
     * 适用于追踪分布式请求、流式响应等场景
     */
    public void mdcLogging(String chatId, String sessionId) {
        try {
            // 设置 MDC 上下文
            MDC.put("chatId", chatId);
            MDC.put("sessionId", sessionId);
            
            // 后续的日志都会自动包含这些上下文信息
            log.info("开始处理聊天请求");
            log.info("调用 AI 模型");
            log.info("返回响应");
            
        } finally {
            // ⚠️ 重要：必须清理 MDC，避免内存泄漏
            MDC.clear();
        }
    }

    /**
     * 条件日志（避免不必要的对象创建）
     */
    public void conditionalLogging(Object complexObject) {
        // ✅ 推荐：使用 isDebugEnabled 判断
        if (log.isDebugEnabled()) {
            log.debug("复杂对象内容: {}", complexObject.toString());
        }
        
        // 对于简单的字符串，不需要判断（SLF4J 已经优化）
        log.debug("简单消息: {}", "value");
    }

    /**
     * AI 流式响应日志记录示例
     */
    public void streamLogging(String chatId, String chunk) {
        try {
            MDC.put("chatId", chatId);
            MDC.put("requestType", "stream");
            
            log.info("接收到流式响应片段，长度: {}", chunk.length());
            
            if (log.isDebugEnabled()) {
                log.debug("流式响应内容: {}", chunk);
            }
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * 性能监控日志
     */
    public void performanceLogging() {
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行业务逻辑
            Thread.sleep(100);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            if (duration > 1000) {
                log.warn("操作耗时过长: {} ms", duration);
            } else {
                log.debug("操作耗时: {} ms", duration);
            }
        }
    }
}
