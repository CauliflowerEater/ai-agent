package com.shawn.aiagent.app.rag;

import com.shawn.aiagent.domain.rag.DocumentChunk;
import com.shawn.aiagent.domain.rag.ReindexResult;
import com.shawn.aiagent.port.rag.DocumentLoaderGateway;
import com.shawn.aiagent.port.rag.VectorStoreGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 重新索引文档用例实现
 * 编排文档加载、向量化和存储的流程
 */
@Component
@Slf4j
public class ReindexDocumentsUseCaseImpl implements ReindexDocumentsUseCase {
    
    private final DocumentLoaderGateway documentLoaderGateway;
    private final VectorStoreGateway vectorStoreGateway;
    
    /**
     * 乐观锁：用于防止并发执行 reindex 操作
     */
    private final AtomicBoolean isReindexing = new AtomicBoolean(false);
    
    public ReindexDocumentsUseCaseImpl(
            DocumentLoaderGateway documentLoaderGateway,
            VectorStoreGateway vectorStoreGateway) {
        this.documentLoaderGateway = documentLoaderGateway;
        this.vectorStoreGateway = vectorStoreGateway;
    }
    
    @Override
    public Mono<ReindexResult> execute() {
        log.info("尝试获取 reindex 锁");
        
        // 尝试获取乐观锁，如果失败则返回"正在执行"的结果
        if (!isReindexing.compareAndSet(false, true)) {
            log.warn("reindex 操作正在进行中，拒绝重复执行");
            return Mono.just(ReindexResult.inProgress());
        }
        
        log.info("成功获取 reindex 锁，开始执行重新索引");
        
        // 使用 Mono.fromCallable 在非响应式线程池中执行阻塞操作
        return Mono.fromCallable(() -> {
            try {
                // 1. 加载文档
                log.info("开始加载文档");
                List<DocumentChunk> chunks = documentLoaderGateway.loadDocuments();
                log.info("成功加载 {} 个文档块", chunks.size());
                
                if (chunks.isEmpty()) {
                    log.warn("没有文档需要处理");
                    return ReindexResult.success(0);
                }
                
                // 2. 写入向量存储（会自动进行向量化）
                log.info("开始写入向量存储");
                vectorStoreGateway.addDocuments(chunks);
                log.info("成功写入 {} 个文档块到向量存储", chunks.size());
                
                return ReindexResult.success(chunks.size());
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
}

