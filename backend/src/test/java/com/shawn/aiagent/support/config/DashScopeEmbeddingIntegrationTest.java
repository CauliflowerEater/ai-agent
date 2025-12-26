package com.shawn.aiagent.support.config;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 集成验证：自定义 DashScopeEmbeddingModel + 自定义 WebClient 能否真实发起请求。
 */
class DashScopeEmbeddingIntegrationTest {

    private static MockWebServer server;

    @BeforeAll
    static void startServer() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void stopServer() throws Exception {
        server.shutdown();
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(WebClientConfig.class, DashScopeEmbeddingConfig.class)
            .withPropertyValues(
                    // 使用自定义 Bean，关闭自动配置
                    "spring.ai.dashscope.embedding.enabled=false",
                    // DashScopeEmbeddingProperties 读取的 apiKey/baseUrl
                    "spring.ai.dashscope.api-key=dummy-key",
                    "spring.ai.dashscope.embedding.api-key=dummy-key",
                    "spring.ai.dashscope.embedding.base-url=${mock.base-url}",
                    // WebClient 超时可按需调整
                    "app.webclient.reindex.connect-timeout-ms=1500",
                    "app.webclient.reindex.response-timeout-ms=3000"
            )
            // 动态注入 mock.base-url
            .withInitializer(ctx -> ctx.getEnvironment().getSystemProperties()
                    .put("mock.base-url", server.url("/").toString()));

    @Test
    void should_send_embedding_request_via_custom_webclient() throws Exception {
        // 模拟 DashScope embedding 返回体（最简结构）
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "request_id": "req-1",
                          "output": {
                            "embeddings": [
                              { "text_index": 0, "embedding": [0.1, 0.2, 0.3] }
                            ]
                          },
                          "usage": { "total_tokens": 1 }
                        }
                        """));

        contextRunner.run(ctx -> {
            EmbeddingModel model = ctx.getBean(EmbeddingModel.class); // 默认 reindexEmbeddingModel

            // 触发一次 embedding 调用
            List<float[]> result = model.embed(List.of("hello"));

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).containsExactly(0.1f, 0.2f, 0.3f);

            RecordedRequest req = server.takeRequest(1, TimeUnit.SECONDS);
            assertThat(req).isNotNull();
            assertThat(req.getMethod()).isEqualTo("POST");
            assertThat(req.getPath()).contains("embeddings");
            assertThat(req.getHeader("Authorization")).isEqualTo("Bearer dummy-key");
            assertThat(req.getBody().readUtf8()).contains("hello");
        });
    }
}


