package com.shawn.aiagent.infra.chat;

import com.shawn.aiagent.domain.chat.ConversationId;
import com.shawn.aiagent.domain.chat.Message;
import com.shawn.aiagent.port.chat.ChatModelGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * DashScope聊天模型适配器
 * 实现ChatModelGateway接口，使用Spring AI DashScope
 */
@Component
@Slf4j
public class DashScopeChatModelAdapter implements ChatModelGateway {
    
    private final ChatClient chatClient;
    
    public DashScopeChatModelAdapter(
            ChatModel chatModel,
            ChatMemory chatMemory) {
        // 构建ChatClient，配置默认Advisor
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }
    
    @Override
    public Flux<String> streamChat(Message message, ConversationId conversationId, String systemPrompt) {
        log.debug("开始流式聊天，会话ID: {}, 消息长度: {}", conversationId.getValue(), message.getContent().length());
        
        try {
            Flux<String> response = chatClient
                    .prompt()
                    .system(systemPrompt != null ? systemPrompt : "")
                    .user(message.getContent())
                    .advisors(spec -> spec.param(CONVERSATION_ID, conversationId.getValue()))
                    .stream()
                    .content();
            
            log.debug("流式聊天请求已发送，会话ID: {}", conversationId.getValue());
            return response;
        } catch (Exception e) {
            log.error("流式聊天失败，会话ID: {}, 错误: {}", conversationId.getValue(), e.getMessage(), e);
            throw new RuntimeException("流式聊天失败: " + e.getMessage(), e);
        }
    }
}

