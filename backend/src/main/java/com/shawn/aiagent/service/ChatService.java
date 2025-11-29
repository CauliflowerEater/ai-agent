package com.shawn.aiagent.service;

import com.shawn.aiagent.app.PsychiatristApp;
import com.shawn.aiagent.exception.BusinessException;
import com.shawn.aiagent.exception.ErrorCode;
import com.shawn.aiagent.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;

/**
 * 聊天服务
 */
@Service
@Slf4j
public class ChatService {

    @Resource
    private PsychiatristApp psychiatristApp;

    /**
     * 执行流式聊天
     *
     * @param message 用户消息
     * @param chatId  会话ID
     * @return AI回复流
     */
    public Flux<String> doChatStream(String message, String chatId) {
        ThrowUtils.throwIf(message == null || message.trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "消息不能为空");

        try {
            // 调用PsychiatristApp的doChat方法
            if (chatId != null && !chatId.trim().isEmpty()) {
                return psychiatristApp.doChatStream(message, chatId);
            } else {
                return psychiatristApp.doChatStream(message);
            }
        } catch (Exception e) {
            log.error("聊天失败", e);
            //检查异常的处理方式是否同统一；
            //todo
            return Flux.error(new BusinessException(ErrorCode.SYSTEM_ERROR, "聊天失败：" + e.getMessage()));
        }
    }
}
