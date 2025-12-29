# VectorStore 机制说明

## V1. similaritySearch(topK = N) 必须遵守 topK 上限

**Given** 调用方传入 topK 参数为 N（N ≥ 1）  
**When** 调用 `similaritySearch(queryEmbedding, topK = N)`  
**Then** 返回结果列表的大小必须 `<= N`

- 责任层级：Infra（VectorStore 实现 / Driver）
- 说明：
  - VectorStore 不关心具体业务语义，仅保证遵守 topK 参数约束
  - 具体使用 topK = 1 / 5 / 10 属于 UseCase 决策
- 风险：若返回结果数量超过 N，将破坏所有依赖该参数的 UseCase 语义
- 验证方式：Integration Test（至少覆盖 N=1 与 N>1 两种情况）
- 建议测试类：`VectorStoreTopKLimitIT`

## V2. similaritySearch 返回结果必须保证正确排序（降序）

- 排序规则与 topK 数值无关，仅影响结果截断前的全序关系。
- 通过相似度分数对返回结果进行降序排序，确保最相似的结果位于列表前端
- VectorStore 实现应保证排序算法的稳定性和一致性
- 不同实现可能采用不同排序策略，但必须满足降序要求

## V3. similaritySearch 在无匹配结果时必须返回空列表

**Given** VectorStore 中不存在与 queryEmbedding 相匹配的向量记录  
**When** 调用 `similaritySearch(queryEmbedding, topK = N)`  
**Then** 必须返回空列表，而不是 `null`、占位记录或抛出异常

- 责任层级：Infra（VectorStore API 语义）
- 说明：
  - 空列表表示“合法但无结果”的确定性状态
  - VectorStore 不应通过异常或特殊值表达“无匹配”
- 风险：
  - 返回 `null` 会导致调用方 NPE
  - 抛出异常会破坏 UseCase 的 NotFound 分支语义
- 验证方式：Integration Test（空库 / 不相交向量）
- 建议测试类：`VectorStoreEmptyResultIT`

## V4. VectorStore 查询超时必须在本地失败返回

**Given** VectorStore 查询阻塞或响应时间超过 client / driver 配置的超时阈值  
**When** 超过超时配置  
**Then** 查询必须在本地失败返回（抛出异常 / onError），而不是无限阻塞等待

- 责任层级：Infra（VectorStore Client / Driver）
- 说明：
  - 超时语义由 client / driver 实现，而非 UseCase
  - 本地失败是资源保护与系统稳定性的必要条件
- 风险：
  - 若超时不生效，将导致线程、连接或连接池资源被长期占用
- 验证方式：Integration Test（人为延迟查询 / 限速）
- 建议测试类：`VectorStoreQueryTimeoutIT`

## V5. 查询超时失败不保证远端副作用未发生（明确为不可保证）

**Given** VectorStore 查询因超时在本地失败  
**Then** 不保证远端查询是否已实际执行、被取消或消耗计算资源

- 状态：UNKNOWN（不作为可验证机制条款）
- 说明：
  - 具体行为取决于 VectorStore 实现、网络协议与底层执行模型
  - UseCase 与 Infra 均不应对该行为作出强保证
- 对策：
  - 通过业务容忍度设计、幂等或限流进行风险控制

---

# 约束与说明

- VectorStore 作为基础设施组件，不承担业务语义，仅保证接口契约
- topK 参数由调用方传入，VectorStore 负责遵守，不做业务逻辑判断
- 返回结果数量 ≤ topK，排序满足降序，且稳定一致
- 本文档不定义任何固定的 topK 取值，topK 的选择属于 UseCase 契约范畴。
