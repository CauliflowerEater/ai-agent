

# EmbeddingClient – Tests Coverage Matrix

> 目的：将 `mechanism.md` 中的 Infra 机制条款（I*）映射到可执行的测试证据（IT/CT）。
> 约定：
> - IT = Integration Test（真实 client + MockWebServer/WireMock 或真实依赖）
> - CT = Component Test（更轻量，可能只验证异常形态/适配器边界）
> - 本文件不描述测试实现细节，仅记录“条款 → 证据”的对应关系。

---

## 覆盖矩阵

| Mechanism ID | Mechanism 摘要 | Evidence Type | Suggested Test Class | Status |
|---|---|---|---|---|
| I1 | response-timeout 必须在本地生效（不允许无限阻塞） | IT | `EmbeddingClientResponseTimeoutIT` | TODO |
| I2 | 超时失败的异常形态必须保留“超时语义”（可被 unwrap/cause-chain 识别） | IT | `EmbeddingClientTimeoutExceptionShapeIT` | TODO |
| I3 | 超时失败不保证远端副作用未发生（UNKNOWN，仅文档化） | N/A | N/A | Documented |

---

## 证据要求（最小标准）

- I1：测试必须证明超时是由 **client 本地**触发（例如在 response-timeout + ε 内失败），而不是靠上层 `Mono.timeout()` 触发。
- I2：测试必须记录并断言异常的 cause 链包含明确的“超时语义”（例如 `TimeoutException` / `ReadTimeoutException` 等），且在 runtime 包装下仍可识别。
