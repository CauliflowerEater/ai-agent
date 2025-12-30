package com.shawn.aiagent.support.timeoutSemanticClassifier;

/**
 * 识别异常是否具备“超时语义”的纯逻辑接口。
 */
public interface TimeoutSemanticClassifier {

    /**
     * Intent: 识别异常（含完整 cause 链）是否具备“超时语义”，为上层错误分类与映射提供稳定信号。
     * Input: 任意 Throwable，可为 null；允许被多层包装。
     * Output: boolean；true 表示存在可识别的超时语义，false 表示未识别到或无法确定。
     * SideEffects: 无；纯逻辑函数，不修改输入对象。
     * Failure: 不抛出异常；即使输入为 null 或包含循环 cause 也需返回确定值。
     * Idempotency: 对同一输入重复调用结果一致。
     */
    boolean isTimeout(Throwable error);
}

