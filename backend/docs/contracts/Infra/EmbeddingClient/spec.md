# EmbeddingClient Spec

## Intent

提供“文本向量化（embedding）”能力：将输入文本提交给外部 Embedding 服务并返回向量结果。
该组件的核心承诺是：**在超时与异常语义上对上层可预期**，以便上层稳定做错误码映射与治理。

---

## Scope

- 覆盖两类调用场景：
  - **SLA 检索**（面向在线查询，超时上界更严格）
  - **Reindex**（面向批处理/重建索引，超时上界可更宽松）
- 该组件只负责“发起 embedding 请求并返回向量/失败”，不负责检索、排序与业务语义。

---

## Inputs

- `text`：
  - MUST：非 `null`
  - MUST：去除首尾空白后非空
  - SHOULD：长度不得超过系统配置的上限（上限由配置决定）

调用场景差异（例如 SLA / Reindex）通过不同的 port 暴露与装配体现，而非由调用方通过参数显式传入。

---

## Outputs

- 成功：返回 `List<Double>` 向量。
  - MUST：向量维度与该 embedding 服务的声明维度一致。
- 失败：抛出异常（见 Failure Semantics / Timeout Semantics）。

---

## Timeout & Cancellation Semantics

- MUST：客户端必须具备**本地超时兜底机制**，不得在外部依赖调用上无限阻塞。
- SHOULD：在常见的网络异常行为下（包括但不限于以下情形），客户端应在本地超时上界内失败：
  - **header_delay**：迟迟不返回响应开始（首字节/headers）
  - **body_delay**：响应已开始但读取响应体卡住/极慢
  - **no_response**：连接建立后长期无任何响应数据
- MUST：当发生超时失败时，异常必须具备“超时语义”，可被 `TimeoutSemanticClassifier.isTimeout(Throwable)` 识别为 `true`。
- SHOULD：连接建立阶段应受 connect-timeout 约束，避免连接阶段长时间挂起。
- NON-GOAL：取消（cancellation）不保证能中断远端请求；远端是否已产生副作用不可由本组件保证。

---

## Failure Semantics

- MUST：以下情况必须失败（抛异常）：
  - embedding 服务返回空结果
  - 返回向量维度不符合声明维度
- MUST：超时失败必须满足“超时语义可识别”（见 Timeout & Cancellation Semantics）。
- UNKNOWN：第三方 SDK/服务返回的非超时错误在异常类型、错误码、消息文本上的稳定性（可能随版本变化）。

---

## Idempotency & Side Effects

- 该组件会触发外部 embedding 调用（可能产生费用）。
- 调用语义：**非幂等**（重复调用可能产生重复计费/请求）。
- UNKNOWN：远端是否具备幂等保证、以及是否会对重复请求做去重。

---

## Observability

- SHOULD：支持传入/透传调用链路关联标识（例如 requestId / traceId），用于日志与链路追踪。
- SHOULD：记录关键维度（场景、耗时、成功/失败类型），用于告警与容量规划。

---

## Non-Goals

- 不承诺任何自动重试策略（是否重试由上层策略决定）。
- 不承诺 embedding 服务响应结构在“已使用字段”之外的稳定性。
- 不承诺对网络边界行为进行穷举验证（相关证据与 Known Gaps 见 mechanism/tests 文档）。

---

## Known Gaps

- 未穷举验证所有网络异常形态（例如半开连接、分片后断流等）；如出现线上异常，需结合机制测试补证据。
- 对 header_delay/body_delay/no_response 三类场景的“计时边界”在不同底层实现中可能存在差异；本 spec 仅要求“本地可超时失败 + 超时语义可识别”。
