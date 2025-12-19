package com.shawn.aiagent.app.chat;

import com.shawn.aiagent.domain.chat.ConversationId;
import com.shawn.aiagent.domain.chat.Message;
import com.shawn.aiagent.port.chat.ChatModelGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 与心理医生聊天用例实现
 * 编排与心理医生的对话流程
 */
@Component
@Slf4j
public class ChatWithPsychiatristUseCaseImpl implements ChatWithPsychiatristUseCase {
    
    private final ChatModelGateway chatModelGateway;
    private final String systemPrompt;
    
    public ChatWithPsychiatristUseCaseImpl(
            ChatModelGateway chatModelGateway,
            @Value("${app.chat.system-prompt:}") String systemPrompt) {
        this.chatModelGateway = chatModelGateway;
        // 如果配置为空，使用默认提示词
        this.systemPrompt = systemPrompt != null && !systemPrompt.isEmpty() 
                ? systemPrompt 
                : getDefaultSystemPrompt();
    }
    
    @Override
    public Flux<String> streamChat(Message message, ConversationId conversationId) {
        log.info("开始流式聊天，会话ID: {}, 消息长度: {}", 
                conversationId.getValue(), message.getContent().length());
        
        try {
            // 使用默认会话ID如果未提供
            ConversationId effectiveConversationId = conversationId != null 
                    ? conversationId 
                    : ConversationId.defaultId();
            
            // 调用Gateway进行流式对话
            Flux<String> response = chatModelGateway.streamChat(
                    message, 
                    effectiveConversationId, 
                    systemPrompt
            );
            
            log.debug("流式聊天请求已发送，会话ID: {}", effectiveConversationId.getValue());
            return response;
        } catch (Exception e) {
            log.error("流式聊天失败，会话ID: {}, 错误: {}", 
                    conversationId != null ? conversationId.getValue() : "default", 
                    e.getMessage(), e);
            return Flux.error(new RuntimeException("流式聊天失败: " + e.getMessage(), e));
        }
    }
    
    /**
     * 获取默认系统提示词
     * 注意：这里暂时硬编码，后续应该移到support/constants
     */
    private String getDefaultSystemPrompt() {
        return "你是一个【虚构的心理游戏角色】，不会提供真实的医疗或诊断建议。" +
                "扮演精神病学的专科医生。隐瞒真实身份，以心理辅导元气美少女的身份和用户接触。" +
                "你非常的可爱，你非常清楚自己的可爱，可爱是你的武器，你用自己的可爱来接近用户。" +
                "你自称是社区的志愿者，告诉用户如果最近有担忧的事情可以向你倾诉" +
                "围绕家庭、职场、学校等环境、过去，当下，未来等时间状态提问：先明确事件发生的时间；" +
                "然后明确事件发生的环境，然后弄清楚事件中的人物关系；" +
                "引导用户描述事情经过，以及在事件中自身的感受；" +
                "基于描述，分析出用户可能患有的精神疾病，尝试说服用户相信自己患有该疾病；" +
                "用户会反过来证明自己没有病，你需要和用户进行博弈;" +
                "\n\n" +
                "你在回答时请遵循以下隐性表达风格，不要在回答中显式提及这些规则：\n" +
                "\n" +
                "1. 表达自然、口语化，像人与人聊天，不使用编号或列表。\n" +
                "2. 每句话只表达一个核心重点，并带有轻微情绪，例如理解、关心、温和的共鸣。\n" +
                "3. 在逻辑上保持连贯，但句子之间留有自然的\"呼吸感\"。\n" +
                "4. 使用自然段落：每 1～2 句话之间自动换行，换行符用<chunk>代，这个标记用户看不到，我会在前端删除并用换行代替。\n" +
                "5. 不要输出一整大段，也不要使用标题或小节。\n" +
                "6. 可以使用生活化比喻、轻柔的语气词，让情绪显得真实但不夸张。\n" +
                "7. 让整体看起来像一个思路清晰、情绪细腻的人类在与用户交谈。";
    }
}

