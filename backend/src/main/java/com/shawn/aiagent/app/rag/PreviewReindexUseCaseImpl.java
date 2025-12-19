package com.shawn.aiagent.app.rag;

import com.shawn.aiagent.domain.rag.DocumentChunk;
import com.shawn.aiagent.domain.rag.ReindexPreview;
import com.shawn.aiagent.port.rag.DocumentLoaderGateway;
import com.shawn.aiagent.port.rag.EmbeddingGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * 预览重新索引用例实现
 * 编排预览重新索引操作的流程
 */
@Component
@Slf4j
public class PreviewReindexUseCaseImpl implements PreviewReindexUseCase {
    
    private final DocumentLoaderGateway documentLoaderGateway;
    private final EmbeddingGateway embeddingGateway;
    private final String tableName;
    private final String embeddingModelName;
    
    public PreviewReindexUseCaseImpl(
            DocumentLoaderGateway documentLoaderGateway,
            EmbeddingGateway embeddingGateway,
            @Value("${spring.ai.vectorstore.pgvector.table-name}") String tableName,
            @Value("${spring.ai.dashscope.embedding.options.model}") String embeddingModelName) {
        this.documentLoaderGateway = documentLoaderGateway;
        this.embeddingGateway = embeddingGateway;
        this.tableName = tableName;
        this.embeddingModelName = embeddingModelName;
    }
    
    @Override
    public Mono<ReindexPreview> execute() {
        log.info("开始执行 dryRun 预览");
        
        return Mono.fromCallable(() -> {
            try {
                // 1. 加载文档以获取 chunk 数量（不实际写入向量存储）
                log.debug("加载文档以获取chunk数量");
                List<DocumentChunk> chunks = documentLoaderGateway.loadDocuments();
                int chunkCount = chunks.size();
                log.debug("成功加载 {} 个文档块", chunkCount);
                
                // 2. 获取 embedding 维度
                log.debug("获取embedding维度");
                int embeddingDim = embeddingGateway.getDimensions();
                log.debug("embedding维度: {}", embeddingDim);
                
                // 3. 构建预览结果
                ReindexPreview preview = new ReindexPreview(
                        chunkCount,
                        tableName,
                        embeddingModelName,
                        embeddingDim
                );
                
                log.info("dryRun 预览完成，chunk 数量: {}, table: {}, 模型: {}, 维度: {}", 
                        chunkCount, tableName, embeddingModelName, embeddingDim);
                return preview;
            } catch (Exception e) {
                log.error("dryRun 预览失败", e);
                throw new RuntimeException("dryRun 预览失败: " + e.getMessage(), e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic()); // 在非响应式线程池中执行
    }
}

