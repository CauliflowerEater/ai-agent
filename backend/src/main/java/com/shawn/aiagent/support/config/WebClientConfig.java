package com.shawn.aiagent.support.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * 全局 WebClient.Builder 配置，覆盖默认自动配置，提供超时等基础设置。
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder(
            @Value("${app.webclient.default.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${app.webclient.default.response-timeout:10s}") Duration responseTimeout) {

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(buildHttpClient(connectTimeoutMs, responseTimeout)));
    }

    @Bean(name = "slaWebClientBuilder")
    public WebClient.Builder slaWebClientBuilder(
            @Value("${app.webclient.sla.base-url:}") String baseUrl,
            @Value("${app.webclient.sla.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${app.webclient.sla.response-timeout:5s}") Duration responseTimeout) {

        WebClient.Builder builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(buildHttpClient(connectTimeoutMs, responseTimeout)));

        if (StringUtils.hasText(baseUrl)) {
            builder.baseUrl(baseUrl);
        }
        return builder;
    }

    @Bean(name = "reindexWebClientBuilder")
    public WebClient.Builder reindexWebClientBuilder(
            @Value("${app.webclient.reindex.connect-timeout-ms:8000}") int connectTimeoutMs,
            @Value("${app.webclient.reindex.response-timeout:30s}") Duration responseTimeout) {

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(buildHttpClient(connectTimeoutMs, responseTimeout)));
    }

    private HttpClient buildHttpClient(int connectTimeoutMs, Duration responseTimeout) {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(responseTimeout);
    }
}

