package com.shawn.aiagent.api.rag;

import com.shawn.aiagent.app.rag.PreviewReindexUseCase;
import com.shawn.aiagent.app.rag.ReindexDocumentsUseCase;
import com.shawn.aiagent.domain.rag.ReindexPreview;
import com.shawn.aiagent.domain.rag.ReindexResult;
import com.shawn.aiagent.support.response.ApiResponse;
import com.shawn.aiagent.support.response.ApiResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import jakarta.annotation.Resource;

/**
 * RAG数据摄取接口控制器
 * 处理RAG相关的HTTP请求
 */
@RestController
@RequestMapping("/rag")
@Slf4j
public class RagController {

    @Resource
    private ReindexDocumentsUseCase reindexDocumentsUseCase;

    @Resource
    private PreviewReindexUseCase previewReindexUseCase;

    /**
     * Intent: 执行重新索引或预览操作
     * Input: dryRun (是否预览，默认为true)
     * Output: Mono<ApiResponse<Object>> (重新索引结果或预览结果)
     * SideEffects: 调用UseCase执行重新索引或预览
     * Failure: 如果操作失败，返回错误响应
     * Idempotency: 预览操作幂等，重新索引操作非幂等
     */
    @GetMapping("/reindex")
    public Mono<ApiResponse<Object>> reindex(
            @RequestParam(value = "dryRun", defaultValue = "true") boolean dryRun) {
        log.info("收到重新索引请求，dryRun={}", dryRun);
        
        if (dryRun) {
            return executePreview();
        } else {
            return executeReindex();
        }
    }

    /**
     * Intent: 执行预览操作
     * Input: 无
     * Output: Mono<ApiResponse<Object>> (预览结果)
     * SideEffects: 调用PreviewReindexUseCase
     * Failure: 如果预览失败，返回错误响应
     * Idempotency: 幂等
     */
    @SuppressWarnings("unchecked")
    private Mono<ApiResponse<Object>> executePreview() {
        return previewReindexUseCase.execute()
                .map(this::toPreviewResponse)
                .map(preview -> (ApiResponse<Object>) (ApiResponse<?>) ApiResponseBuilder.success(preview))
                .onErrorResume(e -> {
                    log.error("预览重新索引失败", e);
                    return Mono.just((ApiResponse<Object>) (ApiResponse<?>) ApiResponseBuilder.error(50000, "预览重新索引失败: " + e.getMessage()));
                });
    }

    /**
     * Intent: 执行重新索引操作
     * Input: 无
     * Output: Mono<ApiResponse<Object>> (重新索引结果)
     * SideEffects: 调用ReindexDocumentsUseCase
     * Failure: 如果重新索引失败，返回错误响应
     * Idempotency: 非幂等
     */
    @SuppressWarnings("unchecked")
    private Mono<ApiResponse<Object>> executeReindex() {
        return reindexDocumentsUseCase.execute()
                .map(this::toReindexResponse)
                .map(response -> (ApiResponse<Object>) (ApiResponse<?>) ApiResponseBuilder.success(response))
                .onErrorResume(e -> {
                    log.error("重新索引失败", e);
                    return Mono.just((ApiResponse<Object>) (ApiResponse<?>) ApiResponseBuilder.error(50000, "重新索引失败: " + e.getMessage()));
                });
    }

    /**
     * 将领域对象ReindexResult转换为DTO
     */
    private ReindexResponse toReindexResponse(ReindexResult result) {
        ReindexResponse response = new ReindexResponse();
        response.setDocumentCount(result.getDocumentCount());
        response.setMessage(result.getMessage());
        return response;
    }

    /**
     * 将领域对象ReindexPreview转换为DTO
     */
    private ReindexPreviewResponse toPreviewResponse(ReindexPreview preview) {
        ReindexPreviewResponse response = new ReindexPreviewResponse();
        response.setChunkCount(preview.getChunkCount());
        response.setTableName(preview.getTableName());
        response.setEmbeddingModelName(preview.getEmbeddingModelName());
        response.setEmbeddingDim(preview.getEmbeddingDim());
        return response;
    }
}

