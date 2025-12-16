package com.shawn.aiagent.rag.ingest;

import com.shawn.aiagent.rag.embedding.EmbeddingService;
import com.shawn.aiagent.rag.loader.DreamsJsonDocumentLoader;
import com.shawn.aiagent.rag.vectorstore.VectorStoreWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Dreams RAG 数据摄取器具体实现
 * 继承自 RagIngestionService 抽象模板类，实现具体的 ETL 流程：
 * - Extract: 使用 DreamsJsonDocumentLoader 加载文档
 * - Transform: 可选进行文档预处理和验证（使用可选的 embedding 钩子方法）
 * - Load: 使用 VectorStoreWriter 将文档写入向量存储
 */
@Slf4j
@Component
public class DreamsRagIngestor extends RagIngestionService {

    /**
     * 构造函数
     * 注入具体的 DreamsJsonDocumentLoader 实现
     * 
     * @param documentLoader DreamsJsonDocumentLoader 具体实现
     * @param vectorStoreWriter 向量存储写入器
     * @param embeddingService Embedding 服务（可选，可以为 null）
     * @param batchProcessor 批次处理器
     */
    public DreamsRagIngestor(
            DreamsJsonDocumentLoader documentLoader,
            VectorStoreWriter vectorStoreWriter,
            @Nullable EmbeddingService embeddingService,
            BatchProcessor batchProcessor) {
        super(documentLoader, vectorStoreWriter, embeddingService, batchProcessor);
    }

    /**
     * Extract: 从数据源加载文档（具体实现）
     * 
     * @return 文档列表
     */
    @Override
    protected List<Document> extract() {
        log.debug("开始提取文档");
        return documentLoader.load();
    }

    /**
     * Transform: 对文档进行转换处理（可选实现）
     * 当前实现：使用可选的 embedding 钩子方法进行文档验证
     * 注意：Spring AI 的 VectorStore 会自动处理 embedding，
     * 这里主要用于验证和日志记录，未来可以根据需要扩展
     * 
     * @param documents 原始文档列表
     * @return 转换后的文档列表
     */
    @Override
    protected List<Document> transform(List<Document> documents) {
        log.debug("开始转换文档，文档数量: {}", documents.size());
        
        // 可选：使用父类的 embedDocument 钩子方法进行文档验证
        // 注意：这是可选的扩展点，VectorStore 会自动处理 embedding
        for (Document document : documents) {
            float[] embedding = embedDocument(document);
            if (embedding != null) {
                log.trace("文档 {} 显式向量化成功，维度: {}", document.getId(), embedding.length);
            }
        }
        
        return documents;
    }

    /**
     * Load: 将文档写入向量存储（具体实现）
     * 
     * @param documents 待写入的文档列表
     */
    @Override
    protected void load(List<Document> documents) {
        log.debug("开始加载文档到向量存储，文档数量: {}", documents.size());
        vectorStoreWriter.addDocuments(documents);
    }
}
