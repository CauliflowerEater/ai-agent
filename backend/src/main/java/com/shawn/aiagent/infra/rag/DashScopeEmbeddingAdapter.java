package com.shawn.aiagent.infra.rag;

import com.shawn.aiagent.port.rag.ReindexEmbeddingGateway;
import com.shawn.aiagent.port.rag.SlaEmbeddingGateway;
import com.shawn.aiagent.support.config.RetrievalConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * DashScope向量化适配器
 * 实现EmbeddingGateway接口，使用Spring AI DashScope Embedding Model
 */
@Slf4j
public class DashScopeEmbeddingAdapter implements SlaEmbeddingGateway, ReindexEmbeddingGateway {

    private final EmbeddingModel embeddingModel;
    private final RetrievalConfig retrievalConfig;

    public DashScopeEmbeddingAdapter(EmbeddingModel embeddingModel, RetrievalConfig retrievalConfig) {
        if (embeddingModel == null) {
            throw new IllegalArgumentException("embeddingModel cannot be null");
        }
        this.embeddingModel = embeddingModel;
        this.retrievalConfig = retrievalConfig;
    }
    
    @Override
    public int getDimensions() {
        return embeddingModel.dimensions();
    }

    @Override
    public List<Double> embed(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        }
        String normalized = text.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("text cannot be empty");
        }

        logQueryPreview(normalized);

        try {
            List<float[]> vectors = embeddingModel.embed(List.of(normalized));
            if (vectors == null || vectors.isEmpty()) {
                throw new RuntimeException("embedding result is empty");
            }
            float[] first = vectors.get(0);
            List<Double> result = new ArrayList<>(first.length);
            for (float v : first) {
                result.add((double) v);
            }
            return result;
        } catch (RuntimeException e) {
            log.error("向量化失败: type={}, msg={}", e.getClass().getName(), e.getMessage(), e);
            throw e;
        }
    }

    private void logQueryPreview(String text) {
        try {
            int configuredLen = retrievalConfig != null ? retrievalConfig.getLogQueryPreviewLength() : 128;
            int previewLen = Math.min(configuredLen, text.length());
            String preview = text.substring(0, previewLen);
            preview = preview.replace("\n", " ").replace("\r", " ");
            String hash = sha256Base64(text);
            log.info("Embedding request: len={}, preview=\"{}\", hash={}", text.length(), preview, hash);
        } catch (Exception e) {
            log.warn("记录查询预览失败: {}", e.getMessage());
        }
    }

    private String sha256Base64(String text) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashed = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashed);
    }
}
