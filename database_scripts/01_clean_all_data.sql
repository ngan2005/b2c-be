-- =============================================================================
-- FILE 1: 01_clean_all_data.sql
-- MỤC ĐÍCH: XÓA SẠCH TOÀN BỘ DỮ LIỆU CÁC BẢNG (RESET TOÀN BỘ VỀ RỖNG)
--
-- LƯU Ý QUAN TRỌNG:
-- 1. Bảng flyway_schema_history TUYỆT ĐỐI ĐƯỢC GIỮ NGUYÊN (không xóa, không sửa)
--    để hệ thống Flyway của Spring Boot không bị lỗi conflict phiên bản migration.
-- 2. Dùng lệnh TRUNCATE TABLE ... RESTART IDENTITY CASCADE của PostgreSQL:
--    - Tự động xóa sạch dữ liệu của toàn bộ 34 bảng.
--    - Tự động reset tất cả các sequence (ID tự tăng) về lại 1.
--    - Tự động cascade xử lý triệt để toàn bộ khóa ngoại (Foreign Keys).
-- =============================================================================

BEGIN;

-- Tắt kiểm tra trigger nếu cần và dọn sạch dữ liệu các bảng nghiệp vụ
TRUNCATE TABLE 
    -- 1. Các bảng chi tiết và liên kết con cấp sâu nhất
    variant_option_value,
    product_image,
    product_option_value,
    product_option,
    product_variant,
    cart_item,
    cart,
    order_item,
    payment,
    shipment,
    order_dispute,
    orders,
    review,
    wishlist,
    message,
    conversation,
    seller_notification,
    seller_payout,
    commission_record,
    commission_rule,
    platform_wallet_txn,
    platform_wallet,
    seller_application,
    password_reset_token,
    idempotency_record,
    load_test_result,
    promotion,

    -- 2. Các bảng thực thể cha (Sản phẩm, Danh mục, Người bán, Địa chỉ)
    product,
    category,
    address,
    seller,

    -- 3. Các bảng tài khoản và phân quyền
    user_roles,
    users,
    role
RESTART IDENTITY CASCADE;

COMMIT;

SELECT 'CLEAN_ALL_DATA_SUCCESS' AS status;

