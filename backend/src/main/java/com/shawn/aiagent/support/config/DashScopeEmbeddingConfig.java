package com.shawn.aiagent.support.config;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeEmbeddingProperties;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * DashScope Embedding 多实例配置，按场景（SLA / Reindex）拆分。
 */
@Configuration
@EnableConfigurationProperties(DashScopeEmbeddingProperties.class)
public class DashScopeEmbeddingConfig {

    @Bean
    @Primary
    public EmbeddingModel reindexEmbeddingModel(
            DashScopeEmbeddingProperties props,
            @Qualifier("reindexWebClientBuilder") WebClient.Builder reindexWebClientBuilder,
            RestClient.Builder restClientBuilder) {

        DashScopeApi api = buildDashScopeApi(props, restClientBuilder, reindexWebClientBuilder);
        return new DashScopeEmbeddingModel(api, resolveMetadataMode(props), resolveOptions(props));
    }

    @Bean(name = "slaEmbeddingModel")
    public EmbeddingModel slaEmbeddingModel(
            DashScopeEmbeddingProperties props,
            @Qualifier("slaWebClientBuilder") WebClient.Builder slaWebClientBuilder,
            RestClient.Builder restClientBuilder) {

        DashScopeApi api = buildDashScopeApi(props, restClientBuilder, slaWebClientBuilder);
        return new DashScopeEmbeddingModel(api, resolveMetadataMode(props), resolveOptions(props));
    }

    private DashScopeApi buildDashScopeApi(DashScopeEmbeddingProperties props,
                                           RestClient.Builder restClientBuilder,
                                           WebClient.Builder webClientBuilder) {
        String apiKey = StringUtils.hasText(props.getApiKey()) ? props.getApiKey() : props.getSecretKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("DashScope API key (spring.ai.dashscope.api-key) 不能为空");
        }

        DashScopeApi.Builder builder = new DashScopeApi.Builder()
                .apiKey(apiKey)
                .headers(new LinkedMultiValueMap<>())
                .restClientBuilder(restClientBuilder)
                .webClientBuilder(webClientBuilder)
                .responseErrorHandler(RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);

        if (StringUtils.hasText(props.getBaseUrl())) {
            builder.baseUrl(props.getBaseUrl());
        } else {
            builder.baseUrl(DashScopeApiConstants.DEFAULT_BASE_URL);
        }
        if (StringUtils.hasText(props.getWorkspaceId())) {
            builder.workSpaceId(props.getWorkspaceId());
        }

        return builder.build();
    }

    private MetadataMode resolveMetadataMode(DashScopeEmbeddingProperties props) {
        return props.getMetadataMode() != null ? props.getMetadataMode() : MetadataMode.EMBED;
    }

    private DashScopeEmbeddingOptions resolveOptions(DashScopeEmbeddingProperties props) {
        return props.getOptions() != null
                ? props.getOptions()
                : DashScopeEmbeddingOptions.builder()
                    .withModel(DashScopeApi.DEFAULT_EMBEDDING_MODEL)
                    .withTextType(DashScopeApi.DEFAULT_EMBEDDING_TEXT_TYPE)
                    .build();
    }
}


