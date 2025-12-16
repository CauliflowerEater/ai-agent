package com.shawn.aiagent.rag.ingest;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 批次处理器
 * 负责将文档列表分批处理，并提供超时控制功能
 * 
 * 职责：
 * - 文档分批逻辑
 * - 批次超时控制
 * - 线程池管理
 */
@Slf4j
@Component
public class BatchProcessor {

    /**
     * 批量加载时的批次大小（每批处理的文档数量）
     * 默认值：100，可通过配置 rag.ingestion.batch-size 修改
     */
    @Value("${rag.ingestion.batch-size:100}")
    private int batchSize;

    /**
     * 每批加载的超时时间（秒）
     * 默认值：300秒（5分钟），可通过配置 rag.ingestion.batch-timeout-seconds 修改
     */
    @Value("${rag.ingestion.batch-timeout-seconds:300}")
    private int batchTimeoutSeconds;

    /**
     * 用于执行超时控制的线程池
     */
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 分批处理文档列表
     * 
     * @param documents 待处理的文档列表
     * @param batchProcessor 批次处理函数，接收一个批次的文档列表
     * @return 成功处理的文档数量
     * @throws TimeoutException 如果任何批次超时
     * @throws RuntimeException 如果任何批次处理失败
     */
    public int processInBatches(List<Document> documents, Consumer<List<Document>> batchProcessor) 
            throws TimeoutException {
        if (documents.isEmpty()) {
            return 0;
        }

        int totalBatches = (documents.size() + batchSize - 1) / batchSize;
        log.info("开始分批处理文档，总数: {}, 批次大小: {}, 总批次数: {}", 
                documents.size(), batchSize, totalBatches);

        AtomicInteger successCount = new AtomicInteger(0);

        // 将文档列表分割成批次
        for (int i = 0; i < documents.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, documents.size());
            List<Document> batch = documents.subList(i, endIndex);
            int batchNumber = (i / batchSize) + 1;

            log.info("开始处理第 {}/{} 批，文档数量: {}", batchNumber, totalBatches, batch.size());

            try {
                // 执行单批处理（带超时控制）
                processBatchWithTimeout(batch, batchNumber, totalBatches, batchProcessor);
                successCount.addAndGet(batch.size());
                log.info("第 {}/{} 批处理成功，已处理文档数: {}/{}", 
                        batchNumber, totalBatches, successCount.get(), documents.size());
            } catch (TimeoutException e) {
                log.error("第 {}/{} 批处理超时（超时时间: {}秒）", 
                        batchNumber, totalBatches, batchTimeoutSeconds, e);
                // 超时则停止后续批次处理
                throw new TimeoutException(String.format(
                        "第 %d/%d 批处理超时（超时时间: %d秒），已成功处理 %d/%d 个文档",
                        batchNumber, totalBatches, batchTimeoutSeconds, 
                        successCount.get(), documents.size()));
            } catch (Exception e) {
                log.error("第 {}/{} 批处理失败", batchNumber, totalBatches, e);
                // 其他异常也停止后续批次处理
                throw new RuntimeException(String.format(
                        "第 %d/%d 批处理失败，已成功处理 %d/%d 个文档: %s",
                        batchNumber, totalBatches, successCount.get(), 
                        documents.size(), e.getMessage()), e);
            }
        }

        return successCount.get();
    }

    /**
     * 处理单个批次（带超时控制）
     * 
     * @param batch 当前批次的文档列表
     * @param batchNumber 当前批次号
     * @param totalBatches 总批次数
     * @param batchProcessor 批次处理函数
     * @throws TimeoutException 如果处理超时
     * @throws Exception 如果处理失败
     */
    private void processBatchWithTimeout(
            List<Document> batch, 
            int batchNumber, 
            int totalBatches,
            Consumer<List<Document>> batchProcessor) 
            throws TimeoutException, Exception {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                batchProcessor.accept(batch);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executorService);

        try {
            // 等待完成或超时
            future.get(batchTimeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // 取消任务
            future.cancel(true);
            throw new TimeoutException(String.format(
                    "第 %d/%d 批处理超时（超时时间: %d秒）", 
                    batchNumber, totalBatches, batchTimeoutSeconds));
        } catch (ExecutionException e) {
            // 获取实际异常
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException && cause.getCause() != null) {
                throw (Exception) cause.getCause();
            }
            throw new RuntimeException("批次处理执行异常", cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("批次处理被中断", e);
        }
    }

    /**
     * 关闭线程池（用于资源清理）
     * Spring 容器关闭时自动调用
     */
    @PreDestroy
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

