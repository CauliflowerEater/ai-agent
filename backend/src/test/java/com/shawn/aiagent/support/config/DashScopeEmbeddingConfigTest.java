package com.shawn.aiagent.support.config;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class DashScopeEmbeddingConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                    WebClientConfig.class,
                    DashScopeEmbeddingConfig.class
            )
            // 关闭自动配置的默认 embedding，使用自定义 Bean
            .withPropertyValues(
                    "spring.ai.dashscope.embedding.enabled=false",
                    "spring.ai.dashscope.api-key=dummy-key",
                    "spring.ai.dashscope.embedding.api-key=dummy-key"
            );

    @Test
    void shouldCreateReindexAndSlaEmbeddingModels() {
        contextRunner.run(ctx -> {
            EmbeddingModel reindex = ctx.getBean(EmbeddingModel.class);
            EmbeddingModel sla = ctx.getBean("slaEmbeddingModel", EmbeddingModel.class);

            assertThat(reindex).isInstanceOf(DashScopeEmbeddingModel.class);
            assertThat(sla).isInstanceOf(DashScopeEmbeddingModel.class);
            assertThat(reindex).isNotSameAs(sla);
        });
    }

    @TestConfiguration
    static class TestRestClientConfig {
        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }

        @Bean(name = "reindexWebClientBuilder")
        WebClient.Builder reindexWebClientBuilder() {
            return WebClient.builder();
        }

        @Bean(name = "slaWebClientBuilder")
        WebClient.Builder slaWebClientBuilder() {
            return WebClient.builder();
        }
    }
}


