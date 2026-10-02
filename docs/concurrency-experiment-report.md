# Concurrency Experiment Report — E0 / E1 / E2 / E3

> Template for the final thesis report. Replace the placeholder numbers below with
> the actual values recorded by the `load_test_result` table after each run.

## Setup

| Property | Value |
|---|---|
| Variant id | `1` |
| Initial stock | `10` |
| Concurrent VUs | `50` |
| Iterations per VU | `4` |
| Total attempts | `200` |
| Backend profile | `mock,locking-eN` (N ∈ {0, 1, 2, 3}) |
| DB | H2 in-memory (mock profile) |
| Java | 21 |
| Spring Boot | 4.1.1 |

## Raw Results

| Scenario | Final stock | Reserved | Successful orders | Failed orders | Oversell count | Conflict count | p50 (ms) | p95 (ms) | p99 (ms) | Throughput (rps) |
|---|---|---|---|---|---|---|---|---|---|---|
| **E0** no locking |  |  |  |  |  | n/a |  |  |  |  |
| **E1** optimistic |  |  |  |  | 0 |  |  |  |  |  |
| **E2** pessimistic |  |  |  |  | 0 | n/a |  |  |  |  |
| **E3** optimistic+retry |  |  |  |  | 0 |  |  |  |  |  |

## Analysis

### E0 (Baseline)
- Demonstrates the oversell race condition.
- Expected observation: multiple concurrent transactions read the same stock value
  and all succeed, leading to `oversell_count > 0`.
- The success rate is misleadingly high because the system "trusts" every read.

### E1 (Optimistic Locking)
- Hibernate throws `OptimisticLockingFailureException` when a version mismatch is
  detected. The application translates this into `OutOfStockException` (HTTP 409).
- No oversells because each successful reserve is committed against a known version.
- p95 latency may spike under heavy contention due to repeated failures.

### E2 (Pessimistic Locking)
- `SELECT ... FOR UPDATE` serialises concurrent reservations. Higher p95 latency
  is the trade-off.
- Guaranteed consistency: zero oversells, zero lost updates.

### E3 (Optimistic + Retry)
- Same lock-free read as E1, but the application catches
  `OptimisticLockingFailureException` and retries up to N times with linear
  back-off. Higher success rate than E1 at the cost of slightly higher p95.

## Recommendation

For the production B2C system:

- **Default**: E1 (optimistic) — best balance of throughput and correctness for the
  typical workload (low-medium contention).
- **High-contention SKUs** (e.g. flash sale, low stock): switch to E3 for the
  duration of the event. E2 is rarely the right choice outside of batch processing
  because it holds the row lock for the full reservation transaction.

## How to Reproduce

```bash
# 1. Boot with the desired strategy
SPRING_PROFILES_ACTIVE=mock,locking-e0 ./gradlew bootRun

# 2. Run k6 (replace the placeholders)
k6 run -e BASE=http://localhost:8080 -e TOKEN=$TOKEN \
       -e VARIANT_ID=1 -e CART_ITEM_ID=1 -e ADDRESS_ID=1 \
       loadtest/checkout-e0.js

# 3. Inspect the recorded experiment
curl -H "Authorization: Bearer $ADMIN_TOKEN" \
     http://localhost:8080/api/v1/inventory/load-test/results
```
