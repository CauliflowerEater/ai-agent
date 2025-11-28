package com.shawn.aiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * 自定义日志 Advisor
 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 */
@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    @NotNull
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        // 你可以按需要调整优先级
        return 0;
    }

    /**
     * 前置处理：打印用户请求
     */
    private ChatClientRequest before(ChatClientRequest request) {
        try {
            // 打印整个 Prompt 的内容；你也可以只取最后一条 user message
            log.info("AI Request: {}", request.prompt().getContents());
            log.info("AI full prompt: {}", request.context());
        } catch (Exception e) {
            log.warn("Failed to log AI Request", e);
        }
        return request;
    }

    /**
     * 后置处理：打印 AI 回复文本
     */
    private void observeAfter(ChatClientResponse responseWrapper) {
        try {
            ChatResponse response = responseWrapper.chatResponse();
            if (response == null || response.getResult() == null
                    || response.getResult().getOutput() == null) {
                log.info("AI Response is null or empty");
                return;
            }
            String text = response.getResult().getOutput().getText();
            log.info("AI Response: {}", text);
        } catch (Exception e) {
            log.warn("Failed to log AI Response", e);
        }
    }

    /**
     * 同步调用拦截（原来的 aroundCall）
     */
    @Override
    @NotNull
    public ChatClientResponse adviseCall(@NotNull ChatClientRequest request,
            @NotNull CallAdvisorChain chain) {
        request = this.before(request);
        ChatClientResponse response = chain.nextCall(request);
        this.observeAfter(response);
        return response;
    }

    /**
     * 流式调用拦截（原来的 aroundStream）
     */
    @Override
    @NotNull
    public Flux<ChatClientResponse> adviseStream(@NotNull ChatClientRequest request,
            @NotNull StreamAdvisorChain chain) {
        request = this.before(request);
        Flux<ChatClientResponse> responses = chain.nextStream(request);
        // 聚合完整回复后再调用 observeAfter 进行日志打印
        return new ChatClientMessageAggregator()
                .aggregateChatClientResponse(responses, this::observeAfter);
    }
}