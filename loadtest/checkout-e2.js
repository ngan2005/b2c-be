// k6 load test for the E2 (pessimistic locking) strategy.
// Expects higher p95 latency but zero oversells.
import http from 'k6/http';
import { check } from 'k6';

export const options = {
    scenarios: {
        checkout_race: {
            executor: 'shared-iterations',
            vus: 50,
            iterations: 200,
            maxDuration: '2m',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<2000'],
    },
};

const BASE = __ENV.BASE || 'http://localhost:8080';
const TOKEN = __ENV.TOKEN || '';

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
            'Idempotency-Key': `e2-${__VU}-${__ITER}`,
        },
    });
    check(res, {
        'expected status': (r) => r.status === 201 || r.status === 409 || r.status === 400,
    });
}
