package com.shawn.aiagent.port.rag;

/**
 * 向量化网关接口
 * 定义文本向量化的抽象契约
 */
public interface EmbeddingGateway {
    
    /**
     * Intent: 获取向量维度
     * Input: 无
     * Output: int (向量维度，如1024)
     * SideEffects: 无（查询配置信息）
     * Failure: 如果无法获取维度信息，抛出RuntimeException
     * Idempotency: 幂等（相同配置返回相同维度）
     */
    int getDimensions();
}

