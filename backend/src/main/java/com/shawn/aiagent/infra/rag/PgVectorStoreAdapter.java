package com.shawn.aiagent.infra.rag;

import com.shawn.aiagent.domain.rag.DocumentChunk;
import com.shawn.aiagent.port.rag.VectorStoreGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PostgreSQL pgvector向量存储适配器
 * 实现VectorStoreGateway接口，使用Spring AI VectorStore
 */
@Component
@Slf4j
public class PgVectorStoreAdapter implements VectorStoreGateway {
    
    @Resource
    private VectorStore vectorStore;
    
    @Override
    public void addDocuments(List<DocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            log.debug("文档块列表为空，跳过添加");
            return;
        }
        
        log.info("开始批量添加文档块到向量存储，数量: {}", chunks.size());
        
        try {
            // 将领域对象DocumentChunk转换为Spring AI的Document
            List<Document> documents = chunks.stream()
                    .map(this::toSpringAiDocument)
                    .collect(Collectors.toList());
            
            // 使用Spring AI VectorStore添加文档（会自动进行向量化）
            vectorStore.add(documents);
            
            log.info("成功添加 {} 个文档块到向量存储", chunks.size());
        } catch (Exception e) {
            log.error("添加文档块到向量存储失败，数量: {}, 错误: {}", chunks.size(), e.getMessage(), e);
            throw new RuntimeException("添加文档块到向量存储失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteDocuments(List<String> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            log.debug("文档ID列表为空，跳过删除");
            return;
        }
        
        log.info("开始删除文档，ID数量: {}", documentIds.size());
        
        try {
            vectorStore.delete(documentIds);
            log.info("成功删除文档，ID数量: {}", documentIds.size());
        } catch (Exception e) {
            log.error("删除文档失败，ID数量: {}, 错误: {}", documentIds.size(), e.getMessage(), e);
            throw new RuntimeException("删除文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将领域对象DocumentChunk转换为Spring AI的Document
     */
    private Document toSpringAiDocument(DocumentChunk chunk) {
        return new Document(
                chunk.getId(),
                chunk.getContent(),
                chunk.getMetadata()
        );
    }
}

