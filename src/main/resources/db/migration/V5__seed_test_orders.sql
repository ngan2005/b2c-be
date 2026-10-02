-- =========================================================
-- V5: Seed test orders for the seller dashboard
-- =========================================================
-- Looks up the demo buyer/seller by email rather than hard-coded IDs so the
-- seed works regardless of the sequence values produced by V2/V3.
-- Idempotent via NOT EXISTS guard on order_code.
-- =========================================================

INSERT INTO orders (
    order_code, buyer_id, seller_id,
    receiver_name, receiver_phone, shipping_address,
    subtotal, shipping_fee, discount_amount, platform_discount,
    total_amount,
    payment_method, payment_status, status,
    note, confirmed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    created_at, updated_at
)
SELECT
    v.order_code,
    (SELECT id FROM users WHERE email = v.buyer_email),
    (SELECT s.id FROM seller s JOIN users u ON u.id = s.user_id WHERE u.email = v.seller_email),
    v.receiver_name, v.receiver_phone, v.shipping_address,
    v.subtotal, v.shipping_fee, v.discount_amount, v.platform_discount,
    v.total_amount,
    v.payment_method, v.payment_status, v.status,
    v.note, v.confirmed_at, v.shipped_at, v.delivered_at, v.completed_at, v.cancelled_at,
    v.created_at, v.updated_at
FROM (VALUES
    ('ORD-2026-0001', 'demo.buyer@example.com', 'demo.seller@example.com',
     'Nguyen Van A', '0900000001', '12 Le Loi, District 1, HCMC',
     500000.00, 30000.00, 0.00, 0.00, 530000.00,
     'COD', 'UNPAID', 'PENDING',
     NULL, NULL, NULL, NULL, NULL, NULL,
     CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '10 days'),
    ('ORD-2026-0002', 'demo.buyer@example.com', 'demo.seller@example.com',
     'Nguyen Van A', '0900000001', '12 Le Loi, District 1, HCMC',
     1200000.00, 30000.00, 0.00, 0.00, 1230000.00,
     'VNPAY', 'PAID', 'CONFIRMED',
     NULL, CURRENT_TIMESTAMP - INTERVAL '8 days', NULL, NULL, NULL, NULL,
     CURRENT_TIMESTAMP - INTERVAL '9 days', CURRENT_TIMESTAMP - INTERVAL '8 days'),
    ('ORD-2026-0003', 'demo.buyer@example.com', 'demo.seller@example.com',
     'Nguyen Van A', '0900000001', '12 Le Loi, District 1, HCMC',
     850000.00, 30000.00, 50000.00, 0.00, 830000.00,
     'VNPAY', 'PAID', 'SHIPPING',
     'Fragile items', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '5 days', NULL, NULL, NULL,
     CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '5 days'),
    ('ORD-2026-0004', 'demo.buyer@example.com', 'demo.seller@example.com',
     'Nguyen Van A', '0900000001', '12 Le Loi, District 1, HCMC',
     2300000.00, 0.00, 0.00, 0.00, 2300000.00,
     'COD', 'PAID', 'DELIVERED',
     NULL, CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '1 days', NULL, NULL,
     CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '1 days'),
    ('ORD-2026-0005', 'demo.buyer@example.com', 'demo.seller@example.com',
     'Nguyen Van A', '0900000001', '12 Le Loi, District 1, HCMC',
     450000.00, 30000.00, 0.00, 0.00, 480000.00,
     'MOMO', 'PAID', 'COMPLETED',
     NULL, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '14 days', CURRENT_TIMESTAMP - INTERVAL '12 days', CURRENT_TIMESTAMP - INTERVAL '10 days', NULL,
     CURRENT_TIMESTAMP - INTERVAL '16 days', CURRENT_TIMESTAMP - INTERVAL '10 days'),
    ('ORD-2026-0006', 'demo.buyer@example.com', 'demo.seller@example.com',
     'Nguyen Van A', '0900000001', '12 Le Loi, District 1, HCMC',
     999000.00, 30000.00, 0.00, 0.00, 1029000.00,
     'VNPAY', 'REFUNDED', 'CANCELLED',
     NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '2 days',
     CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '2 days')
) AS v(order_code, buyer_email, seller_email,
        receiver_name, receiver_phone, shipping_address,
        subtotal, shipping_fee, discount_amount, platform_discount, total_amount,
        payment_method, payment_status, status,
        note, confirmed_at, shipped_at, delivered_at, completed_at, cancelled_at,
        created_at, updated_at)
WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.order_code = v.order_code)
  AND EXISTS (SELECT 1 FROM users u WHERE u.email = v.buyer_email)
  AND EXISTS (SELECT 1 FROM seller s JOIN users u ON u.id = s.user_id WHERE u.email = v.seller_email);