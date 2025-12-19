package com.shawn.aiagent.infra.rag;

import com.shawn.aiagent.port.rag.EmbeddingGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * DashScope向量化适配器
 * 实现EmbeddingGateway接口，使用Spring AI DashScope Embedding Model
 */
@Component
@Slf4j
public class DashScopeEmbeddingAdapter implements EmbeddingGateway {
    
    @Resource
    private EmbeddingModel embeddingModel;
    
    @Override
    public int getDimensions() {
        try {
            int dimensions = embeddingModel.dimensions();
            log.debug("获取向量维度: {}", dimensions);
            return dimensions;
        } catch (Exception e) {
            log.error("获取向量维度失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取向量维度失败: " + e.getMessage(), e);
        }
    }
}

