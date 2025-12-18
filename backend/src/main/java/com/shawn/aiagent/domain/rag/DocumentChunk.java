package com.shawn.aiagent.domain.rag;

import java.util.Map;
import java.util.Objects;

/**
 * 文档块值对象
 * 封装文档块的内容和元数据
 */
public final class DocumentChunk {
    
    private final String id;
    private final String content;
    private final Map<String, Object> metadata;
    
    /**
     * Intent: 创建文档块值对象
     * Input: id (文档块ID), content (文档内容), metadata (元数据)
     * Output: DocumentChunk实例
     * SideEffects: 无
     * Failure: 如果id或content为null或空，抛出IllegalArgumentException
     * Idempotency: 幂等
     */
    public DocumentChunk(String id, String content, Map<String, Object> metadata) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Document chunk id cannot be null or empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Document chunk content cannot be null or empty");
        }
        this.id = id.trim();
        this.content = content.trim();
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }
    
    public String getId() {
        return id;
    }
    
    public String getContent() {
        return content;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentChunk that = (DocumentChunk) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "DocumentChunk{id='" + id + "', contentLength=" + content.length() + 
               ", metadataSize=" + metadata.size() + "}";
    }
}

