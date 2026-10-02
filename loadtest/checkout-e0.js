// k6 load test for the E0 (no locking) baseline.
// Run with:
//   k6 run -e SCENARIO=E0 -e VARIANT_ID=1 -e TOKEN=... -e BASE=http://localhost:8080 \
//          loadtest/checkout-e0.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
    scenarios: {
        checkout_race: {
            executor: 'shared-iterations',
            vus: 50,
            iterations: 200,
            maxDuration: '1m',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.5'], // E0 may have many failures
    },
};

const BASE = __ENV.BASE || 'http://localhost:8080';
const VARIANT_ID = parseInt(__ENV.VARIANT_ID || '1', 10);
const TOKEN = __ENV.TOKEN || '';
const IDEM = __ENV.IDEM || `e0-${__VU}-${__ITER}`;

export default function () {
    const payload = JSON.stringify({
        cartItemIds: [parseInt(__ENV.CART_ITEM_ID || '1', 10)],
        addressId: parseInt(__ENV.ADDRESS_ID || '1', 10),
        paymentMethod: 'COD',
    });
    const res = http.post(`${BASE}/api/v1/buyer/orders`, payload, {
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${TOKEN}`,
            'Idempotency-Key': IDEM,
        },
    });
    check(res, {
        'accepted or rejected with 409': (r) => r.status === 201 || r.status === 409 || r.status === 400,
    });
}
