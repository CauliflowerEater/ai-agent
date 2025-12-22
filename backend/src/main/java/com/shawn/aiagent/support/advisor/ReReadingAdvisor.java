package com.shawn.aiagent.support.advisor;

import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 自定义 Re2 Advisor
 * 让模型"重新读一遍问题"，以期提高推理质量
 */
public class ReReadingAdvisor implements CallAdvisor, StreamAdvisor {

    /**
     * 对请求做前置处理：
     * 把最后一条 user message 改成：
     *
     *   原问题
     *   Read the question again: 原问题
     */
    private ChatClientRequest before(ChatClientRequest chatClientRequest) {

        Prompt originalPrompt = chatClientRequest.prompt();

        Prompt augmentedPrompt = originalPrompt.augmentUserMessage(userMessage -> {
            String originalText = userMessage.getText();
            String newText = originalText
                    + System.lineSeparator()
                    + "Read the question again: " + originalText;

            // 用 mutate() 保留其他字段，只改 text
            return userMessage.mutate()
                    .text(newText)
                    .build();
        });

        // 保留原来的 context，仅替换 prompt
        return ChatClientRequest.builder()
                .prompt(augmentedPrompt)
                .context(Map.copyOf(chatClientRequest.context()))
                .build();
    }

    @Override
    @NotNull
    public ChatClientResponse adviseCall(@NotNull ChatClientRequest chatClientRequest,
            @NotNull CallAdvisorChain callAdvisorChain) {
        ChatClientRequest modified = before(chatClientRequest);
        return callAdvisorChain.nextCall(modified);
    }

    @Override
    @NotNull
    public Flux<ChatClientResponse> adviseStream(@NotNull ChatClientRequest chatClientRequest,
            @NotNull StreamAdvisorChain streamAdvisorChain) {
        ChatClientRequest modified = before(chatClientRequest);
        return streamAdvisorChain.nextStream(modified);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    @NotNull
    public String getName() {
        return this.getClass().getSimpleName();
    }
}

