package com.shawn.aiagent.app.chat;

import com.shawn.aiagent.domain.chat.ConversationId;
import com.shawn.aiagent.domain.chat.Message;
import com.shawn.aiagent.port.chat.ChatModelGateway;
import com.shawn.aiagent.support.constants.CharacterCards;
import com.shawn.aiagent.support.constants.ResponseFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 与心理医生聊天用例实现
 * 编排与心理医生的对话流程
 */
@Component
@Slf4j
public class ChatWithPsychiatristUseCaseImpl implements ChatWithPsychiatristUseCase {
    
    private final ChatModelGateway chatModelGateway;
    private final String systemPrompt;
    
    public ChatWithPsychiatristUseCaseImpl(
            ChatModelGateway chatModelGateway,
            @Value("${app.chat.system-prompt:}") String systemPrompt) {
        this.chatModelGateway = chatModelGateway;
        // 如果配置为空，使用默认提示词
        this.systemPrompt = systemPrompt != null && !systemPrompt.isEmpty() 
                ? systemPrompt 
                : getDefaultSystemPrompt();
    }
    
    @Override
    public Flux<String> streamChat(Message message, ConversationId conversationId) {
        log.info("开始流式聊天，会话ID: {}, 消息长度: {}", 
                conversationId.getValue(), message.getContent().length());
        
        try {
            // 使用默认会话ID如果未提供
            ConversationId effectiveConversationId = conversationId != null 
                    ? conversationId 
                    : ConversationId.defaultId();
            
            // 调用Gateway进行流式对话
            Flux<String> response = chatModelGateway.streamChat(
                    message, 
                    effectiveConversationId, 
                    systemPrompt
            );
            
            log.debug("流式聊天请求已发送，会话ID: {}", effectiveConversationId.getValue());
            return response;
        } catch (Exception e) {
            log.error("流式聊天失败，会话ID: {}, 错误: {}", 
                    conversationId != null ? conversationId.getValue() : "default", 
                    e.getMessage(), e);
            return Flux.error(new RuntimeException("流式聊天失败: " + e.getMessage(), e));
        }
    }
    
    /**
     * 获取默认系统提示词
     * 使用support/constants中定义的常量
     */
    private String getDefaultSystemPrompt() {
        return CharacterCards.CUTY_CHARACTER_CARD + ResponseFormat.HUMAN_LIKE_RESPONSE_NEW_LINE;
    }
}

