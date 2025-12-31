package com.shawn.aiagent.app.rag;

import com.shawn.aiagent.domain.common.error.ErrorCode;
import com.shawn.aiagent.domain.common.exception.BusinessException;
import com.shawn.aiagent.domain.rag.RetrievalResult;
import com.shawn.aiagent.port.rag.EmbeddingGateway;
import com.shawn.aiagent.port.rag.VectorStoreGateway;
import com.shawn.aiagent.support.config.RetrievalConfig;
import com.shawn.aiagent.support.timeoutSemanticClassifier.TimeoutSemanticClassifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

/**
 * 基于查询语句检索最相似的 chunk
 */
@Component
@Slf4j
public class RetrieveTop1ChunkByQueryUseCaseImpl implements RetrieveTop1ChunkByQueryUseCase {

    private final EmbeddingGateway embeddingGateway;
    private final VectorStoreGateway vectorStoreGateway;
    private final RetrievalConfig retrievalConfig;
    private final TimeoutSemanticClassifier timeoutSemanticClassifier;

    public RetrieveTop1ChunkByQueryUseCaseImpl(
            EmbeddingGateway embeddingGateway,
            VectorStoreGateway vectorStoreGateway,
            RetrievalConfig retrievalConfig,
            TimeoutSemanticClassifier timeoutSemanticClassifier) {
        this.embeddingGateway = embeddingGateway;
        this.vectorStoreGateway = vectorStoreGateway;
        this.retrievalConfig = retrievalConfig;
        this.timeoutSemanticClassifier = timeoutSemanticClassifier;
    }

    @Override
    public Mono<RetrievalResult> execute(String query, String requestId) {
        return Mono.defer(() -> {
            final String reqId = requestId != null ? requestId : "";
            final String normalized = normalizeQuery(query);

            log.info("收到检索请求，requestId={}, query.length={}", reqId, normalized.length());

            Mono<List<Double>> embeddingMono = Mono.fromCallable(() -> embeddingGateway.embed(normalized))
                    .subscribeOn(Schedulers.boundedElastic())
                    .map(this::validateDimensions)
                    .timeout(Duration.ofSeconds(retrievalConfig.getTimeoutEmbeddingSeconds()))
                    .onErrorMap(e -> {
                        if (e instanceof BusinessException) {
                            return e;
                        }
                        if (timeoutSemanticClassifier.isTimeout(e)) {
                            return new BusinessException(ErrorCode.EMBEDDING_TIMEOUT, "向量化超时");
                        }
                        if (hasCause(e, IllegalArgumentException.class)) {
                            return new BusinessException(ErrorCode.INVALID_QUERY, bestMessage(e));
                        }
                        return new BusinessException(ErrorCode.EMBEDDING_API_ERROR, "向量化失败: " + bestMessage(e));
                    });

            Mono<RetrievalResult> resultMono = embeddingMono.flatMap(embedding ->
                    Mono.fromCallable(() -> vectorStoreGateway.similaritySearch(normalized, embedding, 1))
                            .subscribeOn(Schedulers.boundedElastic())
                            .timeout(Duration.ofSeconds(retrievalConfig.getTimeoutVectorSearchSeconds()))
                            .onErrorMap(e -> {
                                if (e instanceof BusinessException) {
                                    return e;
                                }
                                if (timeoutSemanticClassifier.isTimeout(e)) {
                                    return new BusinessException(ErrorCode.VECTOR_SEARCH_TIMEOUT, "向量检索超时");
                                }
                                if (hasCause(e, IllegalArgumentException.class)) {
                                    return new BusinessException(ErrorCode.INVALID_QUERY, bestMessage(e));
                                }
                                return new BusinessException(ErrorCode.VECTOR_STORE_ERROR, "向量检索失败: " + bestMessage(e));
                            })
                            .map(results -> {
                                if (results == null || results.isEmpty()) {
                                    throw new BusinessException(ErrorCode.RETRIEVAL_NOT_FOUND, "未找到匹配的文档块");
                                }
                                return results.get(0);
                            })
            );

            return resultMono
                    .timeout(Duration.ofSeconds(retrievalConfig.getTimeoutTotalSeconds()))
                    .onErrorMap(e -> {
                        if (e instanceof BusinessException) {
                            return e;
                        }
                        if (timeoutSemanticClassifier.isTimeout(e)) {
                            return new BusinessException(ErrorCode.TOTAL_TIMEOUT, "检索总超时");
                        }
                        return new BusinessException(ErrorCode.SYSTEM_ERROR, "检索失败: " + bestMessage(e));
                    })
                    .doOnSuccess(r -> log.info("检索完成，requestId={}, chunkId={}, score={}",
                            reqId, r.getChunkId(), r.getScore()))
                    .doOnError(e -> log.error("检索失败，requestId={}, error={}", reqId, e.getMessage()));
        });
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            throw new BusinessException(ErrorCode.INVALID_QUERY, "query 不能为空");
        }
        String normalized = query.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_QUERY, "query 不能为空");
        }
        if (normalized.length() > retrievalConfig.getMaxQueryLength()) {
            throw new BusinessException(ErrorCode.INVALID_QUERY, "query 超出最大长度限制");
        }
        return normalized;
    }

    private List<Double> validateDimensions(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            throw new BusinessException(ErrorCode.EMBEDDING_API_ERROR, "embedding 结果为空");
        }
        int expected = embeddingGateway.getDimensions();
        if (expected > 0 && embedding.size() != expected) {
            throw new BusinessException(ErrorCode.MODEL_CONFIG_ERROR,
                    "embedding 维度不匹配，期望 " + expected + " 实际 " + embedding.size());
        }
        return embedding;
    }

    private static boolean hasCause(Throwable e, Class<?> type) {
        Throwable t = Exceptions.unwrap(e);
        while (t != null) {
            if (type.isInstance(t)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private static String bestMessage(Throwable e) {
        Throwable t = Exceptions.unwrap(e);
        while (t != null) {
            String msg = t.getMessage();
            if (msg != null && !msg.isBlank()) {
                return msg;
            }
            t = t.getCause();
        }
        return String.valueOf(e);
    }
}
