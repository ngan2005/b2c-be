-- =========================================================================
-- V39: Standardise categories to Vietnamese and seed rich e-commerce tree
-- =========================================================================

-- 1. Update existing core categories
UPDATE category SET name = 'Thiết Bị Điện Tử', slug = 'thiet-bi-dien-tu', icon_url = 'bi bi-laptop', updated_at = NOW() WHERE id = 1;
UPDATE category SET name = 'Thời Trang & May Mặc', slug = 'thoi-trang-may-mac', icon_url = 'bi bi-bag-heart', updated_at = NOW() WHERE id = 2;
UPDATE category SET name = 'Nhà Cửa & Đời Sống', slug = 'nha-cua-doi-song', icon_url = 'bi bi-house-door', updated_at = NOW() WHERE id = 3;

UPDATE category SET name = 'Điện Thoại & Phụ Kiện', slug = 'dien-thoai-phu-kien', icon_url = 'bi bi-phone', updated_at = NOW() WHERE id = 4;
UPDATE category SET name = 'Máy Tính & Laptop', slug = 'may-tinh-laptop', icon_url = 'bi bi-display', updated_at = NOW() WHERE id = 5;
UPDATE category SET name = 'Thời Trang Nam', slug = 'thoi-trang-nam', icon_url = 'bi bi-person', updated_at = NOW() WHERE id = 6;
UPDATE category SET name = 'Nội Thất & Trang Trí', slug = 'noi-that-trang-tri', icon_url = 'bi bi-lamp', updated_at = NOW() WHERE id = 7;

-- Fix corrupted test subcategory if exists
UPDATE category SET name = 'Thời Trang Nữ', slug = 'thoi-trang-nu', parent_id = 2, level = 2, icon_url = 'bi bi-person-heart', updated_at = NOW() WHERE id = 8;

-- Update QA category into Bách Hóa & Đồ Ăn
UPDATE category SET name = 'Bách Hóa & Đồ Ăn', slug = 'bach-hoa-do-an', is_active = true, icon_url = 'bi bi-cup-hot', updated_at = NOW() WHERE id = 9;

-- Ensure root categories exist (upsert by slug)
INSERT INTO category (name, slug, level, parent_id, is_active, sort_order, icon_url, created_at, updated_at)
VALUES
  ('Sắc Đẹp & Mỹ Phẩm', 'sac-dep-my-pham', 1, NULL, true, 4, 'bi bi-stars', NOW(), NOW())
ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name, icon_url = EXCLUDED.icon_url, is_active = true;

INSERT INTO category (name, slug, level, parent_id, is_active, sort_order, icon_url, created_at, updated_at)
VALUES
  ('Mẹ & Bé', 'me-va-be', 1, NULL, true, 5, 'bi bi-balloon-heart', NOW(), NOW())
ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name, icon_url = EXCLUDED.icon_url, is_active = true;

INSERT INTO category (name, slug, level, parent_id, is_active, sort_order, icon_url, created_at, updated_at)
VALUES
  ('Thể Thao & Dã Ngoại', 'the-thao-da-ngoai', 1, NULL, true, 6, 'bi bi-bicycle', NOW(), NOW())
ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name, icon_url = EXCLUDED.icon_url, is_active = true;

-- Subcategories for Bách Hóa & Đồ Ăn
INSERT INTO category (name, slug, level, parent_id, is_active, sort_order, icon_url, created_at, updated_at)
SELECT 'Đồ Ăn Vặt & Bánh Kẹo', 'do-an-vat-banh-keo', 2, id, true, 1, 'bi bi-cookie', NOW(), NOW()
FROM category WHERE slug = 'bach-hoa-do-an'
ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name, is_active = true;

INSERT INTO category (name, slug, level, parent_id, is_active, sort_order, icon_url, created_at, updated_at)
SELECT 'Thực Phẩm Đóng Hộp & Khô', 'thuc-pham-dong-hop-kho', 2, id, true, 2, 'bi bi-box-seam', NOW(), NOW()
FROM category WHERE slug = 'bach-hoa-do-an'
ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name, is_active = true;

INSERT INTO category (name, slug, level, parent_id, is_active, sort_order, icon_url, created_at, updated_at)
SELECT 'Đồ Uống & Giải Khát', 'do-uong-giai-khat', 2, id, true, 3, 'bi bi-cup-straw', NOW(), NOW()
FROM category WHERE slug = 'bach-hoa-do-an'
ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name, is_active = true;
