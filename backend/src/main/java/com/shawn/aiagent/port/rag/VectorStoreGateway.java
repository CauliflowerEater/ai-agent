package com.shawn.aiagent.port.rag;

import com.shawn.aiagent.domain.rag.DocumentChunk;

import java.util.List;

/**
 * 向量存储网关接口
 * 定义向量存储操作的抽象契约
 */
public interface VectorStoreGateway {
    
    /**
     * Intent: 批量添加文档块到向量存储
     * Input: chunks (文档块列表)
     * Output: 无
     * SideEffects: 将文档块向量化并写入向量数据库
     * Failure: 如果向量化失败或数据库写入失败，抛出RuntimeException
     * Idempotency: 非幂等（重复调用会重复写入，可能导致重复数据）
     */
    void addDocuments(List<DocumentChunk> chunks);
    
    /**
     * Intent: 删除指定ID的文档块
     * Input: documentIds (文档ID列表)
     * Output: 无
     * SideEffects: 从向量数据库删除指定文档
     * Failure: 如果数据库删除失败，抛出RuntimeException
     * Idempotency: 幂等（删除不存在的文档不会报错）
     */
    void deleteDocuments(List<String> documentIds);
}

