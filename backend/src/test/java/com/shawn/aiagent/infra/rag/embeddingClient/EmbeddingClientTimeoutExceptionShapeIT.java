package com.shawn.aiagent.infra.rag.embeddingClient;

import com.shawn.aiagent.infra.rag.config.DashScopeEmbeddingConfig;
import com.shawn.aiagent.infra.rag.config.EmbeddingGatewayWiringConfig;
import com.shawn.aiagent.infra.rag.config.WebClientConfig;
import com.shawn.aiagent.port.rag.SlaEmbeddingGateway;
import com.shawn.aiagent.support.config.RetrievalConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import io.netty.handler.timeout.ReadTimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * I2：超时失败的异常形态需保留“超时语义”（cause 链可识别）。
 */
@SpringBootTest(classes = {
        WebClientConfig.class,
        DashScopeEmbeddingConfig.class,
        EmbeddingGatewayWiringConfig.class,
        RetrievalConfig.class
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
        registry.add("rag.retrieval.logging.query-preview-length", () -> 16);
    }

    @Autowired
    @Qualifier("slaEmbeddingGateway")
    private SlaEmbeddingGateway embeddingGateway;

    @Test
    void givenTimeoutWhenEmbedThenCauseChainHasTimeoutSemantic() {
        server.enqueue(new MockResponse()
                // 不返回任何响应体，强制触发客户端超时
                .setSocketPolicy(SocketPolicy.NO_RESPONSE));

        assertThatThrownBy(() -> embeddingGateway.embed("cause chain"))
                .satisfies(this::assertTimeoutSemantic);
    }

    private void assertTimeoutSemantic(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor != null) {
            if (cursor instanceof ReadTimeoutException
                    || cursor instanceof TimeoutException
                    || cursor instanceof java.net.SocketTimeoutException) {
                String msg = cursor.getMessage();
                if (msg != null && !msg.isEmpty()) {
                    assertThat(isTimeoutMessage(msg)).isTrue();
                }
                return;
            }
            cursor = cursor.getCause();
        }
        String msg = throwable.getMessage();
        if (isTimeoutMessage(msg)) {
            return;
        }
        throw new AssertionError("Expected timeout-related cause but got: " + throwable);
    }

    private boolean isTimeoutMessage(String msg) {
        if (msg == null) {
            return false;
        }
        String lower = msg.toLowerCase();
        return lower.contains("timeout") || lower.contains("timed out");
    }

}