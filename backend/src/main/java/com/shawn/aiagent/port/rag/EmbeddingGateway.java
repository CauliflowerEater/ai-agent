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
     * Input: text (必填，UTF-8 文本；需去除首尾空白后非空，长度不应超过配置上限)
     * Output: List<Double> (向量；维度需与 getDimensions 一致)
     * SideEffects: 调用外部 EmbeddingModel API，可能产生计费；一次调用只触发一次远端请求，不做隐式重试
     * Failure: 网络/超时/配置错误时抛出 RuntimeException（需保留可识别的超时语义，供 TimeoutSemanticClassifier 使用）
     * Idempotency: 幂等（同输入理论上返回同向量，但不承诺结果级幂等/计费幂等）
     */
    java.util.List<Double> embed(String text);
}

