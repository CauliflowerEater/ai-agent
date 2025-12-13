package com.shawn.aiagent.rag.embedding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * Embedding 服务
 * 负责文本向量化，基于 Spring AI Alibaba DashScope Embedding Model
 */
@Service
@Slf4j
public class EmbeddingService {

    @Resource
    private EmbeddingModel embeddingModel;

    /**
     * 将单个文本转换为向量
     *
     * @param text 待转换的文本
     * @return 向量（float 数组）
     */
    public float[] embedText(String text) {
        log.debug("开始向量化文本，长度: {}", text.length());
        try {
            float[] vector = embeddingModel.embed(text);
            log.debug("文本向量化成功，向量维度: {}", vector.length);
            return vector;
        } catch (Exception e) {
            log.error("文本向量化失败: {}", e.getMessage(), e);
            throw new RuntimeException("文本向量化失败", e);
        }
    }

    /**
     * 批量将文本转换为向量
     *
     * @param texts 待转换的文本列表
     * @return 向量列表（float 数组列表）
     */
    public List<float[]> embedBatch(List<String> texts) {
        log.debug("开始批量向量化，文本数量: {}", texts.size());
        try {
            List<float[]> vectors = embeddingModel.embed(texts);
            log.debug("批量向量化成功，向量数量: {}", vectors.size());
            return vectors;
        } catch (Exception e) {
            log.error("批量文本向量化失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量文本向量化失败", e);
        }
    }

    /**
     * 将 Document 对象转换为向量
     *
     * @param document Spring AI Document 对象
     * @return 向量（float 数组）
     */
    public float[] embedDocument(Document document) {
        log.debug("开始向量化 Document，ID: {}", document.getId());
        try {
            float[] vector = embeddingModel.embed(document);
            log.debug("Document 向量化成功，向量维度: {}", vector.length);
            return vector;
        } catch (Exception e) {
            log.error("Document 向量化失败: {}", e.getMessage(), e);
            throw new RuntimeException("Document 向量化失败", e);
        }
    }

    /**
     * 获取完整的 Embedding 响应（包含元数据）
     *
     * @param texts 待转换的文本列表
     * @return EmbeddingResponse 对象
     */
    public EmbeddingResponse embedForResponse(List<String> texts) {
        log.debug("开始获取完整 Embedding 响应，文本数量: {}", texts.size());
        try {
            EmbeddingResponse response = embeddingModel.embedForResponse(texts);
            log.debug("获取 Embedding 响应成功，结果数量: {}", response.getResults().size());
            return response;
        } catch (Exception e) {
            log.error("获取 Embedding 响应失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取 Embedding 响应失败", e);
        }
    }

    /**
     * 使用自定义请求进行向量化
     *
     * @param request 自定义的 EmbeddingRequest
     * @return EmbeddingResponse 对象
     */
    public EmbeddingResponse embed(EmbeddingRequest request) {
        log.debug("开始处理自定义 Embedding 请求");
        try {
            EmbeddingResponse response = embeddingModel.call(request);
            log.debug("自定义 Embedding 请求处理成功");
            return response;
        } catch (Exception e) {
            log.error("自定义 Embedding 请求处理失败: {}", e.getMessage(), e);
            throw new RuntimeException("自定义 Embedding 请求处理失败", e);
        }
    }

    /**
     * 获取 Embedding 向量的维度
     *
     * @return 向量维度
     */
    public int getDimensions() {
        try {
            int dimensions = embeddingModel.dimensions();
            log.debug("Embedding 向量维度: {}", dimensions);
            return dimensions;
        } catch (Exception e) {
            log.error("获取向量维度失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取向量维度失败", e);
        }
    }
}
