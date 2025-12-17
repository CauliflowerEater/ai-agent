package com.shawn.aiagent.service;

import com.shawn.aiagent.common.model.DryRunResult;
import com.shawn.aiagent.common.model.ReindexResult;
import com.shawn.aiagent.rag.embedding.EmbeddingService;
import com.shawn.aiagent.rag.ingest.DreamsRagIngestor;
import com.shawn.aiagent.rag.loader.DreamsJsonDocumentLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import jakarta.annotation.Resource;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RAG 数据摄取服务
 * 负责 RAG 数据重新索引相关的业务逻辑
 */
@Service
@Slf4j
public class RagService {

    @Resource
    private DreamsRagIngestor dreamsRagIngestor;

    @Resource
    private DreamsJsonDocumentLoader documentLoader;

    @Resource
    private EmbeddingService embeddingService;

    @Value("${spring.ai.vectorstore.pgvector.table-name}")
    private String tableName;

    @Value("${spring.ai.dashscope.embedding.options.model}")
    private String embeddingModelName;

    /**
     * 乐观锁：用于防止并发执行 reindex 操作
     */
    private final AtomicBoolean isReindexing = new AtomicBoolean(false);

    /**
     * 执行重新索引（reindex）
     * 从数据源加载文档，进行向量化处理，并写入向量存储
     * 注意：由于 VectorStore 操作涉及阻塞的 Embedding API 调用，需要在非响应式线程中执行
     * 使用乐观锁防止并发执行
     * 
     * @return Mono<ReindexResult> 重新索引结果，包含成功加载的文档数量
     */
    public Mono<ReindexResult> reindex() {
        log.info("尝试获取 reindex 锁");
        
        // 尝试获取乐观锁，如果失败则返回"正在执行"的结果
        if (!isReindexing.compareAndSet(false, true)) {
            log.warn("reindex 操作正在进行中，拒绝重复执行");
            ReindexResult result = new ReindexResult();
            result.setDocumentCount(0);
            result.setMessage("当前正在执行重新索引操作，请稍后再试");
            return Mono.just(result);
        }
        
        log.info("成功获取 reindex 锁，开始执行重新索引");
        
        // 使用 Mono.fromCallable 在非响应式线程池中执行阻塞操作
        return Mono.fromCallable(() -> {
            try {
                int documentCount = dreamsRagIngestor.ingest();
                
                ReindexResult result = new ReindexResult();
                result.setDocumentCount(documentCount);
                result.setMessage("重新索引成功");
                
                log.info("重新索引完成，成功加载 {} 个文档", documentCount);
                return result;
            } catch (Exception e) {
                log.error("重新索引失败", e);
                throw new RuntimeException("重新索引失败: " + e.getMessage(), e);
            } finally {
                // 无论成功还是失败，都要释放锁
                isReindexing.set(false);
                log.info("释放 reindex 锁");
            }
        })
        .subscribeOn(Schedulers.boundedElastic()); // 在非响应式线程池中执行
    }

    /**
     * 执行 dryRun 预览
     * 返回 chunk 数量、表名、embedding 模型名和维度，但不实际执行索引操作
     * 
     * @return Mono<DryRunResult> dryRun 预览结果
     */
    public Mono<DryRunResult> dryRun() {
        log.info("开始执行 dryRun 预览");
        
        return Mono.fromCallable(() -> {
            try {
                // 加载文档以获取 chunk 数量（不实际写入向量存储）
                int chunkCount = documentLoader.load().size();
                
                // 获取 embedding 维度
                int embeddingDim = embeddingService.getDimensions();
                
                DryRunResult result = new DryRunResult();
                result.setChunkCount(chunkCount);
                result.setTableName(tableName);
                result.setEmbeddingModelName(embeddingModelName);
                result.setEmbeddingDim(embeddingDim);
                
                log.info("dryRun 预览完成，chunk 数量: {}, table: {}, 模型: {}, 维度: {}", 
                        chunkCount, tableName, embeddingModelName, embeddingDim);
                return result;
            } catch (Exception e) {
                log.error("dryRun 预览失败", e);
                throw new RuntimeException("dryRun 预览失败: " + e.getMessage(), e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic()); // 在非响应式线程池中执行
    }
}

