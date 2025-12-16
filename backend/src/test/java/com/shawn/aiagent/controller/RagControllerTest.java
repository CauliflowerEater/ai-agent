package com.shawn.aiagent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.aiagent.common.model.BaseResponse;
import com.shawn.aiagent.common.model.DryRunResult;
import com.shawn.aiagent.common.model.ReindexResult;
import com.shawn.aiagent.service.RagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RagController 测试类
 * 测试 RAG 数据摄取接口的功能
 */
@WebFluxTest(controllers = RagController.class)
@DisplayName("RagController 测试")
class RagControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RagService ragService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 每个测试前重置 mock
        reset(ragService);
    }

    @Test
    @DisplayName("测试 dryRun=true（默认值）- 成功场景")
    void testReindex_DryRunTrue_Success() {
        // 准备测试数据
        DryRunResult dryRunResult = new DryRunResult();
        dryRunResult.setChunkCount(100);
        dryRunResult.setCollectionName("test_collection");
        dryRunResult.setEmbeddingModelName("text-embedding-v2");
        dryRunResult.setEmbeddingDim(1536);

        // Mock RagService 行为
        when(ragService.dryRun()).thenReturn(Mono.just(dryRunResult));

        // 执行测试（不传 dryRun 参数，使用默认值 true）
        webTestClient.get()
                .uri("/rag/reindex")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BaseResponse.class)
                .value(response -> {
                    assertEquals(200, response.getCode());
                    assertNotNull(response.getData());
                    
                    // 验证响应数据
                    DryRunResult data = 
                            objectMapper.convertValue(response.getData(), DryRunResult.class);
                    assertEquals(100, data.getChunkCount());
                    assertEquals("test_collection", data.getCollectionName());
                    assertEquals("text-embedding-v2", data.getEmbeddingModelName());
                    assertEquals(1536, data.getEmbeddingDim());
                });

        // 验证 RagService 被调用
        verify(ragService, times(1)).dryRun();
        verify(ragService, never()).reindex();
    }

    @Test
    @DisplayName("测试 dryRun=true（显式指定）- 成功场景")
    void testReindex_DryRunTrue_Explicit_Success() {
        // 准备测试数据
        DryRunResult dryRunResult = new DryRunResult();
        dryRunResult.setChunkCount(200);
        dryRunResult.setCollectionName("dreams_collection");
        dryRunResult.setEmbeddingModelName("text-embedding-v3");
        dryRunResult.setEmbeddingDim(2048);

        // Mock RagService 行为
        when(ragService.dryRun()).thenReturn(Mono.just(dryRunResult));

        // 执行测试（显式指定 dryRun=true）
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rag/reindex")
                        .queryParam("dryRun", "true")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(BaseResponse.class)
                .value(response -> {
                    assertEquals(200, response.getCode());
                    assertNotNull(response.getData());
                    
                    DryRunResult data = 
                            objectMapper.convertValue(response.getData(), DryRunResult.class);
                    assertEquals(200, data.getChunkCount());
                    assertEquals("dreams_collection", data.getCollectionName());
                    assertEquals("text-embedding-v3", data.getEmbeddingModelName());
                    assertEquals(2048, data.getEmbeddingDim());
                });

        // 验证 RagService 被调用
        verify(ragService, times(1)).dryRun();
        verify(ragService, never()).reindex();
    }

    @Test
    @DisplayName("测试 dryRun=false - 成功场景")
    void testReindex_DryRunFalse_Success() {
        // 准备测试数据
        ReindexResult reindexResult = new ReindexResult();
        reindexResult.setDocumentCount(50);
        reindexResult.setMessage("重新索引成功");

        // Mock RagService 行为
        when(ragService.reindex()).thenReturn(Mono.just(reindexResult));

        // 执行测试
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rag/reindex")
                        .queryParam("dryRun", "false")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(BaseResponse.class)
                .value(response -> {
                    assertEquals(200, response.getCode());
                    assertNotNull(response.getData());
                    
                    ReindexResult data = 
                            objectMapper.convertValue(response.getData(), ReindexResult.class);
                    assertEquals(50, data.getDocumentCount());
                    assertEquals("重新索引成功", data.getMessage());
                });

        // 验证 RagService 被调用
        verify(ragService, times(1)).reindex();
        verify(ragService, never()).dryRun();
    }

    @Test
    @DisplayName("测试 dryRun=true - 失败场景")
    void testReindex_DryRunTrue_Failure() {
        // Mock RagService 抛出异常
        when(ragService.dryRun()).thenReturn(
                Mono.error(new RuntimeException("dryRun 预览失败: 文件不存在")));

        // 执行测试
        webTestClient.get()
                .uri("/rag/reindex")
                .exchange()
                .expectStatus().isOk() // Controller 捕获异常并返回 500 状态码
                .expectBody(BaseResponse.class)
                .value(response -> {
                    assertEquals(500, response.getCode());
                    assertNull(response.getData());
                    assertTrue(response.getMessage().contains("dryRun 预览失败"));
                });

        // 验证 RagService 被调用
        verify(ragService, times(1)).dryRun();
    }

    @Test
    @DisplayName("测试 dryRun=false - 失败场景")
    void testReindex_DryRunFalse_Failure() {
        // Mock RagService 抛出异常
        when(ragService.reindex()).thenReturn(
                Mono.error(new RuntimeException("重新索引失败: 数据库连接失败")));

        // 执行测试
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rag/reindex")
                        .queryParam("dryRun", "false")
                        .build())
                .exchange()
                .expectStatus().isOk() // Controller 捕获异常并返回 500 状态码
                .expectBody(BaseResponse.class)
                .value(response -> {
                    assertEquals(500, response.getCode());
                    assertNull(response.getData());
                    assertTrue(response.getMessage().contains("重新索引失败"));
                });

        // 验证 RagService 被调用
        verify(ragService, times(1)).reindex();
    }

    @Test
    @DisplayName("测试 dryRun 参数边界值 - false 字符串")
    void testReindex_DryRunFalse_String() {
        // 准备测试数据
        ReindexResult reindexResult = new ReindexResult();
        reindexResult.setDocumentCount(30);
        reindexResult.setMessage("重新索引成功");

        // Mock RagService 行为
        when(ragService.reindex()).thenReturn(Mono.just(reindexResult));

        // 执行测试（使用字符串 "false"）
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rag/reindex")
                        .queryParam("dryRun", "false")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(BaseResponse.class)
                .value(response -> {
                    assertEquals(200, response.getCode());
                    assertNotNull(response.getData());
                });

        // 验证调用了 reindex 而不是 dryRun
        verify(ragService, times(1)).reindex();
        verify(ragService, never()).dryRun();
    }

    @Test
    @DisplayName("测试响应数据结构 - DryRunResult")
    void testDryRunResult_DataStructure() {
        // 准备测试数据
        DryRunResult dryRunResult = new DryRunResult();
        dryRunResult.setChunkCount(0);
        dryRunResult.setCollectionName("");
        dryRunResult.setEmbeddingModelName("");
        dryRunResult.setEmbeddingDim(0);

        // Mock RagService 行为
        when(ragService.dryRun()).thenReturn(Mono.just(dryRunResult));

        // 执行测试
        webTestClient.get()
                .uri("/rag/reindex")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BaseResponse.class)
                .value(response -> {
                    assertEquals(200, response.getCode());
                    assertNotNull(response.getData());
                    
                    // 验证可以正确转换为 DryRunResult
                    DryRunResult data = 
                            objectMapper.convertValue(response.getData(), DryRunResult.class);
                    assertNotNull(data);
                    assertEquals(0, data.getChunkCount());
                    assertEquals("", data.getCollectionName());
                    assertEquals("", data.getEmbeddingModelName());
                    assertEquals(0, data.getEmbeddingDim());
                });
    }

    @Test
    @DisplayName("测试响应数据结构 - ReindexResult")
    void testReindexResult_DataStructure() {
        // 准备测试数据
        ReindexResult reindexResult = new ReindexResult();
        reindexResult.setDocumentCount(0);
        reindexResult.setMessage("");

        // Mock RagService 行为
        when(ragService.reindex()).thenReturn(Mono.just(reindexResult));

        // 执行测试
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rag/reindex")
                        .queryParam("dryRun", "false")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(BaseResponse.class)
                .value(response -> {
                    assertEquals(200, response.getCode());
                    assertNotNull(response.getData());
                    
                    // 验证可以正确转换为 ReindexResult
                    ReindexResult data = 
                            objectMapper.convertValue(response.getData(), ReindexResult.class);
                    assertNotNull(data);
                    assertEquals(0, data.getDocumentCount());
                    assertEquals("", data.getMessage());
                });
    }
}

