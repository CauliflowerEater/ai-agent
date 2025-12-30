

# TimeoutSemanticClassifier – Specification

## 1. Intent

识别异常 `Throwable` 是否表达了 **“超时（timeout）语义”**，
用于在上层（UseCase / Adapter）中进行**稳定的错误分类与映射**，
避免依赖具体 SDK、HTTP client 或异常实现细节。

---

## 2. Scope

- 适用于所有 **外部依赖调用**（HTTP / SDK / DB / MQ 等）产生的异常
- 主要用于：
  - 错误码映射（如 `*_TIMEOUT`）
  - 重试 / 熔断 / 降级判断的前置语义识别

**不负责：**
- 抛出或包装异常
- 决定超时阈值或超时策略
- 判断远端是否已产生副作用

---

## 3. Input Contract

```java
Throwable error
```

### Input Constraints

- `error` 可以为任意 `Throwable`
- 允许被多层包装（需通过 `getCause()` 链展开）
- `error` 允许为 `null`

---

## 4. Output Contract

```java
boolean
```

### Output Semantics

- `true`：
  - 表示该异常 **在语义上** 表达了“因超时导致的失败”
- `false`：
  - 表示该异常 **不具备明确的超时语义**，或无法确定为超时

---

## 5. Semantic Rules

### R1. 语义识别而非具体异常类型绑定

- 不要求异常类型唯一
- 不要求异常类来自特定 SDK / client
- 不依赖异常的抛出位置或包装层级

判断标准为：
> 异常或其 cause 链中，是否存在 **可合理解释为“超时”的失败信号**。

---

### R2. 必须遍历完整 cause chain

- 实现 **必须遍历 `Throwable#getCause()` 链**
- 允许异常被 SDK / Reactor / 框架多层包装

---

### R3. 纯函数约束

- 该组件必须是 **纯逻辑函数**
- 对任意输入（包括 `null`）：
  - 不得抛出异常
  - 必须返回确定的 boolean 值

---

## 6. Stability Guarantees

- 方法签名应保持稳定
- 内部识别规则允许随运行环境、依赖版本演进而扩展
- 上层调用方 **仅依赖输出语义，不依赖具体识别规则**

---

## 7. Non-Goals

该组件**不保证**：

- 识别所有可能形式的超时异常
- 区分不同类型的超时（connect / read / write）
- 判断是否应当重试或降级

---

## 8. Failure Semantics

- 当异常是否为超时 **无法确定** 时：
  - 必须返回 `false`
  - 不得进行猜测式判断

---

## 9. Examples

| Input Exception | Result |
|----------------|--------|
| `TimeoutException` | `true` |
| `RuntimeException(cause = SocketTimeoutException)` | `true` |
| `WebClientRequestException(cause = ReadTimeoutException)` | `true` |
| `IllegalArgumentException` | `false` |
| `null` | `false` |

---

## 10. Usage Constraint

- 上层代码 **必须仅通过该组件的返回值进行超时语义判断**
- 不得在 UseCase / Adapter 中重复实现异常类型判断逻辑
