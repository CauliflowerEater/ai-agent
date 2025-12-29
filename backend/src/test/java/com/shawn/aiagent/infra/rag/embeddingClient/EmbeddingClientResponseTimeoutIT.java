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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * I1：response-timeout 必须在 client 本地生效。
 */
@SpringBootTest(classes = {
        WebClientConfig.class,
        DashScopeEmbeddingConfig.class,
        EmbeddingGatewayWiringConfig.class,
        RetrievalConfig.class
})
class EmbeddingClientResponseTimeoutIT {

    private static final MockWebServer server = new MockWebServer();
    private static final int SLA_CONNECT_TIMEOUT_MS = 200;
    private static final long SLA_RESPONSE_TIMEOUT_MS = 200L;

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
        // 避免日志截断太长
        registry.add("rag.retrieval.logging.query-preview-length", () -> 16);
    }

    @Autowired
    @Qualifier("slaEmbeddingGateway")
    private SlaEmbeddingGateway embeddingGateway;

    @Test
    void givenSlowServerWhenEmbedThenClientTimesOutLocally() {
        server.enqueue(new MockResponse()
                // 不返回任何响应体，强制触发客户端超时
                .setSocketPolicy(SocketPolicy.NO_RESPONSE));

        long start = System.currentTimeMillis();

        assertThatThrownBy(() -> embeddingGateway.embed("hello world"))
                .satisfies(this::assertTimeoutSemantic);

        long elapsed = System.currentTimeMillis() - start;
        // 断言在 response-timeout 的有限倍数内返回，未无限阻塞
        assertThat(elapsed).isLessThan(SLA_RESPONSE_TIMEOUT_MS * 4);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    private void assertTimeoutSemantic(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor != null) {
            if (cursor instanceof ReadTimeoutException
                    || cursor instanceof java.util.concurrent.TimeoutException
                    || cursor instanceof java.net.SocketTimeoutException) {
                return;
            }
            cursor = cursor.getCause();
        }
        String msg = throwable.getMessage();
        if (isTimeoutMessage(msg)) {
            return;
        }
        throw new AssertionError("Expected a timeout-related cause but got: " + throwable);
    }

    private boolean isTimeoutMessage(String msg) {
        if (msg == null) {
            return false;
        }
        String lower = msg.toLowerCase();
        return lower.contains("timeout") || lower.contains("timed out");
    }

}