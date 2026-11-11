-- =========================================================
-- REVIEW DATA SEED SCRIPT
-- Đường dẫn: database_scripts/review_data.sql
-- Tự động liên kết Order, Order Item và Review mẫu cho sản phẩm ID = 1
-- =========================================================

DO $$
DECLARE
    v_order_id BIGINT;
    v_buyer_id BIGINT;
    v_seller_id BIGINT;
    v_item1_id BIGINT;
    v_item2_id BIGINT;
    v_item3_id BIGINT;
BEGIN
    SELECT id INTO v_buyer_id FROM users WHERE email = 'demo.buyer@example.com' LIMIT 1;
    IF v_buyer_id IS NULL THEN
        SELECT id INTO v_buyer_id FROM users LIMIT 1;
    END IF;

    SELECT id INTO v_seller_id FROM seller WHERE id = 1 LIMIT 1;
    IF v_seller_id IS NULL THEN
        SELECT id INTO v_seller_id FROM seller LIMIT 1;
    END IF;

    IF v_buyer_id IS NOT NULL AND v_seller_id IS NOT NULL THEN
        -- Đảm bảo có một đơn hàng COMPLETED để gắn Order Items
        SELECT id INTO v_order_id FROM orders WHERE buyer_id = v_buyer_id LIMIT 1;
        IF v_order_id IS NULL THEN
            INSERT INTO orders (
                order_code, buyer_id, seller_id, receiver_name, receiver_phone,
                shipping_address, subtotal, shipping_fee, discount_amount,
                platform_discount, total_amount, payment_method, payment_status,
                status, created_at, updated_at
            )
            VALUES (
                'ORD-REV-DEMO-01', v_buyer_id, v_seller_id, 'Hoàng Minh', '0912345678',
                '12 Tràng Tiền, Hoàn Kiếm, Hà Nội', 398000, 0, 0, 0, 398000,
                'COD', 'PAID', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '5 days'
            )
            RETURNING id INTO v_order_id;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM review WHERE product_id = 1) THEN
            -- OrderItem 1: Trắng / L
            INSERT INTO order_item (
                order_id, product_id, variant_id, unit_price, quantity,
                total_price, product_name, variant_name, image_url,
                is_reviewed, created_at, updated_at
            )
            VALUES (
                v_order_id, 1, 2, 199000.00, 1, 199000.00,
                'Áo Thun Nam Cotton Compact Siêu Thoáng Khí Cao Cấp Chống Nhăn',
                'Trắng Tinh Khôi / L',
                'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500',
                true, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days'
            )
            RETURNING id INTO v_item1_id;

            -- OrderItem 2: Đen / L
            INSERT INTO order_item (
                order_id, product_id, variant_id, unit_price, quantity,
                total_price, product_name, variant_name, image_url,
                is_reviewed, created_at, updated_at
            )
            VALUES (
                v_order_id, 1, 4, 199000.00, 1, 199000.00,
                'Áo Thun Nam Cotton Compact Siêu Thoáng Khí Cao Cấp Chống Nhăn',
                'Đen Huyền Bí / L',
                'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=500',
                true, CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '4 days'
            )
            RETURNING id INTO v_item2_id;

            -- OrderItem 3: Trắng / M
            INSERT INTO order_item (
                order_id, product_id, variant_id, unit_price, quantity,
                total_price, product_name, variant_name, image_url,
                is_reviewed, created_at, updated_at
            )
            VALUES (
                v_order_id, 1, 1, 199000.00, 1, 199000.00,
                'Áo Thun Nam Cotton Compact Siêu Thoáng Khí Cao Cấp Chống Nhăn',
                'Trắng Tinh Khôi / M',
                'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500',
                true, CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '6 days'
            )
            RETURNING id INTO v_item3_id;

            -- Review 1: 5 sao kèm 2 ảnh feedback
            INSERT INTO review (
                order_item_id, product_id, variant_id, user_id, seller_id,
                rating, comment, image_urls, is_anonymous, seller_reply,
                replied_at, like_count, status, created_at, updated_at
            )
            VALUES (
                v_item1_id, 1, 2, v_buyer_id, v_seller_id,
                5, 'Áo đẹp xuất sắc, chất vải cotton compact mềm mát cực kỳ, form áo chuẩn regular mặc lên tôn dáng lắm. Sẽ ủng hộ shop thêm màu khác nữa!',
                '["https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500", "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=500"]'::jsonb,
                false, NULL, NULL, 8, 'VISIBLE',
                CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
            );

            -- Review 2: 5 sao kèm phản hồi người bán & 1 ảnh feedback
            INSERT INTO review (
                order_item_id, product_id, variant_id, user_id, seller_id,
                rating, comment, image_urls, is_anonymous, seller_reply,
                replied_at, like_count, status, created_at, updated_at
            )
            VALUES (
                v_item2_id, 1, 4, v_buyer_id, v_seller_id,
                5, 'Chất vải cotton dày dặn, form áo mặc rất vừa vặn và thoáng mát. Giao hàng cực nhanh, đóng gói cẩn thận 2 lớp!',
                '["https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=500"]'::jsonb,
                false, 'Dạ Coolmate cảm ơn bạn đã tin tưởng và đánh giá 5 sao cho sản phẩm ạ! Chúc bạn có trải nghiệm thật tuyệt vời cùng chiếc áo mới nhé!',
                CURRENT_TIMESTAMP - INTERVAL '1 days', 14, 'VISIBLE',
                CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days'
            );

            -- Review 3: 4 sao kèm phản hồi người bán
            INSERT INTO review (
                order_item_id, product_id, variant_id, user_id, seller_id,
                rating, comment, image_urls, is_anonymous, seller_reply,
                replied_at, like_count, status, created_at, updated_at
            )
            VALUES (
                v_item3_id, 1, 1, v_buyer_id, v_seller_id,
                4, 'Chất lượng áo tốt so với tầm giá, đường may chỉn chu. Giao hàng hơi chậm 1 ngày do mưa bão nhưng shop hỗ trợ nhiệt tình.',
                NULL, false, 'Dạ shop cảm ơn bạn đã góp ý ạ, shop sẽ cải thiện tốc độ giao hàng tốt hơn nữa ạ!',
                CURRENT_TIMESTAMP - INTERVAL '4 days', 3, 'VISIBLE',
                CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days'
            );
        END IF;
    END IF;
END $$;

