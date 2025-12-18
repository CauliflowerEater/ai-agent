package com.shawn.aiagent.domain.chat;

import java.util.Objects;

/**
 * 会话ID值对象
 * 封装会话标识符，确保类型安全和不可变性
 */
public final class ConversationId {
    
    private final String value;
    
    private ConversationId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("ConversationId cannot be null or empty");
        }
        this.value = value.trim();
    }
    
    /**
     * Intent: 创建会话ID值对象
     * Input: value (字符串形式的会话ID)
     * Output: ConversationId实例
     * SideEffects: 无
     * Failure: 如果value为null或空，抛出IllegalArgumentException
     * Idempotency: 幂等
     */
    public static ConversationId of(String value) {
        return new ConversationId(value);
    }
    
    /**
     * Intent: 获取默认会话ID
     * Input: 无
     * Output: ConversationId实例（值为"1"）
     * SideEffects: 无
     * Failure: 无
     * Idempotency: 幂等
     */
    public static ConversationId defaultId() {
        return new ConversationId("1");
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConversationId that = (ConversationId) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}

