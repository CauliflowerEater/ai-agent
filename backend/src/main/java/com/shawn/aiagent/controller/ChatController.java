package com.shawn.aiagent.controller;

import com.shawn.aiagent.common.BaseResponse;
import com.shawn.aiagent.common.ResultUtils;
import com.shawn.aiagent.model.dto.ChatRequest;
import com.shawn.aiagent.model.dto.ChatResponse;
import com.shawn.aiagent.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 发送消息
     *
     * @param chatRequest 聊天请求
     * @return AI回复
     */
    @PostMapping("/send")
    public BaseResponse<ChatResponse> sendMessage(@RequestBody ChatRequest chatRequest) {
        String message = chatRequest.getMessage();
        String chatId = chatRequest.getChatId();

        // 调用服务层
        String reply = chatService.doChat(message, chatId);

        // 构造响应
        ChatResponse response = new ChatResponse(reply, chatId != null ? chatId : "1");
        return ResultUtils.success(response);
    }

    /**
     * 测试接口
     *
     * @return 测试消息
     */
    @GetMapping("/test")
    public BaseResponse<String> test() {
        return ResultUtils.success("Chat API is working!");
    }
}
