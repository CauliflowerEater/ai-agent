package com.shawn.aiagent.port.chat;

import com.shawn.aiagent.domain.chat.ConversationId;
import com.shawn.aiagent.domain.chat.Message;
import reactor.core.publisher.Flux;

/**
 * 聊天模型网关接口
 * 定义与LLM进行对话的抽象契约
 */
public interface ChatModelGateway {
    
    /**
     * Intent: 流式调用LLM进行对话
     * Input: message (用户消息), conversationId (会话ID), systemPrompt (系统提示词)
     * Output: Flux<String> (AI回复流，每个元素是一个文本片段)
     * SideEffects: 可能更新对话记忆（由实现决定）
     * Failure: 如果LLM调用失败，抛出RuntimeException
     * Idempotency: 非幂等（相同输入可能产生不同输出）
     */
    Flux<String> streamChat(Message message, ConversationId conversationId, String systemPrompt);
}

