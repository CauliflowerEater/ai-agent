package com.shawn.aiagent.rag.vectorstore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 向量存储写入器
 * 负责将 Document 写入到 Milvus 向量数据库
 */
@Component
@Slf4j
public class VectorStoreWriter {

    @Resource
    private VectorStore vectorStore;

    /**
     * 添加单个文档到向量存储
     *
     * @param document 待添加的文档
     */
    public void addDocument(Document document) {
        log.debug("开始添加单个文档到向量存储，Document ID: {}", document.getId());
        try {
            vectorStore.add(List.of(document));
            log.info("成功添加文档到向量存储，Document ID: {}", document.getId());
        } catch (Exception e) {
            log.error("添加文档到向量存储失败，Document ID: {}, 错误: {}", 
                    document.getId(), e.getMessage(), e);
            throw new RuntimeException("添加文档到向量存储失败", e);
        }
    }

    /**
     * 批量添加文档到向量存储
     *
     * @param documents 待添加的文档列表
     */
    public void addDocuments(List<Document> documents) {
        log.debug("开始批量添加文档到向量存储，文档数量: {}", documents.size());
        try {
            vectorStore.add(documents);
            log.info("成功批量添加文档到向量存储，文档数量: {}", documents.size());
        } catch (Exception e) {
            log.error("批量添加文档到向量存储失败，文档数量: {}, 错误: {}", 
                    documents.size(), e.getMessage(), e);
            throw new RuntimeException("批量添加文档到向量存储失败", e);
        }
    }

    /**
     * 删除指定 ID 的文档
     *
     * @param documentIds 文档 ID 列表
     */
    public void deleteDocuments(List<String> documentIds) {
        log.debug("开始删除文档，文档 ID 数量: {}", documentIds.size());
        try {
            vectorStore.delete(documentIds);
            log.info("成功删除文档，文档 ID 数量: {}", documentIds.size());
        } catch (Exception e) {
            log.error("删除文档失败，文档 ID 数量: {}, 错误: {}", 
                    documentIds.size(), e.getMessage(), e);
            throw new RuntimeException("删除文档失败", e);
        }
    }
}
