package com.shawn.aiagent.infra.rag.embeddingClient.integration;

import com.shawn.aiagent.infra.rag.config.DashScopeEmbeddingConfig;
import com.shawn.aiagent.infra.rag.config.EmbeddingGatewayWiringConfig;
import com.shawn.aiagent.infra.rag.config.WebClientConfig;
import com.shawn.aiagent.infra.rag.config.RestClientConfig;
import com.shawn.aiagent.port.rag.SlaEmbeddingGateway;
import com.shawn.aiagent.port.rag.ReindexEmbeddingGateway;
import com.shawn.aiagent.port.rag.EmbeddingGateway;
import com.shawn.aiagent.support.config.RetrievalConfig;
import com.shawn.aiagent.support.timeoutSemanticClassifier.TimeoutSemanticClassifier;
import com.shawn.aiagent.support.timeoutSemanticClassifier.TimeoutSemanticClassifierImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * I1：response-timeout 必须在 client 本地生效。
 */
@SpringBootTest(classes = {
        WebClientConfig.class,
        RestClientConfig.class,
        DashScopeEmbeddingConfig.class,
        EmbeddingGatewayWiringConfig.class,
        RetrievalConfig.class,
        TimeoutSemanticClassifierImpl.class
})
class EmbeddingClientResponseTimeoutIT {

    private static final MockWebServer server = new MockWebServer();
    private static final int SLA_CONNECT_TIMEOUT_MS = 200;
    private static final long SLA_RESPONSE_TIMEOUT_MS = 200L;
    private static final int REINDEX_CONNECT_TIMEOUT_MS = 200;
    private static final long REINDEX_RESPONSE_TIMEOUT_MS = 200L;

    @BeforeAll
    static void setUp() throws IOException {
        server.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        server.shutdown();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.ai.dashscope.api-key", () -> "test-sk");
        registry.add("spring.ai.dashscope.embedding.api-key", () -> "test-sk");
        registry.add("spring.ai.dashscope.embedding.base-url", () -> server.url("/").toString());
        // WebClient (SLA) 使用极短超时，确保由 client 本地触发
        registry.add("app.webclient.sla.base-url", () -> server.url("/").toString());
        registry.add("app.webclient.sla.connect-timeout-ms", () -> SLA_CONNECT_TIMEOUT_MS);
        registry.add("app.webclient.sla.response-timeout-ms", () -> String.valueOf(SLA_RESPONSE_TIMEOUT_MS));
        registry.add("app.webclient.reindex.base-url", () -> server.url("/").toString());
        registry.add("app.webclient.reindex.connect-timeout-ms", () -> REINDEX_CONNECT_TIMEOUT_MS);
        registry.add("app.webclient.reindex.response-timeout-ms", () -> String.valueOf(REINDEX_RESPONSE_TIMEOUT_MS));
        // 避免日志截断太长
        registry.add("rag.retrieval.logging.query-preview-length", () -> 16);
    }

    @Autowired
    private SlaEmbeddingGateway slaEmbeddingGateway;

    @Autowired
    private ReindexEmbeddingGateway reindexEmbeddingGateway;

    @Autowired
    private TimeoutSemanticClassifier timeoutSemanticClassifier;

    @Test
    void givenSlowServerWhenEmbedThenClientTimesOutLocally() {
        assertGatewayTimesOutWithinBounds(slaEmbeddingGateway, SLA_RESPONSE_TIMEOUT_MS);
    }

    @Test
    void givenSlowServerWhenReindexEmbedThenClientTimesOutLocally() {
        assertGatewayTimesOutWithinBounds(reindexEmbeddingGateway, REINDEX_RESPONSE_TIMEOUT_MS);
    }

    private void assertGatewayTimesOutWithinBounds(EmbeddingGateway gateway, long responseTimeoutMs) {
        int before = server.getRequestCount();
        enqueueDelayedResponse(responseTimeoutMs);
        long start = System.currentTimeMillis();
        assertThatThrownBy(() -> gateway.embed("hello world"))
                .satisfies(this::assertTimeoutSemantic);
        long elapsed = System.currentTimeMillis() - start;
        // 断言在 response-timeout 的有限倍数内返回，未无限阻塞
        assertThat(elapsed).isLessThan(responseTimeoutMs * 4);
        assertThat(server.getRequestCount() - before).isEqualTo(1);
    }

    private void enqueueDelayedResponse(long responseTimeoutMs) {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBodyDelay(responseTimeoutMs * 5, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setBody("{}"));
    }

    private void assertTimeoutSemantic(Throwable throwable) {
        assertThat(timeoutSemanticClassifier.isTimeout(throwable))
                .as("期待异常链包含超时语义，但实际: %s", throwable)
                .isTrue();
    }
}

