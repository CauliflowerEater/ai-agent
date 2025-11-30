package com.shawn.aiagent.app;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;


import org.springframework.ai.chat.model.ChatModel;

import com.shawn.aiagent.advisor.MyLoggerAdvisor;
import com.shawn.aiagent.advisor.ReReadingAdvisor;
import com.shawn.aiagent.constants.CharacterCards;
import com.shawn.aiagent.constants.ResponseFormat;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PsychiatristApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = CharacterCards.CUTY_CHARACTER_CARD + ResponseFormat.HUMAN_LIKE_RESPONSE_NEW_LINE;
            

    public PsychiatristApp(ChatModel dashscopeChatModel, ChatMemory chatMemory) {
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor(),
                        new ReReadingAdvisor()
                )
                .build();
    }

    // 方法重载：使用默认chatId
    public Flux<String> doChatStream(String message) {
        return doChatStream(message, "1");
    }
    
    public Flux<String> doChatStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    //todo
    //更新带报告的对话为流式响应
    public record MentalHealthReport(String title, List<String> suggestions) {
    }
    public MentalHealthReport doChatWithReport(String message, String chatId) {
        MentalHealthReport mentalHealthReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成心理问题状况评估，标题为{用户名}的评估报告，内容为各种常见心理问题的可能性")
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .call()
                .entity(MentalHealthReport.class);
        log.info("MentalHealthReport: {}", mentalHealthReport);
        return mentalHealthReport;
    }



}
