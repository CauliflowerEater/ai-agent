package com.shawn.aiagent.domain.rag;

import java.util.Objects;

/**
 * 重新索引预览值对象
 * 封装重新索引操作的预览信息（不实际执行索引）
 */
public final class ReindexPreview {
    
    private final int chunkCount;
    private final String tableName;
    private final String embeddingModelName;
    private final int embeddingDim;
    
    /**
     * Intent: 创建重新索引预览值对象
     * Input: chunkCount (chunk数量), tableName (表名), embeddingModelName (模型名), embeddingDim (向量维度)
     * Output: ReindexPreview实例
     * SideEffects: 无
     * Failure: 如果chunkCount或embeddingDim小于0，或tableName/modelName为null，抛出IllegalArgumentException
     * Idempotency: 幂等
     */
    public ReindexPreview(int chunkCount, String tableName, String embeddingModelName, int embeddingDim) {
        if (chunkCount < 0) {
            throw new IllegalArgumentException("Chunk count cannot be negative");
        }
        if (embeddingDim < 0) {
            throw new IllegalArgumentException("Embedding dimension cannot be negative");
        }
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        if (embeddingModelName == null || embeddingModelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Embedding model name cannot be null or empty");
        }
        this.chunkCount = chunkCount;
        this.tableName = tableName.trim();
        this.embeddingModelName = embeddingModelName.trim();
        this.embeddingDim = embeddingDim;
    }
    
    public int getChunkCount() {
        return chunkCount;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public String getEmbeddingModelName() {
        return embeddingModelName;
    }
    
    public int getEmbeddingDim() {
        return embeddingDim;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReindexPreview that = (ReindexPreview) o;
        return chunkCount == that.chunkCount && 
               embeddingDim == that.embeddingDim &&
               Objects.equals(tableName, that.tableName) &&
               Objects.equals(embeddingModelName, that.embeddingModelName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(chunkCount, tableName, embeddingModelName, embeddingDim);
    }
    
    @Override
    public String toString() {
        return "ReindexPreview{chunkCount=" + chunkCount + 
               ", tableName='" + tableName + 
               "', embeddingModelName='" + embeddingModelName + 
               "', embeddingDim=" + embeddingDim + "}";
    }
}

