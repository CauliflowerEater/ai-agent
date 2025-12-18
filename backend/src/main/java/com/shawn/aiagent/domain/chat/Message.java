package com.shawn.aiagent.domain.chat;

import java.util.Objects;

/**
 * 消息值对象
 * 封装聊天消息内容，确保不可变性
 */
public final class Message {
    
    private final String content;
    
    private Message(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }
        this.content = content.trim();
    }
    
    /**
     * Intent: 创建消息值对象
     * Input: content (消息内容字符串)
     * Output: Message实例
     * SideEffects: 无
     * Failure: 如果content为null或空，抛出IllegalArgumentException
     * Idempotency: 幂等
     */
    public static Message of(String content) {
        return new Message(content);
    }
    
    public String getContent() {
        return content;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(content, message.content);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(content);
    }
    
    @Override
    public String toString() {
        return "Message{content='" + content + "'}";
    }
}

