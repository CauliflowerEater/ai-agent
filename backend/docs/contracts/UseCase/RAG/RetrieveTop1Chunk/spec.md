方法契约：retrieveTop1ChunkByQuery
	0.	方法定位
方法责任：

	•	将输入的自然语言 query 转换为向量 embedding
	•	在向量数据库中进行相似度检索
	•	返回与输入语句余弦相似度最高（cosine similarity 最大）的 top-1 chunk

不负责：
	•	embedding 模型的实现与参数选择
	•	向量数据库索引的构建、更新与维护
	•	chunk 的生成、切分与持久化
	•	检索结果的缓存、统计、日志持久化
	•	计费、额度管理、调用频控策略

边界：
	•	跨边界调用外部 embedding 模型 API
	•	跨边界访问向量数据库（如 pgvector / Milvus）

	1.	输入契约
输入字段：

	•	query: string（必填）

归一化规则：
	•	去除首尾空白（trim）
	•	统一使用 UTF-8 编码
	•	不做语义改写、不做语言检测、不做分词

校验规则：
	•	query 必须为非空字符串
	•	长度范围为 1..MAX_QUERY_LENGTH
	•	超出长度范围返回 INVALID_QUERY

默认值规则：
	•	无默认值，query 必须由调用方显式提供

不变项：
	•	单次方法调用中 query 内容不可被修改

安全约束：
	•	日志中禁止完整记录 query 内容
	•	允许记录：query 长度、前 N 个字符（如 128）及 hash

	2.	输出契约
成功返回结构：

	•	chunkId: string
	•	text: string
	•	score: number
	•	metadata: object

字段语义：
	•	chunkId：chunk 的唯一标识
	•	text：chunk 原文内容
	•	score：
	•	定义为余弦相似度（cosine similarity）
	•	数值越大表示越相似
	•	top-1 的含义为 score 最大
	•	等价表述：cosine distance = 1 - score 最小
	•	metadata：只读附加信息（章节、偏移等）

成功语义：
	•	返回成功表示本次 embedding 与向量检索流程已完成
	•	返回的是当前数据库中与 query 最相似的 chunk

NOT_FOUND 语义：
	•	当向量数据库中不存在任何可检索记录时返回 NOT_FOUND
	•	NOT_FOUND 不视为异常

输出稳定性：
	•	允许新增字段，但必须向后兼容
	•	已有字段语义不可改变，尤其是 score 的方向含义

	3.	幂等契约
幂等级别：

	•	本方法不承诺结果级幂等

重复请求语义：
	•	相同 query 的多次调用视为独立查询
	•	每次调用可能触发新的 embedding 请求
	•	不保证返回相同结果

说明：
	•	若调用方希望避免重复计费，应在上游自行实现 query 级缓存或去重策略

	4.	一致性契约
一致性级别：

	•	对外承诺强一致（Strong Consistency）

说明：
	•	返回成功即表示本次查询流程已完成
	•	不存在对外可见的中间态
	•	不暴露 INITING / PROCESSING 状态

	5.	失败语义契约
失败分类：

不可重试失败（Non-Retryable）：
	•	INVALID_QUERY：输入不合法
	•	MODEL_CONFIG_ERROR：embedding 维度与向量库 schema 不匹配
	•	VECTOR_SCHEMA_ERROR：向量库结构或配置错误

可重试失败（Retryable）：
	•	embedding API 网络错误
	•	embedding API 超时
	•	向量数据库网络错误
	•	向量数据库超时

UNKNOWN（重要）：
	•	当方法因超时或网络中断返回失败时：
	•	不保证 embedding 调用未发生
	•	embedding 可能已经成功并产生计费
	•	但结果未成功返回给调用方

成功受理边界：
	•	本方法不存在“已受理但未完成”的成功状态
	•	返回成功即表示流程完成
	•	超时不等价于未执行

调用方建议：
	•	UNKNOWN 场景允许重试
	•	重试可能导致再次调用 embedding，从而产生重复计费

	6.	副作用契约
副作用类型：

	•	外部经济副作用：embedding 模型调用可能产生计费、配额消耗或限流影响

副作用次数语义：
	•	embedding 调用为至少一次（At-least-once）
	•	在超时或重试场景下可能发生多次

副作用顺序：
	1.	调用 embedding 模型
	2.	使用 embedding 向量查询向量数据库

失败时副作用不确定性：
	•	允许 embedding 已发生但结果未返回
	•	系统不提供费用级补偿能力

	7.	性能与资源契约
复杂度上限：

	•	单次请求仅处理一条 query
	•	不支持批量输入

超时预算：
	•	总超时预算：T_total
	•	embedding 调用超时：T_embed
	•	向量数据库查询超时：T_vector

超时策略：
	•	任一阶段超时即终止流程并返回失败
	•	不进行内部无限重试

限流语义：
	•	被限流的请求视为未受理
	•	不触发 embedding 调用
	•	不触发向量数据库查询

	8.	并发与竞态契约

	•	支持并发调用
	•	方法内部不共享可变状态
	•	不涉及读-改-写操作
	•	不需要分布式锁
	•	不需要唯一性约束

	9.	可观测性契约
关联标识：

	•	若调用方提供 requestId，应贯穿日志与下游调用

关键事件点：
	•	embedding 调用开始与结束
	•	向量查询开始与结束
	•	超时与异常发生点

指标建议：
	•	embedding 调用耗时
	•	向量查询耗时
	•	超时率与失败率
	•	UNKNOWN 失败比例

一句话总结：
这是一个同步、强一致、只读语义的方法；
但由于依赖外部 embedding 服务，
在超时场景下允许“已计费但结果未知”，
调用方重试需自行承担重复计费风险。