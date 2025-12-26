package com.shawn.aiagent.support.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class WebClientConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(WebClientConfig.class);

    @Test
    void slaWebClientBuilder_ShouldBuildClient() {
        contextRunner
                .withPropertyValues(
                        "app.webclient.sla.base-url=https://sla.example.com",
                        "app.webclient.sla.connect-timeout-ms=1234",
                        "app.webclient.sla.response-timeout-ms=6000"
                )
                .run(ctx -> {
                    WebClient.Builder builder = ctx.getBean("slaWebClientBuilder", WebClient.Builder.class);
                    WebClient client = builder.build();
                    assertThat(client).isNotNull();
                });
    }

    @Test
    void reindexWebClientBuilder_ShouldExist() {
        contextRunner
                .withPropertyValues(
                        "app.webclient.reindex.connect-timeout-ms=4321",
                        "app.webclient.reindex.response-timeout-ms=20000"
                )
                .run(ctx -> {
                    WebClient.Builder builder = ctx.getBean("reindexWebClientBuilder", WebClient.Builder.class);
                    assertThat(builder).isNotNull();
                });
    }
}


