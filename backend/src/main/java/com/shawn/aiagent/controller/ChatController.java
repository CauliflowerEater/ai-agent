package com.shawn.aiagent.controller;

import com.shawn.aiagent.common.BaseResponse;
import com.shawn.aiagent.common.ResultUtils;
import com.shawn.aiagent.model.dto.ChatRequest;
import com.shawn.aiagent.model.dto.ChatResponse;
import com.shawn.aiagent.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


import jakarta.annotation.Resource;



/**
 * 聊天接口
 */
@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {

    @Resource
    private ChatService chatService;

    // 新的流式接口
    @PostMapping(
            value = "/send/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<ServerSentEvent<String>> sendMessageStream(@RequestBody ChatRequest chatRequest) {
        String message = chatRequest.getMessage();
        String chatId = chatRequest.getChatId();

        return chatService.doChatStream(message, chatId)
                .map(chunk -> ServerSentEvent.builder(chunk).build());
    }

}
