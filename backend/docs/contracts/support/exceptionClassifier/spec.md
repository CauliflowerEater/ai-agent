# ExceptionClassifier Spec

## Intent
将任意 Throwable 归类为稳定的粗粒度失败语义，并以统一的 `BusinessException` 形态对外抛出，供 Infra Adapter / UseCase 进行一致的错误分流与返回。

## Scope
- Applies to: EmbeddingClient Adapter、VectorStore Adapter 等所有需要对下游异常进行粗粒度分流的实现。
- Coarse-grained only: 不做细粒度错误码（auth/quota/rate-limit 等）的强承诺。

## Responsibilities
1. 遍历 Throwable cause chain，识别失败语义。
2. 输出稳定的 `BusinessException`：携带 domain 的 `ErrorCode`，并保留原始异常作为 `cause`。
3. 按固定优先级执行分类（见下文）。

## Non-Responsibilities
- 不负责重试/降级/熔断策略决策（由上层基于 ErrorCode 决定）。
- 不负责日志输出（由调用方决定何时记录与记录粒度）。
- 不负责细粒度上游业务错误码映射（可以后续增强，但不属于本组件契约）。

## API Contract
### Input
- `Throwable t`
- 允许 `t == null`：视为未知下游异常，按 UNKNOWN 处理。

### Output
- `BusinessException`
- `BusinessException.errorCode` 必须属于以下四类之一：
  - `ErrorCode.TIMEOUT_ERROR`
  - `ErrorCode.UPSTREAM_BUSINESS_ERROR`
  - `ErrorCode.NETWORK_ERROR`
  - `ErrorCode.UNKNOWN_INFRA_ERROR`
- `BusinessException.cause`：
  - 当 `t != null` 时必须保留 `t`（或其链路中的原始异常）作为 `cause`。
  - 当 `t == null` 时允许 `cause == null`。

## Classification Rules (Priority)
分类优先级 MUST 固定如下（从高到低）：

1) **TIMEOUT**
- 只要 cause chain 命中超时语义，即返回 `TIMEOUT_ERROR`。

2) **UPSTREAM_BUSINESS**
- 命中“下游服务业务异常”（例如 DashScopeException 或等价类型）即返回 `UPSTREAM_BUSINESS_ERROR`。

3) **NETWORK**
- 命中网络异常（WebClient/IO/DNS/SSL/Connect 等）即返回 `NETWORK_ERROR`。

4) **UNKNOWN**
- 以上均未命中则返回 `UNKNOWN_INFRA_ERROR`。

## Cause-chain traversal
- MUST 遍历 `Throwable#getCause()` 链路直至 null。
- 本版本不强制处理循环 cause 引用；若后续遇到实际问题，再以 mechanism/bugfix 的方式补强。

## Extensibility
- MAY 增加针对特定 SDK/驱动的识别规则以提升命中准确率。
- SHOULD 将 SDK/驱动耦合收敛在本组件内部，避免扩散到 Adapter/UseCase。

## Known gaps
- 当前未对循环 cause 引用/异常链过深做硬防护；如线上出现异常链导致性能/死循环问题，再补充保护与对应测试。
