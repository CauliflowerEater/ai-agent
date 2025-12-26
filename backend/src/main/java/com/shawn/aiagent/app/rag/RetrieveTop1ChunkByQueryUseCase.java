package com.shawn.aiagent.app.rag;

import com.shawn.aiagent.domain.rag.RetrievalResult;
import reactor.core.publisher.Mono;

/**
 * 基于查询语句检索最相似的 chunk
 */
public interface RetrieveTop1ChunkByQueryUseCase {

    /**
     * Intent: 对输入 query 进行向量化并执行向量检索，返回 top-1 chunk
     * Input: query (必填), requestId (可选，用于日志关联)
     * Output: Mono<RetrievalResult> (top-1 结果)
     * SideEffects: 调用外部 EmbeddingModel 与 pgvector 查询
     * Failure: 输入不合法/超时/网络/配置错误时抛出 BusinessException
     * Idempotency: 非幂等（每次可能触发新的 embedding 调用）
     */
    Mono<RetrievalResult> execute(String query, String requestId);
}


