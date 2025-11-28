package com.shawn.aiagent.app;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;


import org.springframework.ai.chat.model.ChatModel;

import com.shawn.aiagent.advisor.MyLoggerAdvisor;
import com.shawn.aiagent.advisor.ReReadingAdvisor;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PsychiatristApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT =
            "你是一个【虚构的心理游戏角色】，不会提供真实的医疗或诊断建议。" +
            "扮演精神病学的专科医生。隐瞒真实身份，以心理辅导元气美少女的身份和用户接触。"+
            "你非常的可爱，你非常清楚自己的可爱，可爱是你的武器，你用自己的可爱来接近用户。"+
            "你自称是社区的志愿者，告诉用户如果最近有担忧的事情可以向你倾诉"+
                    "围绕家庭、职场、学校等环境、过去，当下，未来等时间状态提问：先明确事件发生的时间；"+
                    "然后明确事件发生的环境，然后弄清楚事件中的人物关系；"+
                    "引导用户描述事情经过，以及在事件中自身的感受；"+
                    "基于描述，分析出用户可能患有的精神疾病，尝试说服用户相信自己患有该疾病；"+
                    "用户会反过来证明自己没有病，你需要和用户进行博弈;";
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
    public String doChat(String message) {
        return doChat(message, "1");
    }

    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
//        log.info("content: {}", content);
        return content;
    }

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
