package com.shawn.aiagent.rag.vectorstore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 向量存储写入器
 * 负责将 Document 写入到 PostgreSQL + pgvector 向量数据库
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
     * 将文档列表分批处理，每批最多 20 个文档，避免一次性处理过多文档
     *
     * @param documents 待添加的文档列表
     */
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.debug("文档列表为空，跳过添加");
            return;
        }
        
        final int batchSize = 20;
        int totalSize = documents.size();
        int totalBatches = (totalSize + batchSize - 1) / batchSize;
        
        log.debug("开始批量添加文档到向量存储，文档数量: {}, 批次大小: {}, 总批次数: {}", 
                totalSize, batchSize, totalBatches);
        
        int successCount = 0;
        for (int i = 0; i < totalSize; i += batchSize) {
            int endIndex = Math.min(i + batchSize, totalSize);
            List<Document> batch = documents.subList(i, endIndex);
            int batchNumber = (i / batchSize) + 1;
            
            try {
                log.debug("处理第 {}/{} 批，文档数量: {}", batchNumber, totalBatches, batch.size());
                vectorStore.add(batch);
                successCount += batch.size();
                log.info("第 {}/{} 批添加成功，已添加: {}/{}", 
                        batchNumber, totalBatches, successCount, totalSize);
            } catch (Exception e) {
                log.error("第 {}/{} 批添加失败，文档数量: {}, 已成功添加: {}/{}, 错误: {}", 
                        batchNumber, totalBatches, batch.size(), successCount, totalSize, e.getMessage(), e);
                throw new RuntimeException(
                        String.format("批量添加文档到向量存储失败，第 %d/%d 批处理失败，已成功添加 %d/%d 个文档", 
                                batchNumber, totalBatches, successCount, totalSize), e);
            }
        }
        
        log.info("成功批量添加文档到向量存储，总文档数量: {}", successCount);
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
