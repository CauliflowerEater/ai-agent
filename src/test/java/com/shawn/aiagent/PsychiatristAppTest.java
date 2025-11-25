package com.shawn.aiagent;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.shawn.aiagent.app.PsychiatristApp;

import jakarta.annotation.Resource;

@SpringBootTest
class PsychiatristAppTest {

    @Resource
    private PsychiatristApp psychiatristApp;

    @Test
    void testChat() {
        String chatId = UUID
                .randomUUID().toString();
        // 第一轮
        String message = "你好，我是智障，我没病";
        String answer = psychiatristApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我觉得你有病";
        answer = psychiatristApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我是什么来着？刚跟你说过，帮我回忆一下";
        answer = psychiatristApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }
}
