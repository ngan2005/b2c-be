-- =============================================================================
-- FILE 2: 02_seed_clean_data.sql
-- MỤC ĐÍCH: INSERT DỮ LIỆU CHUẨN, SẠCH, ĐẦY ĐỦ CÁC BẢNG CHO TOÀN BỘ HỆ THỐNG
--
-- TẤT CẢ TÀI KHOẢN CÓ MẬT KHẨU LÀ: password123
-- Hash Bcrypt: $2a$10$Xl7DfxWyr85GU9XFWLiQauLFg18wvHtCB7SBgqaucuPWV/JQ8AtcS
-- =============================================================================

BEGIN;

-- =============================================================================
-- 1. BẢNG ROLE (Vai trò hệ thống)
-- =============================================================================
INSERT INTO role (id, code, name, description, created_at)
VALUES 
    (1, 'BUYER',  'Buyer',  'Khách mua hàng cá nhân', CURRENT_TIMESTAMP),
    (2, 'SELLER', 'Seller', 'Nhà bán hàng (Chủ gian hàng)', CURRENT_TIMESTAMP),
    (3, 'ADMIN',  'Admin',  'Quản trị viên toàn hệ thống', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO UPDATE 
SET name = EXCLUDED.name, description = EXCLUDED.description;


-- =============================================================================
-- 2. BẢNG USERS (Tài khoản người dùng)
-- Mật khẩu chung: password123
-- =============================================================================
INSERT INTO users (
    id, role_id, email, phone, password_hash, full_name, avatar_url, 
    gender, date_of_birth, status, email_verified_at, phone_verified_at, created_at, updated_at
)
VALUES
    -- Admin
    (1, 3, 'admin@b2c.local', '0900000001', '$2a$10$Xl7DfxWyr85GU9XFWLiQauLFg18wvHtCB7SBgqaucuPWV/JQ8AtcS', 
     'Quản Trị Viên B2C', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200', 'MALE', '1990-01-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (13, 3, 'luu.kimngan205@gmail.com', '0900000013', '$2a$10$Xl7DfxWyr85GU9XFWLiQauLFg18wvHtCB7SBgqaucuPWV/JQ8AtcS', 
     'Lưu Kim Ngân', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200', 'FEMALE', '2000-01-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Sellers (Chủ shop)
    (2, 2, 'coolmate@gmail.com', '0900000002', '$2a$10$Xl7DfxWyr85GU9XFWLiQauLFg18wvHtCB7SBgqaucuPWV/JQ8AtcS', 
     'Coolmate Store Manager', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200', 'MALE', '1992-05-15', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (3, 2, 'sony@gmail.com', '0900000003', '$2a$10$Xl7DfxWyr85GU9XFWLiQauLFg18wvHtCB7SBgqaucuPWV/JQ8AtcS', 
     'Sony Flagship Representative', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200', 'MALE', '1988-11-20', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (4, 2, 'locknlock@gmail.com', '0900000004', '$2a$10$Xl7DfxWyr85GU9XFWLiQauLFg18wvHtCB7SBgqaucuPWV/JQ8AtcS', 
     'Lock&Lock Official Rep', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200', 'FEMALE', '1994-08-10', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (5, 2, 'logitech@gmail.com', '0900000005', '$2a$10$Xl7DfxWyr85GU9XFWLiQauLFg18wvHtCB7SBgqaucuPWV/JQ8AtcS', 
     'Logitech Audio Rep', 'https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=200', 'MALE', '1991-03-25', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (6, 2, 'anker@gmail.com', '0900000006', '$2a$10$Xl7DfxWyr85GU9XFWLiQauLFg18wvHtCB7SBgqaucuPWV/JQ8AtcS', 
     'Anker Vietnam Rep', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200', 'MALE', '1993-07-12', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Buyers (Khách hàng người mua)
    (10, 1, 'nguyenvana@gmail.com', '0987654321', '$2a$10$Xl7DfxWyr85GU9XFWLiQauLFg18wvHtCB7SBgqaucuPWV/JQ8AtcS', 
     'Nguyễn Văn A', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200', 'MALE', '1998-10-24', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (11, 1, 'tranthib@gmail.com', '0912345678', '$2a$10$Xl7DfxWyr85GU9XFWLiQauLFg18wvHtCB7SBgqaucuPWV/JQ8AtcS', 
     'Trần Thị B', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200', 'FEMALE', '2000-02-14', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (12, 1, 'buyer@b2c.local', '0911223344', '$2a$10$Xl7DfxWyr85GU9XFWLiQauLFg18wvHtCB7SBgqaucuPWV/JQ8AtcS', 
     'Khách Hàng Thử Nghiệm', 'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200', 'OTHER', '1999-09-09', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- =============================================================================
-- 3. BẢNG USER_ROLES (Phân quyền đa vai trò theo chuẩn V31)
-- =============================================================================
INSERT INTO user_roles (user_id, role_id, granted_at)
VALUES
    -- Admin có role ADMIN và BUYER
    (1, 3, CURRENT_TIMESTAMP),
    (1, 1, CURRENT_TIMESTAMP),
    (13, 3, CURRENT_TIMESTAMP),
    (13, 1, CURRENT_TIMESTAMP),

    -- Sellers có role SELLER và BUYER
    (2, 2, CURRENT_TIMESTAMP),
    (2, 1, CURRENT_TIMESTAMP),

    (3, 2, CURRENT_TIMESTAMP),
    (3, 1, CURRENT_TIMESTAMP),

    (4, 2, CURRENT_TIMESTAMP),
    (4, 1, CURRENT_TIMESTAMP),

    (5, 2, CURRENT_TIMESTAMP),
    (5, 1, CURRENT_TIMESTAMP),

    (6, 2, CURRENT_TIMESTAMP),
    (6, 1, CURRENT_TIMESTAMP),

    -- Buyers chỉ có role BUYER
    (10, 1, CURRENT_TIMESTAMP),
    (11, 1, CURRENT_TIMESTAMP),
    (12, 1, CURRENT_TIMESTAMP);


-- =============================================================================
-- 4. BẢNG SELLER (Gian hàng chính hãng - Mall)
-- =============================================================================
INSERT INTO seller (
    id, user_id, shop_name, slug, logo_url, banner_url, description, 
    business_type, rating_avg, rating_count, follower_count, total_product, status, created_at, updated_at
)
VALUES
    (1, 2, 'Coolmate Official Store', 'coolmate-official', 
     'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=200',
     'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1200',
     'Thương hiệu thời trang nam công nghệ tiên phong tại Việt Nam. Giải pháp mua sắm 100% hài lòng.',
     'COMPANY', 4.90, 12500, 350000, 185, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (2, 3, 'Sony Flagship Store', 'sony-flagship', 
     'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=200',
     'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=1200',
     'Kênh phân phối chính hãng các sản phẩm âm thanh, tai nghe, loa và công nghệ cao cấp từ Sony.',
     'COMPANY', 4.90, 8900, 240000, 120, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (3, 4, 'Lock&Lock Official Store', 'locknlock-official', 
     'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=200',
     'https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?w=1200',
     'Đồ gia dụng, bình giữ nhiệt, hộp bảo quản thực phẩm thông minh hàng đầu Hàn Quốc.',
     'COMPANY', 4.80, 6700, 180000, 210, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (4, 5, 'Logitech Official Store', 'logitech-official', 
     'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=200',
     'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=1200',
     'Bàn phím cơ, chuột không dây và phụ kiện công nghệ gaming chuẩn quốc tế.',
     'COMPANY', 4.90, 9400, 195000, 95, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (5, 6, 'Anker Official Store', 'anker-official', 
     'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=200',
     'https://images.unsplash.com/photo-1586953208448-b95a79798f07?w=1200',
     'Thương hiệu củ sạc, pin dự phòng, cáp sạc số 1 công nghệ sạc nhanh toàn cầu.',
     'COMPANY', 4.90, 11200, 280000, 80, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- =============================================================================
-- 5. BẢNG ADDRESS (Sổ địa chỉ người dùng & kho hàng)
-- =============================================================================
INSERT INTO address (
    id, user_id, recipient_name, phone, province_code, district_code, ward_code, 
    street_detail, full_address, type, is_default, created_at, updated_at
)
VALUES
    -- Kho hàng của Sellers
    (1, 2, 'Kho Vận Coolmate', '0900000002', '79', '760', '26734', 
     '163 Hai Bà Trưng', '163 Hai Bà Trưng, Phường 6, Quận 3, TP. Hồ Chí Minh', 'OFFICE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (2, 3, 'Kho Sony Flagship', '0900000003', '01', '001', '00001', 
     'Tòa nhà Landmark 72', 'Tòa nhà Landmark 72, Phạm Hùng, Nam Từ Liêm, Hà Nội', 'OFFICE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Sổ địa chỉ của Buyer Nguyễn Văn A (User 10)
    (10, 10, 'Nguyễn Văn A', '0987654321', '79', '760', '26734', 
     'Số 123 Đường Nguyễn Huệ', 'Số 123 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', 'HOME', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (11, 10, 'Nguyễn Văn A (Văn phòng)', '0987654321', '79', '760', '26740', 
     'Tòa nhà Bitexco, Tầng 18, 2 Hải Triều', 'Tòa nhà Bitexco Financial Tower, Tầng 18, 2 Hải Triều, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', 'OFFICE', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (12, 10, 'Trần Thị B (Mẹ)', '0912345678', '46', '474', '19876', 
     '45 Lê Lợi', '45 Lê Lợi, Phường Vĩnh Ninh, TP. Huế, Thừa Thiên Huế', 'HOME', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- =============================================================================
-- 6. BẢNG CATEGORY (Danh mục sản phẩm đa cấp)
-- Khớp chuẩn với 8 Danh mục nhanh trên Mobile App
-- =============================================================================
INSERT INTO category (id, parent_id, name, slug, icon_url, level, path, sort_order, is_active, created_at, updated_at)
VALUES
    -- 8 Danh mục Gốc (Level 1)
    (1, NULL, 'Thời trang',       'thoi-trang',       'bi bi-person',         1, '/1', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, NULL, 'Điện tử',          'dien-tu',          'bi bi-laptop',         1, '/2', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, NULL, 'Gia dụng',         'gia-dung',         'bi bi-house-door',     1, '/3', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, NULL, 'Làm đẹp',          'lam-dep',          'bi bi-stars',          1, '/4', 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, NULL, 'Giày dép',         'giay-dep',         'bi bi-bicycle',        1, '/5', 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, NULL, 'Đồng hồ',          'dong-ho',          'bi bi-watch',          1, '/6', 6, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, NULL, 'Mẹ & Bé',          'me-va-be',         'bi bi-balloon-heart',  1, '/7', 7, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, NULL, 'Bách hóa',         'bach-hoa',         'bi bi-cup-hot',        1, '/8', 8, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Các Danh mục Con (Level 2)
    (11, 1, 'Áo Thun Nam',        'ao-thun-nam',      'bi bi-tag',            2, '/1/11', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 1, 'Áo Sơ Mi Nam',       'ao-so-mi-nam',     'bi bi-tag',            2, '/1/12', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, 1, 'Quần Nam & Đồ Lót',  'quan-nam',         'bi bi-tag',            2, '/1/13', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (21, 2, 'Tai Nghe & Loa',     'tai-nghe-loa',     'bi bi-headphones',     2, '/2/21', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (22, 2, 'Bàn Phím & Chuột',   'ban-phim-chuot',   'bi bi-keyboard',       2, '/2/22', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 2, 'Củ Sạc & Cáp Sạc',   'cu-sac-cap-sac',   'bi bi-battery-charging', 2, '/2/23', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (31, 3, 'Bình Giữ Nhiệt & Cốc','binh-giu-nhiet',  'bi bi-cup',            2, '/3/31', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (32, 3, 'Dụng Cụ Pha Cà Phê', 'dung-cu-pha-ca-phe','bi bi-cup-straw',     2, '/3/32', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (51, 5, 'Balo & Túi Xách',    'balo-tui-xach',    'bi bi-bag',            2, '/5/51', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (52, 5, 'Giày Sneaker Nam Nữ','giay-sneaker',     'bi bi-tag',            2, '/5/52', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- =============================================================================
-- 7. BẢNG PRODUCT (Sản phẩm chuẩn 100% hiển thị trên App)
-- =============================================================================
INSERT INTO product (
    id, seller_id, category_id, name, slug, description, brand, thumbnail_url,
    min_price, max_price, status, rating_avg, rating_count, sold_count, view_count,
    weight_gram, length_cm, width_cm, height_cm, created_at, updated_at
)
VALUES
    -- SP 1: Áo Thun Nam Coolmate (Best Seller #1)
    (1, 1, 11, 'Áo Thun Nam Cotton Compact Siêu Thoáng Khí Cao Cấp Chống Nhăn', 'ao-thun-nam-cotton-compact', 
     'Áo thun nam Cotton Compact là sự kết hợp hoàn hảo giữa sợi bông tự nhiên cao cấp và công nghệ dệt nén sợi siêu bền. Bề mặt vải mềm mịn gấp 2 lần cotton thông thường, thấm hút mồ hôi tối đa và không bai xù khi giặt máy.',
     'Coolmate', 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&auto=format&fit=crop&q=80',
     199000.00, 299000.00, 'ACTIVE', 4.95, 342, 5800, 24000, 200, 30.0, 20.0, 2.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 2: Tai nghe Sony chống ồn (Best Seller #2)
    (2, 2, 21, 'Tai nghe Bluetooth chống ồn Sony WH-1000XM5 Âm Bass Đỉnh Cao', 'tai-nghe-bluetooth-chong-on-sony', 
     'Tai nghe chụp tai không dây chống ồn hàng đầu thế giới với bộ xử lý V1 và HD QN1 chuyên dụng. Tích hợp 8 micro thu âm đàm thoại trong trẻo, thời lượng pin 30 giờ liên tục kèm sạc nhanh 3 phút nghe 3 giờ.',
     'Sony', 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500&auto=format&fit=crop&q=80',
     299000.00, 460000.00, 'ACTIVE', 4.90, 185, 2400, 18500, 250, 22.0, 18.0, 6.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 3: Bình giữ nhiệt Lock&Lock nhiệt độ thông minh
    (3, 3, 31, 'Bình giữ nhiệt Lock&Lock nhiệt độ thông minh inox 316 cảm ứng LED', 'binh-giu-nhiet-locknlock-thong-minh', 
     'Bình giữ nhiệt chất liệu ruột inox 316 y tế cao cấp chống ăn mòn, nắp cảm ứng một chạm hiển thị nhiệt độ chính xác bằng đèn LED thông minh. Giữ nóng 12h và giữ lạnh 24h trọn vẹn hương vị thức uống.',
     'Lock&Lock', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500&auto=format&fit=crop&q=80',
     149000.00, 298000.00, 'ACTIVE', 4.85, 92, 1850, 12000, 350, 25.0, 8.0, 8.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 4: Bàn phím cơ Bluetooth kèm kê tay gỗ
    (4, 4, 22, 'Bàn phím cơ Bluetooth không dây Logitech kèm kê tay gỗ tự nhiên', 'ban-phim-co-bluetooth-logitech', 
     'Bàn phím cơ đa kết nối Bluetooth 5.1 và USB 2.4Ghz tương thích Windows/Mac/iOS/Android. Switch cơ gõ cực êm, đèn nền LED RGB tùy biến và tặng kèm đệm kê tay gỗ óc chó công thái học chống mỏi cổ tay.',
     'Logitech', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&auto=format&fit=crop&q=80',
     590000.00, 739000.00, 'ACTIVE', 4.90, 78, 920, 8600, 850, 35.0, 15.0, 4.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 5: Củ sạc nhanh Anker GaN 65W 3 cổng
    (5, 5, 23, 'Củ sạc nhanh Anker GaNPrime 65W 3 cổng Type-C & USB-A sạc Laptop', 'cu-sac-nhanh-anker-gan-65w', 
     'Củ sạc nhỏ gọn công nghệ GaNPrime tối tân sạc đồng thời 3 thiết bị cùng lúc: sạc nhanh MacBook Air/Pro, iPhone 15/16 và phụ kiện. Tự động điều phối công suất PowerIQ 4.0 bảo vệ pin an toàn tuyệt đối.',
     'Anker', 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=500&auto=format&fit=crop&q=80',
     399000.00, 550000.00, 'ACTIVE', 4.92, 210, 3100, 16000, 120, 6.0, 4.0, 3.5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 6: Balo canvas unisex thời trang Hàn Quốc (Gợi ý #1)
    (6, 1, 51, 'Balo canvas unisex thời trang cao cấp phong cách Hàn Quốc', 'balo-canvas-unisex-thoi-trang-han-quoc', 
     'Balo unisex chất vải canvas cao cấp, chống trầy xước, ngăn chứa laptop tiện lợi cho sinh viên và dân văn phòng. Thiết kế tối giản, quai đeo đệm xốp êm ái thoáng khí.',
     'Coolmate', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500&auto=format&fit=crop&q=80',
     245000.00, 350000.00, 'ACTIVE', 4.90, 140, 1800, 9500, 450, 42.0, 30.0, 14.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 7: Bộ phễu pha cà phê gốm sứ thủ công kèm bình thủy tinh chịu nhiệt
    (7, 3, 32, 'Bộ phễu pha cà phê gốm sứ thủ công kèm bình thủy tinh chịu nhiệt', 'bo-pheu-pha-ca-phe-gom-su', 
     'Thiết kế gốm sứ tráng men mộc mạc phong cách vintage Nhật Bản, bình hứng thủy tinh Borosilicate chịu nhiệt cao cấp. Tặng kèm 100 tờ giấy lọc cà phê chuyên dụng.',
     'Lock&Lock', 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=500&auto=format&fit=crop&q=80',
     320000.00, 450000.00, 'ACTIVE', 4.80, 65, 950, 6200, 600, 20.0, 15.0, 18.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 8: Đế sạc nhanh không dây 3 trong 1 chuẩn Qi Anker
    (8, 5, 23, 'Đế sạc nhanh không dây 3 trong 1 chuẩn Qi đa năng cho điện thoại, đồng hồ', 'de-sac-nhanh-khong-day-3-in-1-anker', 
     'Hỗ trợ sạc không dây đồng thời iPhone, Apple Watch và tai nghe không dây với công suất lên đến 15W tản nhiệt an toàn. Chân đế gập gọn tiện lợi khi du lịch.',
     'Anker', 'https://images.unsplash.com/photo-1586953208448-b95a79798f07?w=500&auto=format&fit=crop&q=80',
     489000.00, 650000.00, 'ACTIVE', 4.70, 190, 3400, 14500, 210, 14.0, 10.0, 2.5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 9: Chuột không dây Bluetooth Silent Logitech
    (9, 4, 22, 'Chuột không dây Bluetooth Logitech Silent nút bấm êm chống ồn 90%', 'chuot-khong-day-bluetooth-logitech-silent', 
     'Nút bấm công nghệ SilentTouch giảm 90% tiếng ồn, kết nối Bluetooth tiết kiệm pin lên đến 18 tháng sử dụng liên tục. Mắt đọc quang học độ phân giải cao di chuột mượt mà trên mọi bề mặt.',
     'Logitech', 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500&auto=format&fit=crop&q=80',
     185000.00, 250000.00, 'ACTIVE', 4.90, 310, 6800, 28000, 95, 10.0, 6.0, 3.8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 10: Áo sơ mi nam Oxford form Regular Coolmate
    (10, 1, 12, 'Áo sơ mi nam Oxford form Regular tay dài vải sợi tre tự nhiên chống nhăn', 'ao-so-mi-nam-oxford-coolmate', 
     'Chất vải Bamboo kết hợp sợi bông Oxford dệt tổ ong thoáng mát, chống nhăn xù tối ưu và bền màu sau nhiều lần giặt. Tôn vóc dáng nam tính, lịch lãm nơi công sở.',
     'Coolmate', 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500&auto=format&fit=crop&q=80',
     279000.00, 399000.00, 'ACTIVE', 4.80, 240, 5400, 21000, 280, 32.0, 24.0, 3.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 11: Loa bluetooth không dây phong cách Vintage
    (11, 2, 21, 'Loa bluetooth không dây phong cách Vintage bọc da quai xách cổ điển', 'loa-bluetooth-khong-day-vintage', 
     'Âm bass trầm ấm đầy nội lực, pin nghe nhạc liên tục 12 tiếng, thiết kế hoài cổ sang trọng trang trí phòng khách và góc làm việc.',
     'Sony', 'https://images.unsplash.com/photo-1545454675-3531b543be5d?w=500&auto=format&fit=crop&q=80',
     429000.00, 599000.00, 'ACTIVE', 4.90, 80, 1200, 7800, 950, 25.0, 15.0, 12.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 12: Bình giữ nhiệt inox thể thao Lock&Lock 1000ml
    (12, 3, 31, 'Bình giữ nhiệt inox 304 quai xách thể thao dung tích lớn 1000ml', 'binh-giu-nhiet-inox-the-thao-locknlock-1000ml', 
     'Inox 304 giữ nhiệt nóng lạnh 24h, kèm lưới lọc trà và quai xách tiện lợi cho người tập gym, thể thao và đi phượt dã ngoại.',
     'Lock&Lock', 'https://images.unsplash.com/photo-1544816155-12df9643f363?w=500&auto=format&fit=crop&q=80',
     199000.00, 299000.00, 'ACTIVE', 4.80, 175, 4500, 19000, 420, 28.0, 9.0, 9.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- =============================================================================
-- 8. BẢNG PRODUCT_IMAGE (Hình ảnh chi tiết từng sản phẩm)
-- =============================================================================
INSERT INTO product_image (id, product_id, image_url, alt_text, sort_order, is_thumbnail)
VALUES
    -- Ảnh SP 1: Áo thun Coolmate
    (1, 1, 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=800', 'Áo thun nam trắng chính diện', 1, true),
    (2, 1, 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=800', 'Áo thun nam đen mặt sau', 2, false),
    (3, 1, 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=800', 'Chi tiết đường may cổ áo', 3, false),

    -- Ảnh SP 2: Tai nghe Sony
    (4, 2, 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800', 'Tai nghe Sony WH-1000XM5 góc nghiêng', 1, true),
    (5, 2, 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=800', 'Tai nghe Sony đeo trên tai', 2, false),

    -- Ảnh SP 3: Bình Lock&Lock
    (6, 3, 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800', 'Bình giữ nhiệt Lock&Lock nắp LED', 1, true),
    (7, 3, 'https://images.unsplash.com/photo-1577937927133-66ef06acdf18?w=800', 'Bình Lock&Lock trên bàn làm việc', 2, false),

    -- Ảnh SP 4: Bàn phím Logitech
    (8, 4, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800', 'Bàn phím cơ Bluetooth Logitech', 1, true),

    -- Ảnh SP 5: Củ sạc Anker
    (9, 5, 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=800', 'Củ sạc nhanh Anker GaN 65W', 1, true),

    -- Ảnh SP 6: Balo canvas
    (10, 6, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800', 'Balo canvas unisex', 1, true),

    -- Ảnh SP 7..12
    (11, 7, 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800', 'Bộ phễu pha cà phê gốm sứ', 1, true),
    (12, 8, 'https://images.unsplash.com/photo-1586953208448-b95a79798f07?w=800', 'Đế sạc không dây 3 trong 1', 1, true),
    (13, 9, 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=800', 'Chuột Silent Logitech', 1, true),
    (14, 10, 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800', 'Áo sơ mi Oxford Coolmate', 1, true),
    (15, 11, 'https://images.unsplash.com/photo-1545454675-3531b543be5d?w=800', 'Loa Bluetooth Vintage', 1, true),
    (16, 12, 'https://images.unsplash.com/photo-1544816155-12df9643f363?w=800', 'Bình nước thể thao 1000ml', 1, true);


-- =============================================================================
-- 9. BẢNG PRODUCT_OPTION (Tùy chọn thuộc tính: Màu sắc, Kích thước...)
-- =============================================================================
INSERT INTO product_option (id, product_id, name, sort_order)
VALUES
    -- SP 1: Áo thun Coolmate
    (1, 1, 'Màu sắc', 1),
    (2, 1, 'Kích cỡ', 2),

    -- SP 2: Tai nghe Sony
    (3, 2, 'Màu sắc', 1),

    -- SP 3: Bình Lock&Lock
    (4, 3, 'Màu sắc', 1),
    (5, 3, 'Dung tích', 2),

    -- SP 4: Bàn phím Logitech
    (6, 4, 'Màu sắc', 1);


-- =============================================================================
-- 10. BẢNG PRODUCT_OPTION_VALUE (Giá trị thuộc tính: Trắng, Đen, M, L, XL...)
-- =============================================================================
INSERT INTO product_option_value (id, option_id, "value", image_url, sort_order)
VALUES
    -- SP 1: Áo thun (Màu sắc)
    (101, 1, 'Trắng Tinh Khôi', 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=200', 1),
    (102, 1, 'Đen Huyền Bí',    'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=200', 2),
    (103, 1, 'Xanh Navy',       'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=200', 3),

    -- SP 1: Áo thun (Kích cỡ)
    (201, 2, 'M',   NULL, 1),
    (202, 2, 'L',   NULL, 2),
    (203, 2, 'XL',  NULL, 3),
    (204, 2, '2XL', NULL, 4),

    -- SP 2: Tai nghe Sony (Màu sắc)
    (301, 3, 'Đen Nhám (Matte Black)', NULL, 1),
    (302, 3, 'Bạc Ánh Kim (Silver)',   NULL, 2),

    -- SP 3: Bình Lock&Lock
    (401, 4, 'Inox Bạc', NULL, 1),
    (402, 4, 'Đen Nhám', NULL, 2),
    (501, 5, '500ml',    NULL, 1),
    (502, 5, '800ml',    NULL, 2),

    -- SP 4: Bàn phím Logitech
    (601, 6, 'Xám Đen (Graphite)', NULL, 1),
    (602, 6, 'Hồng Pastel (Rose)',  NULL, 2);


-- =============================================================================
-- 11. BẢNG PRODUCT_VARIANT (Biến thể SKU bán hàng thực tế)
-- =============================================================================
INSERT INTO product_variant (
    id, product_id, sku, variant_name, price, sale_price, 
    stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at
)
VALUES
    -- SP 1: Áo thun Coolmate
    (1, 1, 'CM-TSHIRT-WHT-M', 'Trắng Tinh Khôi / M', 299000.00, 199000.00, 100, 0, 1500, 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500', 200, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, 'CM-TSHIRT-WHT-L', 'Trắng Tinh Khôi / L', 299000.00, 199000.00, 85,  0, 2100, 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500', 200, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 1, 'CM-TSHIRT-BLK-M', 'Đen Huyền Bí / M',    299000.00, 199000.00, 90,  0, 1200, 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=500', 200, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 1, 'CM-TSHIRT-BLK-L', 'Đen Huyền Bí / L',    299000.00, 199000.00, 70,  0, 1000, 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=500', 200, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 2: Tai nghe Sony
    (5, 2, 'SNY-WH1000XM5-BLK', 'Đen Nhám (Matte Black)', 460000.00, 299000.00, 45, 0, 1450, 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500', 250, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 2, 'SNY-WH1000XM5-SLV', 'Bạc Ánh Kim (Silver)',   460000.00, 299000.00, 30, 0, 950,  'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=500', 250, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 3: Bình Lock&Lock
    (7, 3, 'LNL-BOTTLE-500ML', 'Inox Bạc / 500ml', 298000.00, 149000.00, 120, 0, 1100, 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500', 350, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 3, 'LNL-BOTTLE-800ML', 'Inox Bạc / 800ml', 348000.00, 189000.00, 80,  0, 750,  'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500', 420, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 4: Bàn phím Logitech
    (9, 4, 'LOGI-K380-GRY', 'Xám Đen (Graphite)', 739000.00, 590000.00, 60, 0, 620, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500', 850, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 4, 'LOGI-K380-ROSE','Hồng Pastel (Rose)', 739000.00, 590000.00, 40, 0, 300, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500', 850, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 5: Sạc Anker 65W
    (11, 5, 'ANK-GAN-65W', 'Anker GaNPrime 65W Đen', 550000.00, 399000.00, 150, 0, 3100, 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=500', 120, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 6: Balo canvas
    (12, 6, 'BALO-CANVAS-KOR', 'Balo Canvas Xám Đen', 350000.00, 245000.00, 95, 0, 1800, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500', 450, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 7: Phễu cà phê
    (13, 7, 'COFFEE-DRIP-SET', 'Bộ phễu gốm sứ mộc', 450000.00, 320000.00, 50, 0, 950, 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=500', 600, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 8: Đế sạc không dây Anker
    (14, 8, 'ANK-WIRELESS-3IN1', 'Đế sạc Anker 3 in 1 Trắng', 650000.00, 489000.00, 80, 0, 3400, 'https://images.unsplash.com/photo-1586953208448-b95a79798f07?w=500', 210, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 9: Chuột Logitech Silent
    (15, 9, 'LOGI-M220-SILENT', 'Chuột Silent Đen', 250000.00, 185000.00, 200, 0, 6800, 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500', 95, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 10: Áo sơ mi Oxford Coolmate
    (16, 10, 'CM-OXFORD-BLU-L', 'Áo Sơ Mi Xanh Nhạt / L', 399000.00, 279000.00, 110, 0, 5400, 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500', 280, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 11: Loa Bluetooth Vintage
    (17, 11, 'SPK-VINTAGE-BRN', 'Loa Bluetooth Nâu Gỗ', 599000.00, 429000.00, 75, 0, 1200, 'https://images.unsplash.com/photo-1545454675-3531b543be5d?w=500', 950, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SP 12: Bình thể thao Lock&Lock 1000ml
    (18, 12, 'LNL-SPORT-1000ML', 'Bình Thể Thao 1000ml Xanh Dương', 299000.00, 199000.00, 130, 0, 4500, 'https://images.unsplash.com/photo-1544816155-12df9643f363?w=500', 420, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- =============================================================================
-- 12. BẢNG VARIANT_OPTION_VALUE (Bảng liên kết giữa Variant & OptionValue)
-- =============================================================================
INSERT INTO variant_option_value (variant_id, option_value_id)
VALUES
    -- SP 1: Trắng / M
    (1, 101), (1, 201),
    -- SP 1: Trắng / L
    (2, 101), (2, 202),
    -- SP 1: Đen / M
    (3, 102), (3, 201),
    -- SP 1: Đen / L
    (4, 102), (4, 202),

    -- SP 2: Tai nghe Sony Đen
    (5, 301),
    -- SP 2: Tai nghe Sony Bạc
    (6, 302),

    -- SP 3: Bình Lock&Lock 500ml
    (7, 401), (7, 501),
    -- SP 3: Bình Lock&Lock 800ml
    (8, 401), (8, 502),

    -- SP 4: Bàn phím Logitech Xám
    (9, 601),
    -- SP 4: Bàn phím Logitech Hồng
    (10, 602);


-- =============================================================================
-- 13. BẢNG PROMOTION (Mã giảm giá & Khuyến mãi công khai)
-- =============================================================================
INSERT INTO promotion (
    id, seller_id, code, name, discount_type, discount_value, max_discount_amount,
    min_order_value, quantity, used_count, limit_per_user, apply_scope,
    start_at, end_at, status, created_at, updated_at
)
VALUES
    (1, NULL, 'MEGASALE50', 'MID-YEAR MEGA SALE 50% OFF', 'PERCENT', 50.00, 100000.00, 
     200000.00, 1000, 145, 1, 'ALL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '90 days', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (2, NULL, 'FREESHIP', 'Miễn phí vận chuyển toàn quốc cho đơn từ 150k', 'FREESHIP', 30000.00, 30000.00, 
     150000.00, 2000, 320, 2, 'ALL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '90 days', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (3, NULL, 'WELCOME10', 'Ưu đãi thành viên mới giảm 10%', 'PERCENT', 10.00, 50000.00, 
     100000.00, 5000, 890, 1, 'ALL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '90 days', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (4, 1, 'COOLMATE20', 'Voucher độc quyền Coolmate giảm ₫20.000', 'FIXED', 20000.00, 20000.00, 
     250000.00, 500, 80, 1, 'ALL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '60 days', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (5, 2, 'SONY50K', 'Voucher Sony Flagship giảm ₫50.000', 'FIXED', 50000.00, 50000.00, 
     500000.00, 300, 42, 1, 'ALL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '60 days', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

COMMIT;

SELECT 'SEED_CLEAN_DATA_SUCCESS' AS status, 
       (SELECT count(*) FROM users) AS total_users,
       (SELECT count(*) FROM seller) AS total_sellers,
       (SELECT count(*) FROM product) AS total_products,
       (SELECT count(*) FROM product_variant) AS total_variants,
       (SELECT count(*) FROM promotion) AS total_promotions;

