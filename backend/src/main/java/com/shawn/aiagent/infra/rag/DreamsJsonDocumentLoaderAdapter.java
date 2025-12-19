package com.shawn.aiagent.infra.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.aiagent.domain.rag.DocumentChunk;
import com.shawn.aiagent.port.rag.DocumentLoaderGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * Dreams JSON文档加载器适配器
 * 实现DocumentLoaderGateway接口，从JSON文件加载文档
 */
@Component
@Slf4j
public class DreamsJsonDocumentLoaderAdapter implements DocumentLoaderGateway {
    
    private final ObjectMapper objectMapper;
    
    public DreamsJsonDocumentLoaderAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public List<DocumentChunk> loadDocuments() {
        log.info("开始加载dreams_chunks.json文档");
        
        try {
            var resource = new ClassPathResource("rag/dreams_chunks.json");
            List<Map<String, Object>> rawChunks = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );
            
            List<DocumentChunk> chunks = new ArrayList<>();
            
            for (Map<String, Object> chunk : rawChunks) {
                // text 字段 = Document 的 content
                String content = (String) chunk.get("text");
                if (content == null || content.trim().isEmpty()) {
                    log.warn("跳过空内容的chunk: {}", chunk);
                    continue;
                }
                
                // 构造一个 id（chunk_index 为必填字段）
                Object idxObj = chunk.get("chunk_index");
                if (idxObj == null) {
                    log.error("Missing chunk_index in dreams chunk: {}", chunk);
                    throw new IllegalArgumentException("chunk_index is required for dreams chunk");
                }
                
                // 原始 ID（用于调试和查询）
                String originalId = "dreams-chunk-" + idxObj;
                
                // pgvector 要求使用 UUID 格式的 ID，使用 UUID v5（基于命名空间和原始 ID）保持可预测性
                UUID namespace = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
                String id = UUID.nameUUIDFromBytes((namespace.toString() + originalId).getBytes()).toString();
                
                // metadata = 除了 text 以外的所有字段
                Map<String, Object> metadata = new HashMap<>(chunk);
                metadata.remove("text");
                
                // 把原始 id 也放 metadata 里，方便检索时看到
                metadata.put("originalId", originalId);
                metadata.put("id", originalId);
                metadata.put("source", "dreams");
                
                DocumentChunk documentChunk = new DocumentChunk(id, content, metadata);
                chunks.add(documentChunk);
            }
            
            log.info("成功加载 {} 个文档块", chunks.size());
            return chunks;
        } catch (IOException e) {
            log.error("加载dreams_chunks.json失败", e);
            throw new RuntimeException("加载dreams_chunks.json失败: " + e.getMessage(), e);
        }
    }
}

