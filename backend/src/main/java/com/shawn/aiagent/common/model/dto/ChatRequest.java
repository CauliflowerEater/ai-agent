package com.shawn.aiagent.common.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 聊天请求
 */
@Data
public class ChatRequest implements Serializable {

    /**
     * 用户消息
     */
    private String message;

    /**
     * 会话ID（可选，默认为"1"）
     */
    private String chatId;

    private static final long serialVersionUID = 1L;
}
