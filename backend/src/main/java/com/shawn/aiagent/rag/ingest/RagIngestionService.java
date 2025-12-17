package com.shawn.aiagent.rag.ingest;

import com.shawn.aiagent.rag.embedding.EmbeddingService;
import com.shawn.aiagent.rag.loader.DocumentLoader;
import com.shawn.aiagent.rag.vectorstore.VectorStoreWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * RAG 数据摄取服务抽象模板类
 * 实现 ETL（Extract, Transform, Load）流程：
 * - Extract: 从数据源加载文档（抽象方法，子类必须实现）
 * - Transform: 对文档进行转换处理（可选，默认实现为空）
 * - Load: 将文档写入向量存储（抽象方法，子类必须实现）
 * 
 * 使用模板方法模式，子类可以：
 * - 必须实现 extract() 和 load() 方法
 * - 可选覆盖 transform() 方法进行自定义转换
 * - 可选使用 embedding() 钩子方法进行显式向量化（虽然 VectorStore 会自动处理）
 * 
 * 注意：Spring AI 的 VectorStore 在添加文档时会自动进行 embedding，
 * 因此 embedding 相关方法是可选的扩展点，用于未来可能需要显式控制 embedding 的场景。
 */
@Slf4j
public abstract class RagIngestionService {

    protected final DocumentLoader documentLoader;
    protected final VectorStoreWriter vectorStoreWriter;
    @Nullable
    protected final EmbeddingService embeddingService;
    protected final BatchProcessor batchProcessor;

    /**
     * 构造函数
     * 
     * @param documentLoader 文档加载器（必需）
     * @param vectorStoreWriter 向量存储写入器（必需）
     * @param embeddingService Embedding 服务（可选，可以为 null）
     *                         注意：Spring AI 的 VectorStore 会自动处理 embedding，
     *                         此参数用于未来可能需要显式控制 embedding 的场景
     * @param batchProcessor 批次处理器（必需）
     */
    protected RagIngestionService(
            DocumentLoader documentLoader,
            VectorStoreWriter vectorStoreWriter,
            @Nullable EmbeddingService embeddingService,
            BatchProcessor batchProcessor) {
        this.documentLoader = documentLoader;
        this.vectorStoreWriter = vectorStoreWriter;
        this.embeddingService = embeddingService;
        this.batchProcessor = batchProcessor;
    }

    /**
     * 执行 RAG 数据摄取流程（模板方法）
     * 优化：将 load 操作进行分批处理，并设置超时时间
     * 
     * @return 成功加载的文档数量
     */
    public final int ingest() {
        log.info("开始执行 RAG 数据摄取流程");
        
        try {
            // Extract: 从数据源加载文档
            List<Document> documents = extract();
            log.info("成功提取 {} 个文档", documents.size());
            
            if (documents.isEmpty()) {
                log.warn("没有文档需要处理");
                return 0;
            }
            
            // Transform: 对文档进行转换处理（可选）
            // List<Document> transformedDocuments = transform(documents);
            // log.info("成功转换 {} 个文档", transformedDocuments.size());
            
            // Load: 将文档分批写入向量存储（带超时控制）
            int loadedCount = batchProcessor.processInBatches(documents, this::load);
            log.info("成功将 {} 个文档写入向量存储", loadedCount);
            
            return loadedCount;
        } catch (TimeoutException e) {
            log.error("RAG 数据摄取流程超时失败", e);
            throw new RuntimeException("RAG 数据摄取流程超时失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("RAG 数据摄取流程失败", e);
            throw new RuntimeException("RAG 数据摄取流程失败", e);
        }
    }

    /**
     * Extract: 从数据源加载文档（抽象方法，子类必须实现）
     * 
     * @return 文档列表
     */
    protected abstract List<Document> extract();

    /**
     * Transform: 对文档进行转换处理（可选方法，默认实现为空）
     * 子类可以覆盖此方法进行自定义转换，例如：
     * - 文档预处理
     * - 元数据增强
     * - 显式调用 embedding（虽然 VectorStore 会自动处理）
     * 
     * @param documents 原始文档列表
     * @return 转换后的文档列表（默认返回原文档列表）
     */
    protected List<Document> transform(List<Document> documents) {
        log.debug("使用默认转换方法，文档数量: {}", documents.size());
        // 默认实现：直接返回原文档列表
        // 子类可以覆盖此方法进行自定义转换
        return documents;
    }

    /**
     * Load: 将文档写入向量存储（抽象方法，子类必须实现）
     * 
     * @param documents 待写入的文档列表
     */
    protected abstract void load(List<Document> documents);

    /**
     * 可选的 Embedding 钩子方法（扩展点）
     * 用于未来可能需要显式控制 embedding 的场景
     * 注意：Spring AI 的 VectorStore 会自动处理 embedding，此方法默认不执行任何操作
     * 
     * @param document 待向量化的文档
     * @return 向量数组（如果执行了 embedding），否则返回 null
     */
    protected float[] embedDocument(Document document) {
        // 默认实现：不执行显式 embedding
        // 子类可以覆盖此方法以启用显式 embedding
        if (embeddingService != null) {
            log.trace("可选：对文档 {} 进行显式向量化", document.getId());
            try {
                return embeddingService.embedDocument(document);
            } catch (Exception e) {
                log.warn("文档 {} 显式向量化失败，将在写入 VectorStore 时自动处理: {}", 
                        document.getId(), e.getMessage());
            }
        }
        return null;
    }
}
