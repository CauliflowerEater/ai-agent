package com.shawn.aiagent.port.rag;

import com.shawn.aiagent.domain.rag.DocumentChunk;

import java.util.List;

/**
 * 文档加载网关接口
 * 定义从数据源加载文档的抽象契约
 */
public interface DocumentLoaderGateway {
    
    /**
     * Intent: 从数据源加载文档块
     * Input: 无（数据源由实现决定）
     * Output: List<DocumentChunk> (文档块列表)
     * SideEffects: 可能读取文件系统、数据库或HTTP资源
     * Failure: 如果数据源不可访问或数据格式错误，抛出RuntimeException
     * Idempotency: 幂等（相同数据源返回相同结果）
     */
    List<DocumentChunk> loadDocuments();
}

