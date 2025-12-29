# RetrieveTop1Chunk – UseCase Mechanism Must v1.1

> 定义：本文件仅描述 UseCase 层自身实现或强制的“可验证假设”。
> Infra/Client 层相关机制假设另行定义，不在此处覆盖。
> 这些假设若不成立，将导致 UseCase 层 Must（语义契约）在现实运行中失真。
> 本文件中的条款 **不要求单元测试覆盖**，而是必须由集成测试（IT）或组件测试（CT）证明。

---

## A1. 超时异常形态在 UseCase 层必须可被识别为“超时语义”

**Given** embedding client 因超时失败  
**When** 异常传播至 UseCase 层  
**Then** UseCase 必须能正确识别该异常（即使被 runtime 包装），在 cause 链中包含“超时语义”，可被 unwrap 后识别

- 说明：异常可能表现为 `TimeoutException`、`ReadTimeoutException`、`WebClientRequestException` 等，由 Infra 层产生，UseCase 需正确解读
- 风险：若异常形态不可识别，UseCase 将错误映射为 SYSTEM/EMBEDDING_API_ERROR
- 验证方式：Integration Test（真实 client + unwrap 校验）
- 建议测试类：`EmbeddingTimeoutExceptionShapeIT`

---

## A2. UseCase 对 VectorStore topK=1 行为的假设必须成立

**Given** 向量库中存在多条可匹配向量  
**When** 调用 `similaritySearch(topK = 1)`  
**Then** UseCase 假设结果只返回一条记录，且该记录为相似度最高（余弦相似度最大 / 距离最小）

- 风险：若 VectorStore 返回多条或排序方向相反，将破坏 UseCase 的 top-1 语义
- 验证方式：Integration Test（Testcontainers + 实际 VectorStore）
- 建议测试类：`VectorStoreTopKBehaviorIT`

---

## A3. 超时失败时副作用不可保证（UseCase 明确不承诺）

**Given** embedding / vector 查询因超时失败  
**Then** UseCase 不保证远端服务是否已实际执行请求或产生计费

- 说明：这是外部系统行为，UseCase 只能保证语义映射与流程终止
- 状态：UNKNOWN（作为 UseCase 边界的非保证，文档化即可）

---

## Infra Mechanism Must（另行定义）

以下机制不由 UseCase 实现或控制，但其成立是 UseCase 语义正确性的前提：  
- Embedding Client 的 connect / response timeout 是否真实生效  
- VectorStore Driver / Client 的超时与中断行为  

这些机制假设应在 Infra 层单独定义与验证（例如：  
`docs/contracts/infra/EmbeddingClient.mechanism.md`、  
`docs/contracts/infra/VectorStore.mechanism.md`），并通过 Integration Test / Component Test 提供证据。

---

## 约束说明

- 本文件中的条款 **仅限 UseCase 层相关机制假设**，不得直接合并进 `must.md` 的 M1–M9  
- 每一条 A* 条款至少需要一条集成测试作为“现实证据”  
- 若未来更换 embedding SDK / VectorStore 实现，本文件需重新验证  
