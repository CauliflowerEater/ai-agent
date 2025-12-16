package com.shawn.aiagent.controller;

import com.shawn.aiagent.common.ResultUtils;
import com.shawn.aiagent.common.model.BaseResponse;
import com.shawn.aiagent.common.model.DryRunResult;
import com.shawn.aiagent.common.model.ReindexResult;
import com.shawn.aiagent.service.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import jakarta.annotation.Resource;

/**
 * RAG 数据摄取接口
 * 提供重新索引（reindex）功能
 */
@RestController
@RequestMapping("/rag")
@Slf4j
public class RagController {

    @Resource
    private RagService ragService;

    /**
     * 执行重新索引（reindex）或 dryRun 预览
     * 根据 dryRun 参数决定执行实际索引操作还是预览操作
     * 
     * @param dryRun 是否执行 dryRun 预览（默认为 true）
     *                - true: 返回 chunk 数量、collection 名、embedding 模型名和维度，但不实际执行索引操作
     *                - false: 执行实际的重新索引操作，从数据源加载文档，进行向量化处理，并写入向量存储
     * @return Mono<BaseResponse<Object>> 重新索引结果或 dryRun 预览结果
     */
    @GetMapping("/reindex")
    public Mono<BaseResponse<Object>> reindex(
            @RequestParam(value = "dryRun", defaultValue = "true") boolean dryRun) {
        log.info("收到重新索引请求，dryRun={}", dryRun);
        
        if (dryRun) {
            return executeDryRun()
                    .map(response -> new BaseResponse<>(response.getCode(), (Object) response.getData(), response.getMessage()));
        } else {
            return executeReindex()
                    .map(response -> new BaseResponse<>(response.getCode(), (Object) response.getData(), response.getMessage()));
        }
    }

    /**
     * 执行 dryRun 预览（私有方法）
     */
    private Mono<BaseResponse<DryRunResult>> executeDryRun() {
        return ragService.dryRun()
                .map(ResultUtils::success)
                .onErrorResume(e -> {
                    log.error("dryRun 预览失败", e);
                    return Mono.just(new BaseResponse<>(500, null, "dryRun 预览失败: " + e.getMessage()));
                });
    }

    /**
     * 执行实际的重新索引操作（私有方法）
     */
    private Mono<BaseResponse<ReindexResult>> executeReindex() {
        return ragService.reindex()
                .map(ResultUtils::success)
                .onErrorResume(e -> {
                    log.error("重新索引失败", e);
                    return Mono.just(new BaseResponse<>(500, null, "重新索引失败: " + e.getMessage()));
                });
    }
}

