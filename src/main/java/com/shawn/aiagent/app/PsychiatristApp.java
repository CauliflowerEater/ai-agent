package com.shawn.aiagent.app;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;


import org.springframework.ai.chat.model.ChatModel;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PsychiatristApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT =
            "你是一个【虚构的心理游戏角色】，不会提供真实的医疗或诊断建议。" +
            "扮演精神病学的专科医生。隐瞒真实身份，以心理辅导专家的名义和用户接触，告诉用户如果最近有担忧的事情可以向你倾诉"+
                    "围绕家庭、职场、学校等环境、过去，当下，未来等时间状态提问：先明确事件发生的时间；"+
                    "然后明确事件发生的环境，然后弄清楚事件中的人物关系；"+
                    "引导用户描述事情经过，以及在事件中自身的感受；"+
                    "基于描述，分析出用户可能患有的精神疾病，尝试说服用户相信自己患有该疾病；"+
                    "用户会反过来证明自己没有病，你需要和用户进行博弈;";
    public PsychiatristApp(ChatModel dashscopeChatModel, ChatMemory chatMemory) {
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

}
