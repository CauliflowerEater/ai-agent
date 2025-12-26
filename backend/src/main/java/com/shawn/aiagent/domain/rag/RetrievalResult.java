package com.shawn.aiagent.domain.rag;

import java.util.Map;
import java.util.Objects;

/**
 * 检索结果值对象
 * 表示向量检索返回的单条 chunk 结果
 */
public final class RetrievalResult {

    private final String chunkId;
    private final String text;
    private final double score;
    private final Map<String, Object> metadata;

    /**
     * Intent: 创建检索结果值对象
     * Input: chunkId (chunk 唯一标识), text (原文内容), score (余弦相似度，越大越相似), metadata (附加信息)
     * Output: RetrievalResult 实例
     * SideEffects: 无
     * Failure: chunkId/text 为空时抛出 IllegalArgumentException
     * Idempotency: 幂等
     */
    public RetrievalResult(String chunkId, String text, double score, Map<String, Object> metadata) {
        if (chunkId == null || chunkId.trim().isEmpty()) {
            throw new IllegalArgumentException("chunkId cannot be null or empty");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("text cannot be null or empty");
        }
        this.chunkId = chunkId.trim();
        this.text = text;
        this.score = score;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public String getChunkId() {
        return chunkId;
    }

    public String getText() {
        return text;
    }

    /**
     * 余弦相似度，数值越大越相似
     */
    public double getScore() {
        return score;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetrievalResult that = (RetrievalResult) o;
        return Objects.equals(chunkId, that.chunkId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunkId);
    }

    @Override
    public String toString() {
        return "RetrievalResult{chunkId='" + chunkId + "', score=" + score +
                ", metadataSize=" + metadata.size() + "}";
    }
}


