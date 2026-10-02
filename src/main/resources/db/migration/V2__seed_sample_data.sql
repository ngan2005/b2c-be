-- Sample seed data for development and demo purposes
-- This migration runs AFTER V1 to populate the database with sample data
--
-- BCrypt hash for "password123" (cost=10, verified):
--   $2a$10$cC5WBQWEpuZgMSmjFwV5fO/NKYtvoR6vHe8f8/yHGSFTmiEOanF72
-- (Generate a new one via /api/v1/__debug/bcrypt?password=password123)

-- ===========================================
-- 1. USERS (with role_id directly; password = "password123")
-- ===========================================
INSERT INTO users (email, password_hash, full_name, phone, status, role_id, email_verified_at, created_at, updated_at)
SELECT 'demo.buyer@example.com',
       '$2a$10$cC5WBQWEpuZgMSmjFwV5fO/NKYtvoR6vHe8f8/yHGSFTmiEOanF72',
       'Demo Buyer', '0901234567', 'ACTIVE', r.id, NOW(), NOW(), NOW()
FROM role r WHERE r.code = 'BUYER'
  AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'demo.buyer@example.com');

INSERT INTO users (email, password_hash, full_name, phone, status, role_id, email_verified_at, created_at, updated_at)
SELECT 'demo.seller@example.com',
       '$2a$10$cC5WBQWEpuZgMSmjFwV5fO/NKYtvoR6vHe8f8/yHGSFTmiEOanF72',
       'Demo Seller', '0909876543', 'ACTIVE', r.id, NOW(), NOW(), NOW()
FROM role r WHERE r.code = 'SELLER'
  AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'demo.seller@example.com');

-- Also patch any existing seed user rows that were created with the old
-- (mismatched) hash so that "password123" actually works.
UPDATE users
   SET password_hash = '$2a$10$cC5WBQWEpuZgMSmjFwV5fO/NKYtvoR6vHe8f8/yHGSFTmiEOanF72'
 WHERE email IN ('demo.buyer@example.com', 'demo.seller@example.com')
   AND password_hash <> '$2a$10$cC5WBQWEpuZgMSmjFwV5fO/NKYtvoR6vHe8f8/yHGSFTmiEOanF72';

-- ===========================================
-- 2. SELLER PROFILE
-- ===========================================
INSERT INTO seller (user_id, shop_name, slug, description, business_type, status, rating_avg, rating_count, total_product, created_at, updated_at)
SELECT id, 'Demo Shop', 'demo-shop', 'Welcome to our demo shop!', 'INDIVIDUAL', 'ACTIVE', 4.50, 100, 0, NOW(), NOW()
FROM users WHERE email = 'demo.seller@example.com'
  AND NOT EXISTS (SELECT 1 FROM seller WHERE user_id = (SELECT id FROM users WHERE email = 'demo.seller@example.com'));

-- ===========================================
-- 3. CATEGORIES (hierarchical)
-- ===========================================
-- Use standard SQL MERGE so the script works on PostgreSQL 15+ and H2
-- (PostgreSQL mode). Earlier ON CONFLICT clause was PostgreSQL-only.
MERGE INTO category AS t
USING (VALUES
    ('Electronics', 'electronics', 1, NULL, true, 1),
    ('Fashion',     'fashion',     1, NULL, true, 2),
    ('Home & Living','home-living',1, NULL, true, 3)
) AS s(name, slug, level, parent_id, is_active, sort_order)
ON t.slug = s.slug
WHEN NOT MATCHED THEN
    INSERT (name, slug, level, parent_id, is_active, sort_order, created_at, updated_at)
    VALUES (s.name, s.slug, s.level, s.parent_id, s.is_active, s.sort_order, NOW(), NOW());

-- Subcategories
INSERT INTO category (name, slug, level, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Smartphones', 'smartphones', 2, id, true, 1, NOW(), NOW()
FROM category WHERE slug = 'electronics'
  AND NOT EXISTS (SELECT 1 FROM category WHERE slug = 'smartphones');

INSERT INTO category (name, slug, level, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Laptops', 'laptops', 2, id, true, 2, NOW(), NOW()
FROM category WHERE slug = 'electronics'
  AND NOT EXISTS (SELECT 1 FROM category WHERE slug = 'laptops');

INSERT INTO category (name, slug, level, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Mens Clothing', 'mens-clothing', 2, id, true, 1, NOW(), NOW()
FROM category WHERE slug = 'fashion'
  AND NOT EXISTS (SELECT 1 FROM category WHERE slug = 'mens-clothing');

INSERT INTO category (name, slug, level, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Furniture', 'furniture', 2, id, true, 1, NOW(), NOW()
FROM category WHERE slug = 'home-living'
  AND NOT EXISTS (SELECT 1 FROM category WHERE slug = 'furniture');

-- ===========================================
-- 4. SAMPLE PRODUCTS
-- ===========================================
INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'iphone-15-pro-256gb-titanium',
  'iPhone 15 Pro 256GB Titanium',
  'The latest Apple iPhone 15 Pro with A17 Pro chip, titanium design, and advanced camera system. 256GB storage.',
  'Apple',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  25000000, 30000000, 187,
  4.80, 120, 500, 10000,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'iphone-15-pro-256gb-titanium');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'macbook-pro-m3-14-inch',
  'MacBook Pro 14 inch M3 8GB 512GB',
  'Apple MacBook Pro 14 inch with M3 chip, Liquid Retina XDR display, 8GB unified memory, 512GB SSD.',
  'Apple',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  45000000, 55000000, 1600,
  4.90, 80, 200, 5000,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'macbook-pro-m3-14-inch');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'samsung-galaxy-s24-ultra',
  'Samsung Galaxy S24 Ultra 512GB',
  'Samsung Galaxy S24 Ultra with S Pen, 200MP camera, Snapdragon 8 Gen 3, 512GB storage.',
  'Samsung',
  'https://cdn.tgdd.vn/Products/Images/42/320730/samsung-galaxy-s24-ultra-1.jpg',
  'ACTIVE',
  28000000, 32000000, 232,
  4.70, 95, 300, 7500,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'samsung-galaxy-s24-ultra');
