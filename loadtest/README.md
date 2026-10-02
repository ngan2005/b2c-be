# k6 Load Test Scripts

Scripts used to run the E0–E3 concurrency experiments for the Order/Inventory checkout
pipeline. They assume a buyer user has been pre-provisioned with at least one cart item
pointing to a low-stock variant.

## Usage

```bash
# 1. Start the backend with the desired strategy profile
SPRING_PROFILES_ACTIVE=mock,locking-e0 ./gradlew bootRun
SPRING_PROFILES_ACTIVE=mock,locking-e1 ./gradlew bootRun   # default production
SPRING_PROFILES_ACTIVE=mock,locking-e2 ./gradlew bootRun
SPRING_PROFILES_ACTIVE=mock,locking-e3 ./gradlew bootRun

# 2. Login to obtain a buyer JWT token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"buyer@example.com","password":"password"}' | jq -r .data.accessToken)

# 3. Add an item to the cart (so we have cartItemIds=1)
# ... (see API docs)

# 4. Run the experiment
k6 run -e BASE=http://localhost:8080 -e TOKEN=$TOKEN \
       -e VARIANT_ID=1 -e CART_ITEM_ID=1 -e ADDRESS_ID=1 \
       loadtest/checkout-e0.js
```

## Metrics

After each run, inspect the recorded experiment via:

```bash
GET /api/v1/inventory/load-test/results   (requires ADMIN role)
```

The `load_test_result` table includes: scenario, oversell count, throughput, p50/p95/p99
latency, and conflict count. Use this table to build the final comparison chart in the
thesis report.
