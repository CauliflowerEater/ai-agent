package com.shawn.aiagent.rag;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.aiagent.common.exception.DataIntegrityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class DreamsJsonDocumentLoader {

    private final ObjectMapper objectMapper;

    public DreamsJsonDocumentLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Document> loadDreamsDocuments() {
        try {
            var resource = new ClassPathResource("rag/dreams_chunks_overlap.json");
            List<Map<String, Object>> rawChunks = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            List<Document> documents = new ArrayList<>();

            for (Map<String, Object> chunk : rawChunks) {
                // text 字段 = Document 的 content
                String content = (String) chunk.get("text");

                // 构造一个 id（chunk_index 为必填字段，用于保证数据完整性）
                Object idxObj = chunk.get("chunk_index");
                if (idxObj == null) {
                    log.error("Missing chunk_index in dreams chunk: {}", chunk);
                    throw new DataIntegrityException("chunk_index is required for dreams chunk. This indicates a data quality issue that should be fixed in development.");
                }
                String id = "dreams-chunk-" + idxObj;

                // metadata = 除了 text 以外的所有字段
                Map<String, Object> metadata = new HashMap<>(chunk);
                metadata.remove("text");

                // 把 id 也放 metadata 里，方便检索时看到
                metadata.put("id", id);
                metadata.put("source", "dreams");  // 方便以后混多个语料

                Document doc = new Document(id, content, metadata);
                documents.add(doc);
            }

            return documents;
        } catch (IOException e) {
            log.error("Failed to load dreams_chunks_overlap.json", e);
            throw new DataIntegrityException("Failed to load dreams_chunks_overlap.json. Check if the file exists and is properly formatted.", e);
        }
    }
}
