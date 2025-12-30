

# TimeoutSemanticClassifier – MUST Contract

> 本文件从 `spec.md` 中抽取 **必须被测试验证的契约条款（MUST）**。
> 这些条款用于生成单元测试，作为该组件行为正确性的最低保证。

---

## MUST-1：对 null 输入的稳定性

**描述**：
- 当输入的 `Throwable` 为 `null` 时，分类器必须返回 `false`。

**原因**：
- 该组件是纯逻辑函数，不允许因空输入抛出异常或产生不确定行为。

**可测试性**：
- 输入：`null`
- 期望输出：`false`

---

## MUST-2：必须遍历完整 cause chain

**描述**：
- 当超时异常被多层包装时，只要在任意一层 `cause` 中存在超时语义，分类器必须返回 `true`。

**原因**：
- 外部 SDK / 框架常对异常进行多层包装，若只判断顶层异常将导致语义丢失。

**可测试性**：
- 输入：`RuntimeException(new RuntimeException(new TimeoutException()))`
- 期望输出：`true`

---

## MUST-3：识别 Java 标准库超时异常

**描述**：
- 当异常或其 cause 链中包含以下任一异常类型时，必须被识别为超时语义：
  - `java.util.concurrent.TimeoutException`
  - `java.net.SocketTimeoutException`

**原因**：
- 这是 JVM 生态中最通用、最稳定的超时异常表示形式。

**可测试性**：
- 输入：`new TimeoutException()`
- 输入：`new SocketTimeoutException()`
- 期望输出：`true`

---

## MUST-4：识别 Netty / Reactor-Netty 常见超时异常（通过类名）

**描述**：
- 当异常或其 cause 链中出现以下任一异常类名时，必须被识别为超时语义：
  - `io.netty.handler.timeout.ReadTimeoutException`
  - `io.netty.handler.timeout.WriteTimeoutException`
  - `io.netty.channel.ConnectTimeoutException`
  - `reactor.netty.http.client.PrematureCloseException`

**原因**：
- 该组件需避免对 Netty / Reactor-Netty 产生编译期依赖，因此通过 FQCN 匹配进行识别。

**可测试性**：
- 输入：自定义异常类，`getClass().getName()` 返回上述 FQCN
- 期望输出：`true`

---

## MUST-5：非超时异常不得被误判

**描述**：
- 对于不具备明确超时语义的异常，分类器必须返回 `false`。

**原因**：
- 误判会导致错误的错误码映射、错误的重试或降级策略。

**可测试性**：
- 输入：`IllegalArgumentException`
- 输入：`NullPointerException`
- 期望输出：`false`

---

## MUST-6：实现必须为纯函数

**描述**：
- 对同一输入，分类器必须返回一致结果。
- 不得修改异常对象。
- 不得抛出任何异常。

**原因**：
- 该组件将被广泛复用，其行为必须可预测、可推理。

**可测试性**：
- 同一异常对象多次调用，结果一致
- 调用过程中无异常抛出

---

## MUST-7：不确定情况必须返回 false

**描述**：
- 当异常是否为超时语义无法确定时，分类器必须返回 `false`。

**原因**：
- 该组件用于错误分类，错误的“猜测式 true”风险高于漏判。

**可测试性**：
- 输入：自定义 RuntimeException（无 cause）
- 期望输出：`false`

---

## Scope Clarification

- 上述 MUST 条款仅约束 **超时语义识别行为**。
- 不约束：
  - 超时阈值
  - 超时发生的具体时机
  - 是否需要重试或降级

---

> 若以上任一 MUST 条款被破坏，应视为 **TimeoutSemanticClassifier 行为回归**。