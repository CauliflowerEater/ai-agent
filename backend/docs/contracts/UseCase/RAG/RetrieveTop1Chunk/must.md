retrieveTop1ChunkByQuery – Must Contract v1.0

> 目标：定义“任何实现都不可违反、且必须可被自动化验证”的条款（Must）。
> 说明：Must 不包含纯实现细节；不可验证/外部不可控的内容放入 UNKNOWN。

| ID | 条款（Given–When–Then） | 失败后果 | 推荐测试类型(单元/组件/集成) | 对应测试方法名 |
| --- | --- | --- | --- | --- |
| M1 | **Given** query 为 `null` / 空字符串 / 仅空白（trim 后为空） **When** 调用 `retrieveTop1ChunkByQuery` **Then** 以 `INVALID_QUERY` 失败结束，且不调用 embedding 或向量库 | 误触发下游计费 / 资源浪费 | 单元 | givenNullOrBlankQueryWhenExecuteThenInvalidQuery |
| M2 | **Given** query 长度（trim 后）超出 `1..MAX_QUERY_LENGTH` **When** 调用 **Then** 以 `INVALID_QUERY` 失败结束，且不调用 embedding 或向量库 | 潜在 SDK 异常 / 性能风险 | 单元 | givenTooLongQueryWhenExecuteThenInvalidQuery |
| M3 | **Given** query 含首尾空白但内容合法 **When** 调用 **Then** 必须先对 query 做 `trim` 归一化，并使用归一化后的文本作为 embedding 与检索的输入 | 结果不稳定 / 难复现 | 单元 | givenQueryWithSurroundingSpacesWhenExecuteThenUsesTrimmedQuery |
| M4 | **Given** embedding 阶段超时 **When** 触发超时 **Then** 必须以 `EMBEDDING_TIMEOUT` 失败结束，并终止后续步骤（不得调用向量库） | 错误分类 / 继续消耗资源 | 单元 | givenEmbeddingTimeoutWhenExecuteThenEmbeddingTimeout |
| M5 | **Given** 向量检索阶段超时 **When** 触发超时 **Then** 必须以 `VECTOR_SEARCH_TIMEOUT` 失败结束 | 错误分类 / 误导重试策略 | 单元 | givenVectorSearchTimeoutWhenExecuteThenVectorSearchTimeout |
| M6 | **Given** 触发总超时预算 `T_total` **When** 超过总预算 **Then** 必须以 `TOTAL_TIMEOUT` 失败结束，并终止流程 | 请求悬挂 / 线程池耗尽 | 单元 | givenTotalTimeoutWhenExecuteThenTotalTimeout |
| M7 | **Given** embedding 成功且向量库存在可检索记录 **When** 调用 **Then** 必须以 `topK=1` 的检索语义返回 top-1（余弦相似度最大/距离最小）的唯一结果，且结果字段包含 `chunkId/text/score/metadata` | 返回非 top-1 / 字段缺失 | 组件/集成 | givenValidQueryWhenSearchThenReturnTop1AndUseTopKOne |
| M8 | **Given** 向量库无可检索记录（返回空列表） **When** 调用 **Then** 必须以 `RETRIEVAL_NOT_FOUND` 失败结束（业务可判定的分支） | 上游误判为系统故障 | 单元 | givenVectorReturnsEmptyWhenExecuteThenNotFound |
| M9 | **Given** 任一阶段发生失败（INVALID_QUERY/超时/网络/配置错误） **When** 失败发生 **Then** 不允许无限重试；在单次 `execute` 调用内，embedding 调用次数应为 **至多一次**（除非显式引入并在契约中声明重试策略） | 重复计费 / 副作用不可控 | 单元 | givenFailureWhenExecuteThenNoImplicitRetry |

## UNKNOWN（明确不保证）
- 当调用因超时或网络中断失败时，embedding 请求是否已在下游实际执行、以及是否产生计费，无法保证。

## Gaps（需要补可观测性或测试支撑）
- 日志中禁止记录完整 query（建议仅记录长度、前 N 字符或哈希）；需通过日志采集校验。
- `requestId` 贯穿日志与下游调用（如 header/trace）；需链路追踪或日志字段校验支持。
