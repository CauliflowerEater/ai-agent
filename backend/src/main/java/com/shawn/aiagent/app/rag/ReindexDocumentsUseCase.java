package com.shawn.aiagent.app.rag;

import com.shawn.aiagent.domain.rag.ReindexResult;
import reactor.core.publisher.Mono;

/**
 * 重新索引文档用例接口
 * 定义重新索引文档的业务契约
 */
public interface ReindexDocumentsUseCase {
    
    /**
     * Intent: 重新索引文档到向量存储
     * Input: 无
     * Output: Mono<ReindexResult> (重新索引结果，包含成功加载的文档数量)
     * SideEffects: 从数据源加载文档，向量化并写入向量数据库
     * Failure: 如果数据源不可访问、向量化失败或数据库写入失败，抛出RuntimeException
     *          如果操作正在进行中，返回ReindexResult.inProgress()
     * Idempotency: 非幂等（重复调用会重复写入数据）
     */
    Mono<ReindexResult> execute();
}

