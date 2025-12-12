package com.shawn.aiagent.rag;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

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

                // 构造一个 id（可选，也可以不加）
                //todo
                //埋点
                //idx不存在应视为异常，应由log捕获;
                Object idxObj = chunk.get("chunk_index");
                String id = idxObj != null ? "dreams-chunk-" + idxObj : UUID.randomUUID().toString();

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
            //todo
            //埋点
            //确认抛异常是否符合统一格式;
            throw new RuntimeException("Failed to load dreams_chunks_overlap.json", e);
        }
    }
}