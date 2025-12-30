package com.shawn.aiagent.support.timeoutSemanticClassifier;

import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * 默认超时语义分类器实现，遵循 timeoutSemanticClassifier 的契约规范。
 */
@Component
public class TimeoutSemanticClassifierImpl implements TimeoutSemanticClassifier {

    private static final Set<String> KNOWN_TIMEOUT_FQCN = Set.of(
            "io.netty.handler.timeout.ReadTimeoutException",
            "io.netty.handler.timeout.WriteTimeoutException",
            "io.netty.channel.ConnectTimeoutException",
            "reactor.netty.http.client.PrematureCloseException"
    );

    @Override
    public boolean isTimeout(Throwable error) {
        if (error == null) {
            return false;
        }

        // 防止异常链中存在环导致死循环
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable cursor = error;
        while (cursor != null && visited.add(cursor)) {
            if (hasTimeoutSemantic(cursor)) {
                return true;
            }
            cursor = cursor.getCause();
        }
        return false;
    }

    private boolean hasTimeoutSemantic(Throwable throwable) {
        if (throwable instanceof TimeoutException || throwable instanceof SocketTimeoutException) {
            return true;
        }
        String fqcn = throwable.getClass().getName();
        return KNOWN_TIMEOUT_FQCN.contains(fqcn);
    }
}

