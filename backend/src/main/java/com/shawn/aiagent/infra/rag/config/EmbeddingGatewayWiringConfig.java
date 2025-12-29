package com.shawn.aiagent.infra.rag.config;

import com.shawn.aiagent.infra.rag.DashScopeEmbeddingAdapter;
import com.shawn.aiagent.port.rag.ReindexEmbeddingGateway;
import com.shawn.aiagent.port.rag.SlaEmbeddingGateway;
import com.shawn.aiagent.support.config.RetrievalConfig;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring config: bind different EmbeddingModel beans (SLA vs Reindex) to different Port types.
 *
 * Notes:
 * - This class lives in infra because it wires SDK/framework beans to Port interfaces.
 * - The actual EmbeddingModel beans (e.g. "slaEmbeddingModel", "reindexEmbeddingModel")
 *   should be provided by another infra config (e.g. DashScopeEmbeddingConfig).
 */
@Configuration
public class EmbeddingGatewayWiringConfig {

    @Bean
    public SlaEmbeddingGateway slaEmbeddingGateway(
            @Qualifier("slaEmbeddingModel") EmbeddingModel slaEmbeddingModel,
            RetrievalConfig retrievalConfig) {
        return new DashScopeEmbeddingAdapter(slaEmbeddingModel, retrievalConfig);
    }

    @Bean
    public ReindexEmbeddingGateway reindexEmbeddingGateway(
            @Qualifier("reindexEmbeddingModel") EmbeddingModel reindexEmbeddingModel,
            RetrievalConfig retrievalConfig) {
        return new DashScopeEmbeddingAdapter(reindexEmbeddingModel, retrievalConfig);
    }
}
