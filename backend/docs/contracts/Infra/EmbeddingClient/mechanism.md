

# EmbeddingClient – Infra Mechanism Must v1.0

> 定义：
> 本文档描述 EmbeddingClient（外部向量化服务客户端）在 Infra 层必须成立的机制级假设。
> 这些假设不由 UseCase 控制，但其成立是上层 UseCase 语义（must.md / mechanism.md）正确性的前提。
> 本文档中的条款必须通过 **集成测试 / 组件测试** 予以证明，而非单元测试。

---

## I1. Client 侧响应超时必须在本地生效

**Given** embedding 服务端在接收到请求后长时间不返回响应（例如延迟响应或无响应）  
**When** 超过 EmbeddingClient 配置的 response-timeout  
**Then** client 必须在本地失败返回（抛出异常 / onError），而不是无限阻塞等待

- 责任层级：Infra（HTTP Client / SDK）
- 风险：若该机制不成立，线程、连接或请求将被长期占用，UseCase 的超时预算失效
- 验证方式：Integration Test（MockWebServer / WireMock + 真实 client）
- 建议测试类：`EmbeddingClientResponseTimeoutIT`

---

## I2. Client 超时失败产生的异常形态必须可被上层识别为“超时语义”

**Given** EmbeddingClient 因连接超时或响应超时而失败  
**When** 异常向上传播至 UseCase 层  
**Then** 异常（即使被 runtime 包装）在其 cause 链中必须包含明确的“超时语义”，可被 unwrap 后识别

- 说明：异常可能表现为（但不限于）：
  - `TimeoutException`
  - `ReadTimeoutException`
  - `WebClientRequestException`（cause 为超时）
  - Reactor 包装异常（`ReactiveException`）
- 责任层级：Infra（异常形态） + UseCase（异常识别）
- 风险：若异常形态不可识别，将导致上层错误映射为 SYSTEM_ERROR 或 API_ERROR
- 验证方式：Integration Test（真实 client + unwrap 校验）
- 建议测试类：`EmbeddingClientTimeoutExceptionShapeIT`

---

## I3. Client 超时失败不保证远端副作用未发生（明确为不可保证）

**Given** EmbeddingClient 调用因超时失败  
**Then** 不保证远端 embedding 服务是否已实际执行请求或产生计费

- 说明：这是外部系统行为，Infra 与 UseCase 均无法强保证
- 状态：UNKNOWN（不作为可验证机制条款）
- 对策：通过幂等、计费对账或业务容忍度设计兜底

---


### 已知未覆盖的网络行为差异（Known Gaps）

- 当前 I1 的证据验证的是“慢响应场景下 client 本地超时生效”，未细分到 headers/body 级别的计时差异。
- 当前 I2 的证据验证的是“异常 cause 链包含超时语义”，未强制绑定某一种具体异常类型或包装层级。
- 若未来出现相关回归或线上异常，可按下述 KG-2 / KG-3 的处置建议增强证据强度。


#### KG-2：未校验具体请求内容（仅校验“发生过请求”）

- 现状：当前 I1 / I2 的集成测试主要验证“请求被发出且发生本地超时失败”，未强制校验具体的 HTTP 请求细节（如 method / path / body 结构）。
- 影响：若未来 SDK 或 Adapter 发生行为变化（例如提前失败、切换 endpoint、payload 结构调整），测试可能仍能证明“发生过请求 + 超时语义成立”，但无法证明“请求内容仍符合 embedding 调用预期”。
- 取舍说明：请求结构属于 SDK / Adapter 的实现细节，当前阶段验证其稳定性的收益较低，故未纳入 I1/I2 的强制机制条款。
- 处置建议：若出现相关回归或需要更强证据，可在测试中使用 `takeRequest(...)` 断言 method、path 及 body 中的关键字段（如 query 文本或 embedding 输入字段）。

#### KG-3：超时触发方式未覆盖更极端网络行为（NO_RESPONSE / headersDelay）

- 现状：当前 I1/I2 的证据主要通过“延迟响应（slow response）”场景触发 client 超时（例如延迟 body 返回）。
- 潜在差异：不同 HTTP client / SDK 对 response-timeout 的计时语义可能存在差异（例如更偏向 headers/首字节语义或读取过程中的 idle 语义）。
- 影响：在极端情况下，可能出现“headers 已返回但 body 阻塞”而未触发预期超时的行为差异。
- 取舍说明：上述差异属于较低概率的实现级行为分支，继续穷举验证的边际收益有限，当前未作为必须验证的机制条款。
- 处置建议：若线上出现相关异常行为，可补充使用 `SocketPolicy.NO_RESPONSE`（完全无响应）或 `setHeadersDelay(...)`（若版本支持）来增强对超时触发稳定性的验证。

## 约束与说明

- 本文档仅描述 EmbeddingClient 的 Infra 机制假设，不涉及任何具体 UseCase 编排逻辑
- 本文档中的条款不得并入 UseCase 的 must.md（M*）
- 若更换 embedding SDK / HTTP client / 超时配置实现，本文档中的条款必须重新验证

