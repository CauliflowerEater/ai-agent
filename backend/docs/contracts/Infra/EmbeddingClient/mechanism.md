

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

## 约束与说明

- 本文档仅描述 EmbeddingClient 的 Infra 机制假设，不涉及任何具体 UseCase 编排逻辑
- 本文档中的条款不得并入 UseCase 的 must.md（M*）
- 若更换 embedding SDK / HTTP client / 超时配置实现，本文档中的条款必须重新验证
