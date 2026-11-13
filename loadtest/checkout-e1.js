// k6 load test for the E1 (optimistic locking) strategy at 100 requests/second.
import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

// Custom metrics to separate business outcomes from system crashes
const ordersCreated = new Counter('orders_created');
const optimisticConflicts = new Counter('optimistic_conflicts');
const clientErrors = new Counter('client_errors');
const serverErrors = new Counter('server_errors');

export const options = {
    scenarios: {
        checkout_100_rps: {
            executor: 'constant-arrival-rate',
            rate: 100,              // 100 requests/giây
            timeUnit: '1s',
            duration: '1s',         // 100 requests trong 1 giây
            preAllocatedVUs: 100,   // 100 Virtual Users sẵn sàng bắn cùng lúc
            maxVUs: 150,
        },
    },
    thresholds: {
        // Tuyệt đối không được xảy ra lỗi sập backend (HTTP 500)
        server_errors: ['count==0'],
        // Tỉ lệ phản hồi hợp lệ (201, 409, 400) phải trên 95%
        checks: ['rate>0.95'],
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

    // Idempotency-Key ngẫu nhiên cho mỗi request để không bị coi là trùng lặp
    const uniqueKey = `e1-${__VU}-${__ITER}-${Date.now()}-${Math.floor(Math.random() * 1000000)}`;

    const res = http.post(`${BASE}/api/v1/buyer/orders`, payload, {
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${TOKEN}`,
            'Idempotency-Key': uniqueKey,
        },
    });

    if (res.status === 201) {
        ordersCreated.add(1);
    } else if (res.status === 409) {
        // 409: Bị chặn bởi khóa lạc quan (Optimistic Lock) hoặc OutOfStock — ĐÂY LÀ KẾT QUẢ MONG MUỐN KHI TRANH CHẤP
        optimisticConflicts.add(1);
    } else if (res.status === 400) {
        clientErrors.add(1);
    } else if (res.status >= 500) {
        serverErrors.add(1);
    }

    check(res, {
        'status is valid (201, 409, 400)': (r) => r.status === 201 || r.status === 409 || r.status === 400,
    });
}
