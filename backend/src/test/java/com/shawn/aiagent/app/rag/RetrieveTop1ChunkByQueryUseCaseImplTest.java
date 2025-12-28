package com.shawn.aiagent.app.rag;

import com.shawn.aiagent.api.error.ErrorCode;
import com.shawn.aiagent.api.exception.BusinessException;
import com.shawn.aiagent.domain.rag.RetrievalResult;
import com.shawn.aiagent.port.rag.EmbeddingGateway;
import com.shawn.aiagent.port.rag.VectorStoreGateway;
import com.shawn.aiagent.support.config.RetrievalConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetrieveTop1ChunkByQueryUseCaseImplTest {

    @Mock
    private EmbeddingGateway embeddingGateway;

    @Mock
    private VectorStoreGateway vectorStoreGateway;

    private RetrieveTop1ChunkByQueryUseCaseImpl newUseCase(int maxLen, int embeddingTimeoutSec, int vectorTimeoutSec, int totalTimeoutSec) {
        RetrievalConfig config = new RetrievalConfig();
        config.setMaxQueryLength(maxLen);
        config.setTimeoutEmbeddingSeconds(embeddingTimeoutSec);
        config.setTimeoutVectorSearchSeconds(vectorTimeoutSec);
        config.setTimeoutTotalSeconds(totalTimeoutSec);
        return new RetrieveTop1ChunkByQueryUseCaseImpl(embeddingGateway, vectorStoreGateway, config);
    }

    private List<Double> sampleEmbedding() {
        return List.of(0.1, 0.2, 0.3);
    }

    private RetrievalResult sampleResult() {
        return new RetrievalResult("chunk-1", "text-1", 0.88, Map.of("chapter", "1"));
    }

    private void assertBusinessException(Throwable throwable, ErrorCode expected) {
        assertTrue(throwable instanceof BusinessException);
        BusinessException ex = (BusinessException) throwable;
        assertEquals(expected.getCode(), ex.getCode());
    }

    @Test
    void givenNullOrBlankQueryWhenExecuteThenInvalidQuery() {
        RetrieveTop1ChunkByQueryUseCaseImpl useCase = newUseCase(10, 5, 5, 5);

        StepVerifier.create(useCase.execute("   ", "req-blank"))
                .expectErrorSatisfies(e -> assertBusinessException(e, ErrorCode.INVALID_QUERY))
                .verify();

        StepVerifier.create(useCase.execute(null, "req-null"))
                .expectErrorSatisfies(e -> assertBusinessException(e, ErrorCode.INVALID_QUERY))
                .verify();

        verifyNoInteractions(embeddingGateway, vectorStoreGateway);
    }

    @Test
    void givenTooLongQueryWhenExecuteThenInvalidQuery() {
        RetrieveTop1ChunkByQueryUseCaseImpl useCase = newUseCase(5, 5, 5, 5);
        String tooLong = "123456";

        StepVerifier.create(useCase.execute(tooLong, "req-long"))
                .expectErrorSatisfies(e -> assertBusinessException(e, ErrorCode.INVALID_QUERY))
                .verify();

        verifyNoInteractions(embeddingGateway, vectorStoreGateway);
    }

    @Test
    void givenQueryWithSurroundingSpacesWhenExecuteThenUsesTrimmedQuery() {
        RetrieveTop1ChunkByQueryUseCaseImpl useCase = newUseCase(50, 5, 5, 5);
        List<Double> embedding = sampleEmbedding();
        RetrievalResult expected = sampleResult();
        when(embeddingGateway.getDimensions()).thenReturn(embedding.size());
        when(embeddingGateway.embed("hello")).thenReturn(embedding);
        when(vectorStoreGateway.similaritySearch(eq("hello"), eq(embedding), eq(1)))
                .thenReturn(List.of(expected));

        StepVerifier.create(useCase.execute("  hello  ", "req-trim"))
                .expectNextMatches(r ->
                        r.getChunkId().equals(expected.getChunkId()) &&
                                r.getText().equals(expected.getText()) &&
                                r.getScore() == expected.getScore() &&
                                r.getMetadata().equals(expected.getMetadata()))
                .verifyComplete();

        verify(embeddingGateway).embed("hello");
        verify(vectorStoreGateway).similaritySearch("hello", embedding, 1);
    }

    @Test
    void givenEmbeddingTimeoutWhenExecuteThenEmbeddingTimeout() {
        RetrieveTop1ChunkByQueryUseCaseImpl useCase = newUseCase(50, 1, 5, 5);

        when(embeddingGateway.getDimensions()).thenReturn(sampleEmbedding().size());
        when(embeddingGateway.embed(eq("q"))).thenThrow(new TimeoutException("embedding timeout"));

        StepVerifier.create(useCase.execute("q", "req-embed-timeout"))
                .expectErrorSatisfies(e -> assertBusinessException(e, ErrorCode.EMBEDDING_TIMEOUT))
                .verify();

        verify(embeddingGateway).embed("q");
        verifyNoInteractions(vectorStoreGateway);
    }

    @Test
    void givenVectorSearchTimeoutWhenExecuteThenVectorSearchTimeout() {
        RetrieveTop1ChunkByQueryUseCaseImpl useCase = newUseCase(50, 5, 1, 5);
        List<Double> embedding = sampleEmbedding();
        when(embeddingGateway.getDimensions()).thenReturn(embedding.size());
        when(embeddingGateway.embed(eq("query"))).thenReturn(embedding);
        when(vectorStoreGateway.similaritySearch(eq("query"), eq(embedding), eq(1)))
                .thenThrow(new TimeoutException("vector search timeout"));

        StepVerifier.create(useCase.execute("query", "req-vector-timeout"))
                .expectErrorSatisfies(e -> assertBusinessException(e, ErrorCode.VECTOR_SEARCH_TIMEOUT))
                .verify();

        verify(embeddingGateway).embed("query");
        verify(vectorStoreGateway).similaritySearch("query", embedding, 1);
    }

    @Test
    void givenTotalTimeoutWhenExecuteThenTotalTimeout() {
        RetrieveTop1ChunkByQueryUseCaseImpl useCase = newUseCase(50, 5, 5, 0);

        StepVerifier.create(useCase.execute("query", "req-total-timeout"))
                .expectErrorSatisfies(e -> assertBusinessException(e, ErrorCode.TOTAL_TIMEOUT))
                .verify();

        verifyNoInteractions(embeddingGateway, vectorStoreGateway);
    }

    @Test
    void givenValidQueryWhenSearchThenReturnTop1AndUseTopKOne() {
        RetrieveTop1ChunkByQueryUseCaseImpl useCase = newUseCase(50, 5, 5, 5);
        List<Double> embedding = sampleEmbedding();
        RetrievalResult first = sampleResult();
        when(embeddingGateway.getDimensions()).thenReturn(embedding.size());
        when(embeddingGateway.embed("q")).thenReturn(embedding);

        ArgumentCaptor<Integer> topKCaptor = ArgumentCaptor.forClass(Integer.class);
        when(vectorStoreGateway.similaritySearch(eq("q"), eq(embedding), topKCaptor.capture()))
                .thenReturn(List.of(first));

        StepVerifier.create(useCase.execute("q", "req-top1"))
                .expectNextMatches(r ->
                        r.getChunkId().equals(first.getChunkId()) &&
                                r.getText().equals(first.getText()) &&
                                r.getScore() == first.getScore() &&
                                r.getMetadata().equals(first.getMetadata()))
                .verifyComplete();

        assertThat(topKCaptor.getValue()).isEqualTo(1);
        verify(vectorStoreGateway).similaritySearch("q", embedding, 1);
        // TODO: 集成测试版本：使用真实 VectorStore 实例验证 top-1 与字段映射
    }

    @Test
    void givenVectorReturnsEmptyWhenExecuteThenNotFound() {
        RetrieveTop1ChunkByQueryUseCaseImpl useCase = newUseCase(50, 5, 5, 5);
        List<Double> embedding = sampleEmbedding();
        when(embeddingGateway.getDimensions()).thenReturn(embedding.size());
        when(embeddingGateway.embed(eq("query"))).thenReturn(embedding);
        when(vectorStoreGateway.similaritySearch(eq("query"), eq(embedding), eq(1))).thenReturn(List.of());

        StepVerifier.create(useCase.execute("query", "req-not-found"))
                .expectErrorSatisfies(e -> assertBusinessException(e, ErrorCode.RETRIEVAL_NOT_FOUND))
                .verify();

        verify(vectorStoreGateway).similaritySearch("query", embedding, 1);
    }

    @Test
    void givenFailureWhenExecuteThenNoImplicitRetry() {
        RetrieveTop1ChunkByQueryUseCaseImpl useCase = newUseCase(50, 5, 5, 5);
        when(embeddingGateway.getDimensions()).thenReturn(sampleEmbedding().size());
        when(embeddingGateway.embed(eq("query"))).thenThrow(new RuntimeException("downstream failure"));

        StepVerifier.create(useCase.execute("query", "req-no-retry"))
                .expectErrorSatisfies(e -> assertBusinessException(e, ErrorCode.EMBEDDING_API_ERROR))
                .verify();

        verify(embeddingGateway).embed("query");
        verify(vectorStoreGateway, never()).similaritySearch(eq("query"), eq(sampleEmbedding()), eq(1));
    }
}
