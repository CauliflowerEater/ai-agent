package com.shawn.aiagent.api.chat;

import com.shawn.aiagent.api.exception.BusinessException;
import com.shawn.aiagent.api.error.ErrorCode;
import com.shawn.aiagent.app.chat.ChatWithPsychiatristUseCase;
import com.shawn.aiagent.domain.chat.ConversationId;
import com.shawn.aiagent.domain.chat.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;

/**
 * 聊天接口控制器
 * 处理聊天相关的HTTP请求
 */
@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {

    @Resource
    private ChatWithPsychiatristUseCase chatWithPsychiatristUseCase;

    /**
     * Intent: 流式聊天接口
     * Input: chatRequest (聊天请求DTO)
     * Output: Flux<ServerSentEvent<String>> (SSE流式响应)
     * SideEffects: 调用UseCase进行流式对话
     * Failure: 如果请求参数无效，抛出BusinessException
     * Idempotency: 非幂等
     */
    @PostMapping(
            value = "/send/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<ServerSentEvent<String>> sendMessageStream(@RequestBody ChatRequest chatRequest) {
        log.info("收到流式聊天请求，chatId: {}", chatRequest.getChatId());
        
        // 参数验证
        if (chatRequest.getMessage() == null || chatRequest.getMessage().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息不能为空");
        }
        
        // 转换为领域对象
        Message message = Message.of(chatRequest.getMessage());
        ConversationId conversationId = chatRequest.getChatId() != null && !chatRequest.getChatId().trim().isEmpty()
                ? ConversationId.of(chatRequest.getChatId())
                : ConversationId.defaultId();
        
        // 调用UseCase
        Flux<String> responseStream = chatWithPsychiatristUseCase.streamChat(message, conversationId);
        
        // 转换为SSE格式
        return responseStream
                .map(chunk -> ServerSentEvent.builder(chunk).build())
                .doOnError(error -> log.error("流式聊天失败", error));
    }
}

