package com.shawn.aiagent.infra.rag.config;

import com.shawn.aiagent.support.time.DurationParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * RestClient.Builder 配置，按场景（default / SLA / reindex）提供不同超时。
 */
@Configuration
public class RestClientConfig {

    @Bean
    @Primary
    public RestClient.Builder restClientBuilder(
            @Value("${app.webclient.default.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${app.webclient.default.response-timeout-ms:10000}") String responseTimeoutMs) {
        return buildRestClient(connectTimeoutMs, responseTimeoutMs);
    }

    @Bean(name = "slaRestClientBuilder")
    public RestClient.Builder slaRestClientBuilder(
            @Value("${app.webclient.sla.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${app.webclient.sla.response-timeout-ms:5000}") String responseTimeoutMs) {
        return buildRestClient(connectTimeoutMs, responseTimeoutMs);
    }

    @Bean(name = "reindexRestClientBuilder")
    public RestClient.Builder reindexRestClientBuilder(
            @Value("${app.webclient.reindex.connect-timeout-ms:8000}") int connectTimeoutMs,
            @Value("${app.webclient.reindex.response-timeout-ms:30000}") String responseTimeoutMs) {
        return buildRestClient(connectTimeoutMs, responseTimeoutMs);
    }

    private RestClient.Builder buildRestClient(int connectTimeoutMs, String responseTimeoutMs) {
        Duration rt = parseDurationMs(responseTimeoutMs);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout((int) rt.toMillis());
        return RestClient.builder().requestFactory(factory);
    }

    private Duration parseDurationMs(String raw) {
        return DurationParser.parseMsOrIso(raw);
    }
}

