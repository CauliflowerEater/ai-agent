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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * I2：超时失败的异常形态需保留“超时语义”（cause 链可识别）。
 */
@SpringBootTest(classes = {
        WebClientConfig.class,
        RestClientConfig.class,
        DashScopeEmbeddingConfig.class,
        EmbeddingGatewayWiringConfig.class,
        RetrievalConfig.class,
        TimeoutSemanticClassifierImpl.class
})
class EmbeddingClientTimeoutExceptionShapeIT {

    private static final MockWebServer server = new MockWebServer();

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
        registry.add("app.webclient.sla.base-url", () -> server.url("/").toString());
        registry.add("app.webclient.sla.connect-timeout-ms", () -> 200);
        registry.add("app.webclient.sla.response-timeout-ms", () -> "200");
        registry.add("app.webclient.reindex.base-url", () -> server.url("/").toString());
        registry.add("app.webclient.reindex.connect-timeout-ms", () -> 200);
        registry.add("app.webclient.reindex.response-timeout-ms", () -> "200");
        registry.add("rag.retrieval.logging.query-preview-length", () -> 16);
    }

    @Autowired
    @Qualifier("slaEmbeddingGateway")
    private SlaEmbeddingGateway slaEmbeddingGateway;

    @Autowired
    @Qualifier("reindexEmbeddingGateway")
    private ReindexEmbeddingGateway reindexEmbeddingGateway;

    @Autowired
    private TimeoutSemanticClassifier timeoutSemanticClassifier;

    @Test
    void givenTimeoutWhenEmbedThenCauseChainHasTimeoutSemantic() {
        assertTimeoutSemanticForGateway(slaEmbeddingGateway);
    }

    @Test
    void givenTimeoutWhenReindexEmbedThenCauseChainHasTimeoutSemantic() {
        assertTimeoutSemanticForGateway(reindexEmbeddingGateway);
    }

    private void assertTimeoutSemanticForGateway(EmbeddingGateway gateway) {
        server.enqueue(new MockResponse()
                .setBodyDelay(700, TimeUnit.MILLISECONDS)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(sampleEmbeddingResponse()));

        assertThatThrownBy(() -> gateway.embed("cause chain"))
                .satisfies(ex -> assertThat(timeoutSemanticClassifier.isTimeout(ex))
                        .as("Expected timeout-related cause but got: %s", ex)
                        .isTrue());
    }

    private String sampleEmbeddingResponse() {
        return """
                {
                  "request_id": "req-2",
                  "output": {
                    "embeddings": [
                      {
                        "embedding": [0.01, 0.02, 0.03],
                        "text_index": 0
                      }
                    ]
                  },
                  "usage": { "total_tokens": 3 }
                }
                """;
    }
}

