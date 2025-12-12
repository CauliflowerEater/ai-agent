# AI Agent 项目日志配置说明

## 📋 日志框架

本项目使用 **Logback + SLF4J** 作为日志解决方案。

### 版本信息

- **SLF4J**: 2.0.17 (JDK 21 完全兼容)
- **Logback**: 1.5.18 (最新稳定版)
- **Spring Boot**: 3.4.4 (自动集成)

### 为什么选择 Logback？

✅ **JDK 21 完全兼容** - 最新版本对 JDK 21 原生支持  
✅ **Spring Boot 默认集成** - 零配置即可使用  
✅ **高性能** - 异步日志、零垃圾回收模式  
✅ **功能强大** - MDC、条件配置、动态重载  
✅ **AI Agent 友好** - 非常适合追踪流式响应和异步调用  

## 📁 配置文件

### 主配置文件

- **logback-spring.xml** - Logback 核心配置（支持 Spring Profile）
- **application.properties** - 基础日志参数配置

### 日志文件位置

```
./logs/
├── ai-agent.log              # 主日志文件
├── ai-agent-chat.log         # AI 聊天专用日志
├── ai-agent.log.2025-12-12.0.gz  # 归档日志
└── ...
```

## 🎯 日志级别说明

| 级别 | 用途 | 示例场景 |
|------|------|---------|
| **TRACE** | 最详细的跟踪信息 | 调试算法细节 |
| **DEBUG** | 调试信息 | 开发环境，查看变量值 |
| **INFO** | 重要业务流程 | 用户请求、AI 响应 |
| **WARN** | 警告信息 | 重试、降级处理 |
| **ERROR** | 错误信息 | 异常、失败情况 |

## 🔧 环境配置

### 开发环境 (dev/default)
- 控制台输出：彩色格式
- 文件输出：完整日志
- 级别：DEBUG

### 测试环境 (test)
- 控制台输出：简化格式
- 文件输出：INFO 级别
- 级别：INFO

### 生产环境 (prod)
- 控制台输出：JSON 格式
- 文件输出：异步写入（高性能）
- 级别：INFO
- 自动归档：每天 + 100MB

## 💡 使用示例

### 1. 基本日志记录

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    
    public void doChat(String message) {
        log.info("接收到用户消息: {}", message);
        log.debug("消息长度: {}", message.length());
    }
}
```

### 2. 异常日志

```java
try {
    // 业务逻辑
} catch (Exception e) {
    // ✅ 推荐：记录完整异常堆栈
    log.error("处理聊天请求失败", e);
}
```

### 3. MDC 追踪（流式响应）

```java
import org.slf4j.MDC;

public void handleStreamResponse(String chatId) {
    try {
        MDC.put("chatId", chatId);
        MDC.put("requestType", "stream");
        
        log.info("开始流式响应");
        // 后续所有日志都会包含 chatId 和 requestType
        
    } finally {
        MDC.clear(); // 必须清理
    }
}
```

### 4. 性能监控

```java
long start = System.currentTimeMillis();
try {
    // 业务逻辑
} finally {
    long duration = System.currentTimeMillis() - start;
    if (duration > 1000) {
        log.warn("AI 响应耗时过长: {} ms", duration);
    }
}
```

## 🎨 日志格式

### 控制台输出（彩色）
```
2025-12-12 10:30:45.123  INFO 12345 --- [nio-8080-exec-1] c.s.a.service.ChatService : 接收到用户消息
```

### 文件输出
```
2025-12-12 10:30:45.123 [nio-8080-exec-1] INFO  com.shawn.aiagent.service.ChatService - 接收到用户消息
```

## 📊 特殊日志配置

### AI 聊天日志独立记录

AI 相关的日志会同时输出到：
- `ai-agent-chat.log` - 专用日志文件
- 控制台 - 实时查看

涉及的包：
- `com.shawn.aiagent.app.*`
- `com.shawn.aiagent.service.*`

### 第三方库日志控制

```xml
<!-- Spring AI 框架 -->
<logger name="org.springframework.ai" level="INFO"/>

<!-- Netty (WebFlux 底层) -->
<logger name="io.netty" level="WARN"/>
<logger name="reactor.netty" level="INFO"/>

<!-- 阿里云 SDK -->
<logger name="com.alibaba" level="WARN"/>
```

## 🚀 最佳实践

### ✅ DO（推荐）

1. **使用参数化日志**
   ```java
   log.info("用户 {} 发送消息: {}", userId, message);
   ```

2. **记录完整异常**
   ```java
   log.error("操作失败", exception);
   ```

3. **使用 MDC 追踪请求**
   ```java
   MDC.put("chatId", chatId);
   ```

4. **条件日志避免性能损耗**
   ```java
   if (log.isDebugEnabled()) {
       log.debug("复杂对象: {}", obj.toString());
   }
   ```

### ❌ DON'T（避免）

1. **不要使用字符串拼接**
   ```java
   // ❌ 错误
   log.info("用户 " + userId + " 发送消息");
   ```

2. **不要只记录异常消息**
   ```java
   // ❌ 错误：丢失堆栈信息
   log.error("错误: " + e.getMessage());
   ```

3. **不要忘记清理 MDC**
   ```java
   // ❌ 可能导致内存泄漏
   MDC.put("key", "value");
   // 忘记 MDC.clear()
   ```

## 🔍 日志查看

### 实时查看
```bash
# 查看主日志
tail -f ./logs/ai-agent.log

# 查看 AI 聊天日志
tail -f ./logs/ai-agent-chat.log

# 过滤错误日志
tail -f ./logs/ai-agent.log | grep ERROR
```

### 搜索日志
```bash
# 按时间搜索
grep "2025-12-12 10:30" ./logs/ai-agent.log

# 按 chatId 搜索
grep "chatId=123" ./logs/ai-agent-chat.log

# 按异常类型搜索
grep "DataIntegrityException" ./logs/ai-agent.log
```

## 📈 性能优化

### 异步日志（生产环境）

生产环境自动启用异步日志，特点：
- 非阻塞写入
- 队列缓冲 512 条
- 不丢弃任何日志（discardingThreshold=0）

### 日志归档策略

- 单文件最大：100MB
- 保留天数：30 天
- 总容量上限：3GB
- 压缩格式：gzip

## 🛠️ 自定义配置

### 修改日志级别（不重启）

编辑 `application.properties`:
```properties
# 调整特定包的日志级别
logging.level.com.shawn.aiagent.rag=TRACE
```

### 添加新的 Appender

编辑 `logback-spring.xml`，参考已有的 `AI_LOG` appender。

## 📚 参考文档

- [Logback 官方文档](https://logback.qos.ch/manual/)
- [SLF4J 官方文档](https://www.slf4j.org/manual.html)
- [Spring Boot 日志](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)

## 🆘 常见问题

### Q: 日志文件在哪里？
A: 默认在 `./logs/` 目录下，可通过 `logging.file.path` 配置修改。

### Q: 如何增加 AI API 调用日志？
A: 在 `logback-spring.xml` 中将 `org.springframework.ai` 级别改为 `DEBUG`。

### Q: 如何在生产环境关闭控制台日志？
A: 在 `prod` profile 中移除 `CONSOLE` appender 引用。

### Q: MDC 上下文在异步场景会丢失吗？
A: WebFlux 会自动传递 Reactor Context，但需要使用 `Mono.deferContextual()` 配合。
