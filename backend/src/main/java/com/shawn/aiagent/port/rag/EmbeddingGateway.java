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

    /**
     * Intent: 将文本转换为向量
     * Input: text (必填，UTF-8 文本)
     * Output: List<Double> (向量)
     * SideEffects: 调用外部 EmbeddingModel API，可能产生计费
     * Failure: 网络/超时/配置错误时抛出 RuntimeException（上层映射到 ErrorCode）
     * Idempotency: 幂等（同输入理论上返回同向量，但不承诺结果级幂等）
     */
    java.util.List<Double> embed(String text);
}

