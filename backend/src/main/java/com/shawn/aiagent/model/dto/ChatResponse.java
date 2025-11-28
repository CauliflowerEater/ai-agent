package com.shawn.aiagent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 聊天响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse implements Serializable {

    /**
     * AI回复内容
     */
    private String reply;

    /**
     * 会话ID
     */
    private String chatId;

    private static final long serialVersionUID = 1L;
}
