package com.shawn.aiagent.app.chat;

import com.shawn.aiagent.domain.chat.ConversationId;
import com.shawn.aiagent.domain.chat.Message;
import reactor.core.publisher.Flux;

/**
 * 与心理医生聊天用例接口
 * 定义与心理医生进行流式对话的业务契约
 */
public interface ChatWithPsychiatristUseCase {
    
    /**
     * Intent: 与心理医生进行流式对话
     * Input: message (用户消息), conversationId (会话ID，可选，默认使用"1")
     * Output: Flux<String> (AI回复流，每个元素是一个文本片段)
     * SideEffects: 更新对话记忆（通过ChatModelGateway）
     * Failure: 如果消息为空或LLM调用失败，抛出RuntimeException
     * Idempotency: 非幂等（相同输入可能产生不同输出）
     */
    Flux<String> streamChat(Message message, ConversationId conversationId);
}

