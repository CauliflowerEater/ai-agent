

# ExceptionClassifier MUST

本文件定义 ExceptionClassifier 的**最小强约束行为**，用于单元测试与回归验证。
所有 MUST 条款均以“异常语义分流正确性”为目标，而非穷举下游 SDK 的异常形态。

---

## MUST-1：Null 输入兜底
**Given** 输入 `Throwable t == null`
**When** 调用 exceptionClassifier
**Then** 必须返回 `BusinessException`
- `errorCode == ErrorCode.UNKNOWN_INFRA_ERROR`
- `cause == null`

---

## MUST-2：超时语义优先级最高
**Given** 任意 `Throwable` 的 cause chain 中存在“超时语义”异常
**When** 调用 exceptionClassifier
**Then** 必须返回 `BusinessException`
- `errorCode == ErrorCode.TIMEOUT_ERROR`
- 不论同一 cause chain 中是否同时存在网络异常或上游业务异常

> 说明：TIMEOUT 的判定优先级必须高于所有其他分类规则。

---

## MUST-3：上游业务异常识别
**Given** 任意 `Throwable` 的 cause chain 中存在“下游服务业务异常”（如 DashScopeException 或等价类型）
**And** cause chain 中不存在超时语义
**When** 调用 exceptionClassifier
**Then** 必须返回 `BusinessException`
- `errorCode == ErrorCode.UPSTREAM_BUSINESS_ERROR`

---

## MUST-4：网络异常识别
**Given** 任意 `Throwable` 的 cause chain 中存在网络异常（WebClient / IO / DNS / SSL / Connect 等）
**And** cause chain 中不存在超时语义
**And** cause chain 中不存在上游业务异常
**When** 调用 exceptionClassifier
**Then** 必须返回 `BusinessException`
- `errorCode == ErrorCode.NETWORK_ERROR`

---

## MUST-5：未知异常兜底
**Given** 任意 `Throwable`
**And** cause chain 中未命中超时语义、上游业务异常、网络异常
**When** 调用 exceptionClassifier
**Then** 必须返回 `BusinessException`
- `errorCode == ErrorCode.UNKNOWN_INFRA_ERROR`

---

## MUST-6：Cause 保留
**Given** 输入 `Throwable t != null`
**When** exceptionClassifier 返回 `BusinessException`
**Then** `BusinessException.getCause()` 必须保留原始异常 `t` 或其 cause chain 中的原始异常对象

---

## MUST-7：不抛出额外异常
**Given** 任意输入（包括 null、异常链复杂的 Throwable）
**When** 调用 exceptionClassifier
**Then** 不得抛出新的运行时异常；必须始终返回一个 `BusinessException`

---

## Scope Clarification
- 本 MUST 文件**不要求**识别所有第三方 SDK 的异常类型。
- SDK/驱动相关的异常识别规则属于实现细节，只要最终分类结果满足以上 MUST 即视为正确。
- 真实网络/超时行为的验证应在 mechanism / integration tests 中完成。
