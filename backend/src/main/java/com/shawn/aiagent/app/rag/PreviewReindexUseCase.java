package com.shawn.aiagent.app.rag;

import com.shawn.aiagent.domain.rag.ReindexPreview;
import reactor.core.publisher.Mono;

/**
 * 预览重新索引用例接口
 * 定义预览重新索引操作的业务契约
 */
public interface PreviewReindexUseCase {
    
    /**
     * Intent: 预览重新索引操作（不实际执行索引）
     * Input: 无
     * Output: Mono<ReindexPreview> (预览结果，包含chunk数量、表名、模型名和维度)
     * SideEffects: 仅读取数据源和配置信息，不写入数据库
     * Failure: 如果数据源不可访问或配置错误，抛出RuntimeException
     * Idempotency: 幂等（相同配置返回相同结果）
     */
    Mono<ReindexPreview> execute();
}

