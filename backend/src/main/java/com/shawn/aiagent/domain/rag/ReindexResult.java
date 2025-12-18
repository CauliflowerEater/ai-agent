package com.shawn.aiagent.domain.rag;

import java.util.Objects;

/**
 * 重新索引结果值对象
 * 封装重新索引操作的结果信息
 */
public final class ReindexResult {
    
    private final int documentCount;
    private final String message;
    
    /**
     * Intent: 创建重新索引结果值对象
     * Input: documentCount (成功加载的文档数量), message (结果消息)
     * Output: ReindexResult实例
     * SideEffects: 无
     * Failure: 如果documentCount小于0，抛出IllegalArgumentException
     * Idempotency: 幂等
     */
    public ReindexResult(int documentCount, String message) {
        if (documentCount < 0) {
            throw new IllegalArgumentException("Document count cannot be negative");
        }
        this.documentCount = documentCount;
        this.message = message != null ? message : "";
    }
    
    /**
     * Intent: 创建成功的重新索引结果
     * Input: documentCount (成功加载的文档数量)
     * Output: ReindexResult实例（消息为"重新索引成功"）
     * SideEffects: 无
     * Failure: 如果documentCount小于0，抛出IllegalArgumentException
     * Idempotency: 幂等
     */
    public static ReindexResult success(int documentCount) {
        return new ReindexResult(documentCount, "重新索引成功");
    }
    
    /**
     * Intent: 创建失败的重新索引结果
     * Input: message (错误消息)
     * Output: ReindexResult实例（文档数量为0）
     * SideEffects: 无
     * Failure: 无
     * Idempotency: 幂等
     */
    public static ReindexResult failure(String message) {
        return new ReindexResult(0, message != null ? message : "重新索引失败");
    }
    
    /**
     * Intent: 创建进行中的重新索引结果
     * Input: 无
     * Output: ReindexResult实例（表示操作正在进行中）
     * SideEffects: 无
     * Failure: 无
     * Idempotency: 幂等
     */
    public static ReindexResult inProgress() {
        return new ReindexResult(0, "当前正在执行重新索引操作，请稍后再试");
    }
    
    public int getDocumentCount() {
        return documentCount;
    }
    
    public String getMessage() {
        return message;
    }
    
    public boolean isSuccess() {
        return documentCount > 0 && message.contains("成功");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReindexResult that = (ReindexResult) o;
        return documentCount == that.documentCount && Objects.equals(message, that.message);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(documentCount, message);
    }
    
    @Override
    public String toString() {
        return "ReindexResult{documentCount=" + documentCount + ", message='" + message + "'}";
    }
}

