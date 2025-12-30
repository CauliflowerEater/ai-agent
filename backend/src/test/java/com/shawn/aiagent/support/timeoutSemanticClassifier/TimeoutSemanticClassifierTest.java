package com.shawn.aiagent.support.timeoutSemanticClassifier;

import org.junit.jupiter.api.Test;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class TimeoutSemanticClassifierTest {

    private final TimeoutSemanticClassifier classifier = new TimeoutSemanticClassifierImpl();

    @Test
    void shouldReturnFalseWhenInputIsNull() {
        assertThat(classifier.isTimeout(null)).isFalse();
    }

    @Test
    void shouldReturnTrueWhenTimeoutInNestedCauseChain() {
        Throwable error = new RuntimeException(new RuntimeException(new TimeoutException()));
        assertThat(classifier.isTimeout(error)).isTrue();
    }

    @Test
    void shouldRecognizeStandardTimeoutExceptions() {
        assertThat(classifier.isTimeout(new TimeoutException())).isTrue();
        assertThat(classifier.isTimeout(new SocketTimeoutException())).isTrue();
    }

    @Test
    void shouldRecognizeNettyAndReactorTimeoutByFqcn() {
        assertThat(classifier.isTimeout(new io.netty.handler.timeout.ReadTimeoutException())).isTrue();
        assertThat(classifier.isTimeout(new io.netty.handler.timeout.WriteTimeoutException())).isTrue();
        assertThat(classifier.isTimeout(new io.netty.channel.ConnectTimeoutException())).isTrue();
        assertThat(classifier.isTimeout(new reactor.netty.http.client.PrematureCloseException())).isTrue();
    }

    @Test
    void shouldReturnFalseForNonTimeoutExceptions() {
        assertThat(classifier.isTimeout(new IllegalArgumentException())).isFalse();
        assertThat(classifier.isTimeout(new NullPointerException())).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTimeoutSemanticIsUncertain() {
        assertThat(classifier.isTimeout(new RuntimeException("unknown"))).isFalse();
    }

    @Test
    void shouldBePureFunctionForSameInput() {
        Throwable error = new TimeoutException();
        assertThatCode(() -> classifier.isTimeout(error)).doesNotThrowAnyException();
        assertThat(classifier.isTimeout(error)).isTrue();
        assertThat(classifier.isTimeout(error)).isTrue();
    }
}
