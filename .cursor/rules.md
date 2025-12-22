# Project AI Coding Rules

## 1. Core Goal
- Minimize human cognitive load
- Humans should understand the system by reading:
  - Class names
  - Method names
  - Interfaces
  - Contract comments
  - Tests
- Humans should NOT need to read implementation details.

---

## 2. Architecture Rules

### 2.1 Layering
- api/: Controllers, DTOs, Error definitions
- domain/: Entities, Value Objects, Policies, (optional) domain services (pure logic)
- app/: UseCase implementations / orchestration only (workflow, transactions, progress updates)
- port/: Interfaces (Ports/Gateways) for external capabilities used by app/domain (DB/HTTP/MQ/Redis/LLM/VectorStore/File, etc.)
- infra/: Implementations (Adapters) of port interfaces: DB, HTTP, MQ, Redis, third-party SDKs, file IO
- support/: Cross-cutting concerns and configuration: logging, metrics, tracing, AOP/advisors, constants, @ConfigurationProperties, feature flags, wiring helpers

Dependency rules (MUST):
- api -> app
- app -> domain + port
- domain -> (no dependencies on api/app/infra/support)
- port -> (no dependencies on infra; keep framework-free as much as possible)
- infra -> port (+ domain types if needed)
- support may be used by api/app/infra (avoid using it from domain; keep domain pure)

Placement rules (MUST NOT):
- domain MUST NOT import Spring/DB/HTTP SDKs
- port MUST NOT reference concrete implementations
- infra MUST NOT be referenced by api/app/domain directly (only via port)
---

## 3. Interface & Contract Rules

### 3.1 Small Interfaces
- Each UseCase interface should have 1–3 methods only
- Avoid "god services"

### 3.2 Mandatory Contract Block
Every public method MUST include a contract comment in the following format:

/**
 * Intent:
 * Input:
 * Output:
 * SideEffects:
 * Failure:
 * Idempotency:
 */

### 3.3 Consistency, Idempotency & Repair Rules

When designing or generating any method, the following decision rules MUST be applied.

#### Consistency
- If a method has side effects (DB writes, MQ publish, HTTP calls, file IO, external SDK calls),
  and these effects are NOT fully completed within a single atomic and rollbackable boundary,
  the method MUST be treated as eventually consistent.
- Do NOT assume "method returns success = work is fully completed".
- If intermediate states are possible, they MUST be explicit (e.g. INITING / READY / FAILED).

#### Replay & Idempotency
- If a method may be retried, resumed, concurrently invoked, or executed again under unknown prior outcome,
  it MUST be replay-safe.
- Replay safety MUST be achieved by one of:
  - Idempotency keys with well-defined scope and uniqueness
  - Step-level deduplication / resumable workflow design
  - Database uniqueness constraints that collapse duplicates into a single outcome
- Do NOT rely on "this is an internal method" as a reason to skip idempotency.
  Internal methods are treated as replayable if they can be re-entered after failure.

#### Failure & Repair
- If a method is eventually consistent, it MUST define failure handling semantics:
  - Whether automatic repair is supported
  - Retry or resume strategy (if any)
  - Terminal failure state (e.g. FAILED) to avoid infinite intermediate states
- If partial effects cannot be safely repeated, a compensation or explicit manual recovery path MUST be defined.
- Infinite INITING or hanging states are forbidden; time bounds or termination rules MUST be specified.

#### Design Preference
- Prefer step-based, resumable workflows over one-shot implementations.
- Prefer explicit state transitions over implicit success assumptions.
- Avoid introducing compensation or idempotency unless replay is possible or expected.

---

## 4. Naming Rules (Strict)

### Forbidden Names
- process / handle / execute / do
- Manager / Helper / Util
- Common / Base / Misc

### Required Naming Style
- UseCase: Verb + DomainObject (e.g. CreateOrderUseCase)
- Policy: Noun + Policy (e.g. DiscountPolicy)
- Gateway: DomainObject + Gateway (e.g. InventoryGateway)

---

## 5. Testing Rules

### 5.1 Contract Tests First
- Write tests before or alongside implementations
- Tests define system behavior and serve as documentation

### 5.2 Test Types
- Contract Tests: required
- Architecture Tests: required
- E2E Tests: minimal and critical paths only

---

## 6. Implementation Rules

- Prefer strong typing over maps or generic objects
- Avoid implicit behavior and hidden side effects
- Implementations may change freely as long as contracts and tests pass

---

## 7. AI Output Style
- Prefer clarity over cleverness
- Prefer explicit over implicit
- Prefer readability over micro-optimizations