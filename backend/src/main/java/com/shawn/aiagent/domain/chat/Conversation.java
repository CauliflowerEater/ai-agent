package com.shawn.aiagent.domain.chat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 会话实体
 * 封装对话会话的状态和历史消息
 */
public class Conversation {
    
    private final ConversationId id;
    private final List<Message> messages;
    private final Instant createdAt;
    private Instant updatedAt;
    
    /**
     * Intent: 创建新会话
     * Input: id (会话ID)
     * Output: Conversation实例
     * SideEffects: 无
     * Failure: 如果id为null，抛出IllegalArgumentException
     * Idempotency: 幂等
     */
    public Conversation(ConversationId id) {
        if (id == null) {
            throw new IllegalArgumentException("ConversationId cannot be null");
        }
        this.id = id;
        this.messages = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }
    
    /**
     * Intent: 从现有数据重建会话
     * Input: id, messages, createdAt, updatedAt
     * Output: Conversation实例
     * SideEffects: 无
     * Failure: 如果id为null，抛出IllegalArgumentException
     * Idempotency: 幂等
     */
    public Conversation(ConversationId id, List<Message> messages, Instant createdAt, Instant updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("ConversationId cannot be null");
        }
        this.id = id;
        this.messages = new ArrayList<>(messages != null ? messages : Collections.emptyList());
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }
    
    /**
     * Intent: 添加用户消息到会话
     * Input: message (用户消息)
     * Output: 无
     * SideEffects: 更新messages列表和updatedAt时间戳
     * Failure: 如果message为null，抛出IllegalArgumentException
     * Idempotency: 非幂等（每次调用都会添加新消息）
     */
    public void addUserMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        this.messages.add(message);
        this.updatedAt = Instant.now();
    }
    
    /**
     * Intent: 添加AI回复到会话
     * Input: message (AI回复消息)
     * Output: 无
     * SideEffects: 更新messages列表和updatedAt时间戳
     * Failure: 如果message为null，抛出IllegalArgumentException
     * Idempotency: 非幂等（每次调用都会添加新消息）
     */
    public void addAiMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        this.messages.add(message);
        this.updatedAt = Instant.now();
    }
    
    public ConversationId getId() {
        return id;
    }
    
    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public int getMessageCount() {
        return messages.size();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversation that = (Conversation) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Conversation{id=" + id + ", messageCount=" + messages.size() + 
               ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "}";
    }
}

