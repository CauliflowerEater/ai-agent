

# VectorStore – Tests Coverage Matrix

> 目的：将 `mechanism.md` 中的 Infra 机制条款（V*）映射到可执行的测试证据（IT/CT）。
> 约定：
> - IT = Integration Test（真实 VectorStore/Driver，优先使用 Testcontainers）
> - CT = Component Test（更轻量，可能只验证适配器边界或异常形态）
> - 本文件不描述测试实现细节，仅记录“条款 → 证据”的对应关系。

---

## 覆盖矩阵

| Mechanism ID | Mechanism 摘要 | Evidence Type | Suggested Test Class | Status |
|---|---|---|---|---|
| V1 | similaritySearch(topK=N) 必须遵守 topK 上限（size <= N） | IT | `VectorStoreTopKLimitIT` | TODO |
| V2 | similaritySearch 结果必须按“相似度最高优先”（cosine similarity 最大 / distance 最小）排序 | IT | `VectorStoreSimilarityOrderIT` | TODO |
| V3 | 无匹配结果必须返回空列表（非 null / 非异常 / 非占位记录） | IT | `VectorStoreEmptyResultIT` | TODO |
| V4 | 查询超时必须在本地失败返回（不允许无限阻塞） | IT | `VectorStoreQueryTimeoutIT` | TODO |
| V5 | 查询超时失败不保证远端副作用未发生（UNKNOWN，仅文档化） | N/A | N/A | Documented |

---

## 证据要求（最小标准）

- V1：测试至少覆盖 N=1 与 N>1 两种 topK，且断言结果数量 `<= N`。
- V2：测试必须使用一组“相似度可预期”的已知向量数据，断言排序方向正确（避免只测 size 不测 order）。
- V3：测试必须断言返回值为 **空列表**（`isEmpty()`），并显式断言不为 `null`。
- V4：测试必须证明超时是由 **driver/client 本地**触发（例如在 timeout + ε 内失败），而不是靠上层 `Mono.timeout()` 触发。
