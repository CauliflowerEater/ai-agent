package com.shawn.aiagent.support.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 检索相关配置
 */
@Configuration
@ConfigurationProperties(prefix = "rag.retrieval")
public class RetrievalConfig {

    /**
     * 查询最大长度
     */
    private int maxQueryLength = 5000;

    /**
     * 总超时时间（秒）
     */
    private int timeoutTotalSeconds = 30;

    /**
     * Embedding 阶段超时（秒）
     */
    private int timeoutEmbeddingSeconds = 10;

    /**
     * 向量检索阶段超时（秒）
     */
    private int timeoutVectorSearchSeconds = 10;

    /**
     * 日志中 query 预览长度
     */
    private int logQueryPreviewLength = 128;

    public int getMaxQueryLength() {
        return maxQueryLength;
    }

    public void setMaxQueryLength(int maxQueryLength) {
        this.maxQueryLength = maxQueryLength;
    }

    public int getTimeoutTotalSeconds() {
        return timeoutTotalSeconds;
    }

    public void setTimeoutTotalSeconds(int timeoutTotalSeconds) {
        this.timeoutTotalSeconds = timeoutTotalSeconds;
    }

    public int getTimeoutEmbeddingSeconds() {
        return timeoutEmbeddingSeconds;
    }

    public void setTimeoutEmbeddingSeconds(int timeoutEmbeddingSeconds) {
        this.timeoutEmbeddingSeconds = timeoutEmbeddingSeconds;
    }

    public int getTimeoutVectorSearchSeconds() {
        return timeoutVectorSearchSeconds;
    }

    public void setTimeoutVectorSearchSeconds(int timeoutVectorSearchSeconds) {
        this.timeoutVectorSearchSeconds = timeoutVectorSearchSeconds;
    }

    public int getLogQueryPreviewLength() {
        return logQueryPreviewLength;
    }

    public void setLogQueryPreviewLength(int logQueryPreviewLength) {
        this.logQueryPreviewLength = logQueryPreviewLength;
    }
}


