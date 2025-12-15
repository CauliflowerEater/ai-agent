package com.shawn.aiagent.rag.ingest;

import com.shawn.aiagent.rag.embedding.EmbeddingService;
import com.shawn.aiagent.rag.loader.DocumentLoader;
import com.shawn.aiagent.rag.vectorstore.VectorStoreWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    /**
     * 批量加载时的批次大小（每批处理的文档数量）
     * 默认值：100，可通过配置 rag.ingestion.batch-size 修改
     */
    @Value("${rag.ingestion.batch-size:100}")
    protected int batchSize;

    /**
     * 每批加载的超时时间（秒）
     * 默认值：300秒（5分钟），可通过配置 rag.ingestion.batch-timeout-seconds 修改
     */
    @Value("${rag.ingestion.batch-timeout-seconds:300}")
    protected int batchTimeoutSeconds;

    /**
     * 用于执行超时控制的线程池
     */
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 构造函数
     * 
     * @param documentLoader 文档加载器（必需）
     * @param vectorStoreWriter 向量存储写入器（必需）
     * @param embeddingService Embedding 服务（可选，可以为 null）
     *                         注意：Spring AI 的 VectorStore 会自动处理 embedding，
     *                         此参数用于未来可能需要显式控制 embedding 的场景
     */
    protected RagIngestionService(
            DocumentLoader documentLoader,
            VectorStoreWriter vectorStoreWriter,
            @Nullable EmbeddingService embeddingService) {
        this.documentLoader = documentLoader;
        this.vectorStoreWriter = vectorStoreWriter;
        this.embeddingService = embeddingService;
    }

    /**
     * 执行 RAG 数据摄取流程（模板方法）
     * 优化：将 load 操作进行分批处理，并设置超时时间
     * 
     * @return 成功加载的文档数量
     */
    public final int ingest() {
        log.info("开始执行 RAG 数据摄取流程，批次大小: {}, 超时时间: {}秒", batchSize, batchTimeoutSeconds);
        
        try {
            // Extract: 从数据源加载文档
            List<Document> documents = extract();
            log.info("成功提取 {} 个文档", documents.size());
            
            if (documents.isEmpty()) {
                log.warn("没有文档需要处理");
                return 0;
            }
            
            // Transform: 对文档进行转换处理（可选）
            List<Document> transformedDocuments = transform(documents);
            log.info("成功转换 {} 个文档", transformedDocuments.size());
            
            // Load: 将文档分批写入向量存储（带超时控制）
            int loadedCount = loadInBatches(transformedDocuments);
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
     * 将文档分批加载到向量存储（带超时控制）
     * 
     * @param documents 待写入的文档列表
     * @return 成功加载的文档数量
     * @throws TimeoutException 如果任何批次超时
     */
    protected int loadInBatches(List<Document> documents) throws TimeoutException {
        if (documents.isEmpty()) {
            return 0;
        }

        int totalBatches = (documents.size() + batchSize - 1) / batchSize;
        log.info("开始分批加载文档，总数: {}, 批次大小: {}, 总批次数: {}", 
                documents.size(), batchSize, totalBatches);

        AtomicInteger successCount = new AtomicInteger(0);

        // 将文档列表分割成批次
        for (int i = 0; i < documents.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, documents.size());
            List<Document> batch = documents.subList(i, endIndex);
            int batchNumber = (i / batchSize) + 1;

            log.info("开始处理第 {}/{} 批，文档数量: {}", batchNumber, totalBatches, batch.size());

            try {
                // 执行单批加载（带超时控制）
                loadBatchWithTimeout(batch, batchNumber, totalBatches);
                successCount.addAndGet(batch.size());
                log.info("第 {}/{} 批加载成功，已加载文档数: {}/{}", 
                        batchNumber, totalBatches, successCount.get(), documents.size());
            } catch (TimeoutException e) {
                log.error("第 {}/{} 批加载超时（超时时间: {}秒）", 
                        batchNumber, totalBatches, batchTimeoutSeconds, e);
                // 超时则停止后续批次处理
                throw new TimeoutException(String.format(
                        "第 %d/%d 批加载超时（超时时间: %d秒），已成功加载 %d/%d 个文档",
                        batchNumber, totalBatches, batchTimeoutSeconds, 
                        successCount.get(), documents.size()));
            } catch (Exception e) {
                log.error("第 {}/{} 批加载失败", batchNumber, totalBatches, e);
                // 其他异常也停止后续批次处理
                throw new RuntimeException(String.format(
                        "第 %d/%d 批加载失败，已成功加载 %d/%d 个文档: %s",
                        batchNumber, totalBatches, successCount.get(), 
                        documents.size(), e.getMessage()), e);
            }
        }

        return successCount.get();
    }

    /**
     * 加载单个批次（带超时控制）
     * 
     * @param batch 当前批次的文档列表
     * @param batchNumber 当前批次号
     * @param totalBatches 总批次数
     * @throws TimeoutException 如果加载超时
     * @throws Exception 如果加载失败
     */
    protected void loadBatchWithTimeout(List<Document> batch, int batchNumber, int totalBatches) 
            throws TimeoutException, Exception {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                load(batch);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executorService);

        try {
            // 等待完成或超时
            future.get(batchTimeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // 取消任务
            future.cancel(true);
            throw new TimeoutException(String.format(
                    "第 %d/%d 批加载超时（超时时间: %d秒）", 
                    batchNumber, totalBatches, batchTimeoutSeconds));
        } catch (ExecutionException e) {
            // 获取实际异常
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException && cause.getCause() != null) {
                throw (Exception) cause.getCause();
            }
            throw new RuntimeException("批次加载执行异常", cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("批次加载被中断", e);
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
