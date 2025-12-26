package com.shawn.aiagent.support.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.regex.Pattern;

/**
 * 全局 WebClient.Builder 配置，覆盖默认自动配置，提供超时等基础设置。
 */
@Configuration
public class WebClientConfig {

    @Bean
        @Primary
    public WebClient.Builder webClientBuilder(
            @Value("${app.webclient.default.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${app.webclient.default.response-timeout-ms:10000}") String responseTimeoutMs) {

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(buildHttpClient(connectTimeoutMs, parseDurationMs(responseTimeoutMs))));
    }

    @Bean(name = "slaWebClientBuilder")
    public WebClient.Builder slaWebClientBuilder(
            @Value("${app.webclient.sla.base-url:}") String baseUrl,
            @Value("${app.webclient.sla.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${app.webclient.sla.response-timeout-ms:5000}") String responseTimeoutMs) {

        WebClient.Builder builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(buildHttpClient(connectTimeoutMs, parseDurationMs(responseTimeoutMs))));

        if (StringUtils.hasText(baseUrl)) {
            builder.baseUrl(baseUrl);
        }
        return builder;
    }

    @Bean(name = "reindexWebClientBuilder")
    public WebClient.Builder reindexWebClientBuilder(
            @Value("${app.webclient.reindex.connect-timeout-ms:8000}") int connectTimeoutMs,
            @Value("${app.webclient.reindex.response-timeout-ms:30000}") String responseTimeoutMs) {

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(buildHttpClient(connectTimeoutMs, parseDurationMs(responseTimeoutMs))));
    }

    /**
     * 提供 RestClient.Builder，供 DashScopeApi 等使用。
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    private HttpClient buildHttpClient(int connectTimeoutMs, Duration responseTimeout) {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(responseTimeout);
    }

    private static final Pattern DIGITS = Pattern.compile("^\\d+$");

    private Duration parseDurationMs(String raw) {
        if (DIGITS.matcher(raw).matches()) {
            return Duration.ofMillis(Long.parseLong(raw));
        }
        // 兼容已有 ISO-8601 格式
        return Duration.parse(raw);
    }
}

