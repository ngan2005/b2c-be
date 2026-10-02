// k6 load test for the E1 (optimistic locking) strategy.
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
        http_req_failed: ['rate<0.4'], // optimistic conflicts cause 409s
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
            'Idempotency-Key': `e1-${__VU}-${__ITER}`,
        },
    });
    check(res, {
        'expected status': (r) => r.status === 201 || r.status === 409 || r.status === 400,
    });
}
