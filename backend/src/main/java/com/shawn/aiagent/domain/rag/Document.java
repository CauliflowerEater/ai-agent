package com.shawn.aiagent.domain.rag;

import java.util.List;
import java.util.Objects;

/**
 * 文档实体
 * 封装文档的领域概念，包含文档块列表
 */
public class Document {
    
    private final String id;
    private final String source;
    private final List<DocumentChunk> chunks;
    
    /**
     * Intent: 创建文档实体
     * Input: id (文档ID), source (文档来源), chunks (文档块列表)
     * Output: Document实例
     * SideEffects: 无
     * Failure: 如果id或source为null或空，抛出IllegalArgumentException
     * Idempotency: 幂等
     */
    public Document(String id, String source, List<DocumentChunk> chunks) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Document id cannot be null or empty");
        }
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("Document source cannot be null or empty");
        }
        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalArgumentException("Document chunks cannot be null or empty");
        }
        this.id = id.trim();
        this.source = source.trim();
        this.chunks = List.copyOf(chunks);
    }
    
    public String getId() {
        return id;
    }
    
    public String getSource() {
        return source;
    }
    
    public List<DocumentChunk> getChunks() {
        return chunks;
    }
    
    public int getChunkCount() {
        return chunks.size();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Document{id='" + id + "', source='" + source + 
               "', chunkCount=" + chunks.size() + "}";
    }
}

