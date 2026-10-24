-- ===========================================
-- V20: Seed 100 demo products for the demo-shop seller
-- Categories: smartphones (30), laptops (25), mens-clothing (25), furniture (20)
-- All INSERTs are idempotent via NOT EXISTS on slug/sku
-- ===========================================

-- 1) PRODUCTS
INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-001-apple',
  'Apple Smartphone Model 001',
  'Premium smartphone from Apple. Demo product #001 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Apple',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  3350000, 3350000, 176,
  4.08, 351, 1143, 49408,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-001-apple');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-002-nokia',
  'Nokia Smartphone Model 002',
  'Premium smartphone from Nokia. Demo product #002 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Nokia',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  5400000, 5400000, 209,
  3.84, 263, 767, 15095,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-002-nokia');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-003-motorola',
  'Motorola Smartphone Model 003',
  'Premium smartphone from Motorola. Demo product #003 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Motorola',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  3400000, 3400000, 206,
  4.62, 234, 4464, 31957,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-003-motorola');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-004-motorola',
  'Motorola Smartphone Model 004',
  'Premium smartphone from Motorola. Demo product #004 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Motorola',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  11850000, 11850000, 258,
  3.81, 179, 1307, 47060,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-004-motorola');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-005-vivo',
  'Vivo Smartphone Model 005',
  'Premium smartphone from Vivo. Demo product #005 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Vivo',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  7700000, 7700000, 247,
  4.68, 54, 837, 6915,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-005-vivo');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-006-realme',
  'Realme Smartphone Model 006',
  'Premium smartphone from Realme. Demo product #006 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Realme',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  30950000, 30950000, 267,
  4.1, 279, 355, 48178,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-006-realme');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-007-apple',
  'Apple Smartphone Model 007',
  'Premium smartphone from Apple. Demo product #007 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Apple',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  33450000, 33450000, 199,
  4.43, 365, 2962, 40799,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-007-apple');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-008-apple',
  'Apple Smartphone Model 008',
  'Premium smartphone from Apple. Demo product #008 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Apple',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  4050000, 4050000, 371,
  4.69, 56, 653, 15909,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-008-apple');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-009-oneplus',
  'OnePlus Smartphone Model 009',
  'Premium smartphone from OnePlus. Demo product #009 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'OnePlus',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  11850000, 11850000, 240,
  4.76, 112, 1332, 25592,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-009-oneplus');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-010-vivo',
  'Vivo Smartphone Model 010',
  'Premium smartphone from Vivo. Demo product #010 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Vivo',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  26050000, 26050000, 193,
  4.55, 278, 4990, 46603,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-010-vivo');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-011-oppo',
  'Oppo Smartphone Model 011',
  'Premium smartphone from Oppo. Demo product #011 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Oppo',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  8000000, 8000000, 325,
  4.11, 171, 4562, 18954,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-011-oppo');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-012-samsung',
  'Samsung Smartphone Model 012',
  'Premium smartphone from Samsung. Demo product #012 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Samsung',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  10200000, 10200000, 166,
  4.73, 113, 3286, 20832,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-012-samsung');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-013-motorola',
  'Motorola Smartphone Model 013',
  'Premium smartphone from Motorola. Demo product #013 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Motorola',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  31900000, 31900000, 376,
  4.04, 473, 4089, 30017,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-013-motorola');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-014-honor',
  'Honor Smartphone Model 014',
  'Premium smartphone from Honor. Demo product #014 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Honor',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  7300000, 7300000, 217,
  4.08, 387, 4598, 39920,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-014-honor');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-015-motorola',
  'Motorola Smartphone Model 015',
  'Premium smartphone from Motorola. Demo product #015 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Motorola',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  16900000, 16900000, 280,
  4.26, 257, 1796, 10861,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-015-motorola');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-016-apple',
  'Apple Smartphone Model 016',
  'Premium smartphone from Apple. Demo product #016 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Apple',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  27850000, 27850000, 258,
  3.93, 310, 1310, 45906,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-016-apple');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-017-apple',
  'Apple Smartphone Model 017',
  'Premium smartphone from Apple. Demo product #017 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Apple',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  15400000, 15400000, 398,
  4.95, 288, 4334, 20810,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-017-apple');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-018-samsung',
  'Samsung Smartphone Model 018',
  'Premium smartphone from Samsung. Demo product #018 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Samsung',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  25350000, 25350000, 218,
  4.58, 398, 4398, 53607,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-018-samsung');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-019-realme',
  'Realme Smartphone Model 019',
  'Premium smartphone from Realme. Demo product #019 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Realme',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  6250000, 6250000, 374,
  3.98, 373, 26, 47349,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-019-realme');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-020-vivo',
  'Vivo Smartphone Model 020',
  'Premium smartphone from Vivo. Demo product #020 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Vivo',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  19300000, 19300000, 226,
  4.38, 435, 871, 41850,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-020-vivo');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-021-nokia',
  'Nokia Smartphone Model 021',
  'Premium smartphone from Nokia. Demo product #021 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Nokia',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  22950000, 22950000, 394,
  4.23, 403, 1323, 36671,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-021-nokia');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-022-nokia',
  'Nokia Smartphone Model 022',
  'Premium smartphone from Nokia. Demo product #022 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Nokia',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  33300000, 33300000, 387,
  4.17, 190, 159, 7490,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-022-nokia');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-023-vivo',
  'Vivo Smartphone Model 023',
  'Premium smartphone from Vivo. Demo product #023 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Vivo',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  10550000, 10550000, 337,
  4.81, 253, 645, 6258,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-023-vivo');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-024-apple',
  'Apple Smartphone Model 024',
  'Premium smartphone from Apple. Demo product #024 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Apple',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  28000000, 28000000, 192,
  3.94, 140, 3893, 39924,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-024-apple');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-025-nokia',
  'Nokia Smartphone Model 025',
  'Premium smartphone from Nokia. Demo product #025 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Nokia',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  31750000, 31750000, 336,
  4.91, 358, 4417, 53914,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-025-nokia');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-026-oppo',
  'Oppo Smartphone Model 026',
  'Premium smartphone from Oppo. Demo product #026 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Oppo',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  26400000, 26400000, 380,
  4.94, 269, 3059, 31770,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-026-oppo');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-027-honor',
  'Honor Smartphone Model 027',
  'Premium smartphone from Honor. Demo product #027 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Honor',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  6550000, 6550000, 291,
  3.87, 122, 172, 38727,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-027-honor');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-028-motorola',
  'Motorola Smartphone Model 028',
  'Premium smartphone from Motorola. Demo product #028 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Motorola',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  9900000, 9900000, 167,
  4.61, 468, 482, 15485,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-028-motorola');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-029-samsung',
  'Samsung Smartphone Model 029',
  'Premium smartphone from Samsung. Demo product #029 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Samsung',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  31350000, 31350000, 274,
  4.39, 114, 2281, 46123,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-029-samsung');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-smartphone-030-nokia',
  'Nokia Smartphone Model 030',
  'Premium smartphone from Nokia. Demo product #030 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Nokia',
  'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg',
  'ACTIVE',
  6950000, 6950000, 212,
  4.81, 406, 4720, 35696,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'smartphones'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-smartphone-030-nokia');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-031-razer',
  'Razer Laptop Model 031',
  'Premium laptop from Razer. Demo product #031 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Razer',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  69200000, 69200000, 1858,
  3.91, 215, 3531, 26750,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-031-razer');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-032-razer',
  'Razer Laptop Model 032',
  'Premium laptop from Razer. Demo product #032 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Razer',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  72950000, 72950000, 1853,
  4.57, 377, 806, 4778,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-032-razer');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-033-acer',
  'Acer Laptop Model 033',
  'Premium laptop from Acer. Demo product #033 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Acer',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  68750000, 68750000, 1864,
  4.09, 76, 1558, 36704,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-033-acer');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-034-msi',
  'MSI Laptop Model 034',
  'Premium laptop from MSI. Demo product #034 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'MSI',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  27300000, 27300000, 1956,
  4.09, 446, 617, 29658,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-034-msi');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-035-microsoft',
  'Microsoft Laptop Model 035',
  'Premium laptop from Microsoft. Demo product #035 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Microsoft',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  21550000, 21550000, 1987,
  4.95, 390, 120, 6232,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-035-microsoft');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-036-asus',
  'Asus Laptop Model 036',
  'Premium laptop from Asus. Demo product #036 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Asus',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  26150000, 26150000, 1792,
  4.35, 199, 3285, 7127,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-036-asus');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-037-apple',
  'Apple Laptop Model 037',
  'Premium laptop from Apple. Demo product #037 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Apple',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  41200000, 41200000, 1858,
  4.7, 361, 3727, 22421,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-037-apple');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-038-microsoft',
  'Microsoft Laptop Model 038',
  'Premium laptop from Microsoft. Demo product #038 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Microsoft',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  59400000, 59400000, 1997,
  3.98, 34, 2430, 16697,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-038-microsoft');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-039-lg',
  'LG Laptop Model 039',
  'Premium laptop from LG. Demo product #039 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'LG',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  64350000, 64350000, 1899,
  4.66, 249, 468, 3754,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-039-lg');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-040-microsoft',
  'Microsoft Laptop Model 040',
  'Premium laptop from Microsoft. Demo product #040 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Microsoft',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  76700000, 76700000, 1967,
  3.98, 100, 4160, 9410,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-040-microsoft');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-041-dell',
  'Dell Laptop Model 041',
  'Premium laptop from Dell. Demo product #041 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Dell',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  54950000, 54950000, 1991,
  4.79, 460, 3307, 11163,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-041-dell');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-042-lg',
  'LG Laptop Model 042',
  'Premium laptop from LG. Demo product #042 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'LG',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  31500000, 31500000, 1918,
  3.85, 303, 671, 28145,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-042-lg');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-043-lg',
  'LG Laptop Model 043',
  'Premium laptop from LG. Demo product #043 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'LG',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  50100000, 50100000, 1817,
  4.1, 207, 2573, 18215,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-043-lg');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-044-hp',
  'HP Laptop Model 044',
  'Premium laptop from HP. Demo product #044 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'HP',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  60100000, 60100000, 1867,
  4.33, 323, 594, 1204,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-044-hp');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-045-lg',
  'LG Laptop Model 045',
  'Premium laptop from LG. Demo product #045 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'LG',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  21700000, 21700000, 1988,
  4.05, 183, 2172, 10852,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-045-lg');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-046-dell',
  'Dell Laptop Model 046',
  'Premium laptop from Dell. Demo product #046 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Dell',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  74000000, 74000000, 1930,
  4.13, 159, 3589, 39189,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-046-dell');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-047-lg',
  'LG Laptop Model 047',
  'Premium laptop from LG. Demo product #047 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'LG',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  69150000, 69150000, 1988,
  3.81, 344, 4543, 24163,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-047-lg');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-048-dell',
  'Dell Laptop Model 048',
  'Premium laptop from Dell. Demo product #048 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Dell',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  78000000, 78000000, 1891,
  4.1, 84, 876, 49531,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-048-dell');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-049-lenovo',
  'Lenovo Laptop Model 049',
  'Premium laptop from Lenovo. Demo product #049 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Lenovo',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  33900000, 33900000, 1912,
  4.63, 441, 1667, 46722,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-049-lenovo');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-050-lenovo',
  'Lenovo Laptop Model 050',
  'Premium laptop from Lenovo. Demo product #050 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Lenovo',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  48900000, 48900000, 1912,
  4.84, 221, 416, 6464,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-050-lenovo');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-051-lenovo',
  'Lenovo Laptop Model 051',
  'Premium laptop from Lenovo. Demo product #051 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Lenovo',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  17950000, 17950000, 1939,
  4.69, 231, 2145, 12734,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-051-lenovo');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-052-microsoft',
  'Microsoft Laptop Model 052',
  'Premium laptop from Microsoft. Demo product #052 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Microsoft',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  62350000, 62350000, 1981,
  3.81, 81, 616, 45902,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-052-microsoft');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-053-microsoft',
  'Microsoft Laptop Model 053',
  'Premium laptop from Microsoft. Demo product #053 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Microsoft',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  17400000, 17400000, 1782,
  4.47, 26, 1213, 29379,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-053-microsoft');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-054-lenovo',
  'Lenovo Laptop Model 054',
  'Premium laptop from Lenovo. Demo product #054 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Lenovo',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  39450000, 39450000, 1803,
  4.72, 354, 326, 23775,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-054-lenovo');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-laptop-055-asus',
  'Asus Laptop Model 055',
  'Premium laptop from Asus. Demo product #055 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Asus',
  'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg',
  'ACTIVE',
  59750000, 59750000, 1941,
  4.7, 84, 3329, 44004,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'laptops'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-laptop-055-asus');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-056-levis',
  'Levi''s Men''s Apparel Model 056',
  'Premium men''s apparel from Levi''s. Demo product #056 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Levi''s',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  1500000, 1500000, 495,
  4.73, 382, 3377, 5001,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-056-levis');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-057-zara',
  'Zara Men''s Apparel Model 057',
  'Premium men''s apparel from Zara. Demo product #057 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Zara',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  3600000, 3600000, 490,
  4.79, 408, 2032, 19517,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-057-zara');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-058-adidas',
  'Adidas Men''s Apparel Model 058',
  'Premium men''s apparel from Adidas. Demo product #058 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Adidas',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  3350000, 3350000, 659,
  4.79, 475, 1822, 14901,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-058-adidas');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-059-underarmour',
  'Under Armour Men''s Apparel Model 059',
  'Premium men''s apparel from Under Armour. Demo product #059 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Under Armour',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  3100000, 3100000, 456,
  4.71, 342, 1864, 16473,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-059-underarmour');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-060-levis',
  'Levi''s Men''s Apparel Model 060',
  'Premium men''s apparel from Levi''s. Demo product #060 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Levi''s',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  3500000, 3500000, 614,
  4.79, 265, 2286, 25298,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-060-levis');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-061-puma',
  'Puma Men''s Apparel Model 061',
  'Premium men''s apparel from Puma. Demo product #061 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Puma',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  2950000, 2950000, 598,
  3.93, 497, 2139, 13841,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-061-puma');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-062-h&m',
  'H&M Men''s Apparel Model 062',
  'Premium men''s apparel from H&M. Demo product #062 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'H&M',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  450000, 450000, 605,
  4.3, 266, 2569, 31168,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-062-h&m');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-063-adidas',
  'Adidas Men''s Apparel Model 063',
  'Premium men''s apparel from Adidas. Demo product #063 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Adidas',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  3400000, 3400000, 561,
  4.02, 5, 363, 46813,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-063-adidas');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-064-lacoste',
  'Lacoste Men''s Apparel Model 064',
  'Premium men''s apparel from Lacoste. Demo product #064 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Lacoste',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  1800000, 1800000, 530,
  3.88, 344, 2704, 43543,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-064-lacoste');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-065-adidas',
  'Adidas Men''s Apparel Model 065',
  'Premium men''s apparel from Adidas. Demo product #065 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Adidas',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  2650000, 2650000, 628,
  4.57, 156, 2672, 29043,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-065-adidas');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-066-lacoste',
  'Lacoste Men''s Apparel Model 066',
  'Premium men''s apparel from Lacoste. Demo product #066 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Lacoste',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  1200000, 1200000, 641,
  4.56, 467, 3105, 47493,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-066-lacoste');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-067-uniqlo',
  'Uniqlo Men''s Apparel Model 067',
  'Premium men''s apparel from Uniqlo. Demo product #067 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Uniqlo',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  2650000, 2650000, 503,
  4.76, 225, 2489, 21292,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-067-uniqlo');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-068-calvinklein',
  'Calvin Klein Men''s Apparel Model 068',
  'Premium men''s apparel from Calvin Klein. Demo product #068 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Calvin Klein',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  2850000, 2850000, 571,
  4.31, 411, 1750, 35250,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-068-calvinklein');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-069-uniqlo',
  'Uniqlo Men''s Apparel Model 069',
  'Premium men''s apparel from Uniqlo. Demo product #069 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Uniqlo',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  850000, 850000, 659,
  4.56, 492, 2745, 8865,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-069-uniqlo');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-070-levis',
  'Levi''s Men''s Apparel Model 070',
  'Premium men''s apparel from Levi''s. Demo product #070 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Levi''s',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  2750000, 2750000, 512,
  4.03, 248, 200, 3228,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-070-levis');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-071-calvinklein',
  'Calvin Klein Men''s Apparel Model 071',
  'Premium men''s apparel from Calvin Klein. Demo product #071 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Calvin Klein',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  750000, 750000, 633,
  4.82, 361, 4715, 17457,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-071-calvinklein');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-072-puma',
  'Puma Men''s Apparel Model 072',
  'Premium men''s apparel from Puma. Demo product #072 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Puma',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  4300000, 4300000, 670,
  3.97, 399, 45, 49260,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-072-puma');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-073-adidas',
  'Adidas Men''s Apparel Model 073',
  'Premium men''s apparel from Adidas. Demo product #073 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Adidas',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  3700000, 3700000, 462,
  4.72, 290, 4243, 34687,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-073-adidas');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-074-levis',
  'Levi''s Men''s Apparel Model 074',
  'Premium men''s apparel from Levi''s. Demo product #074 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Levi''s',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  1150000, 1150000, 602,
  4.72, 167, 4351, 40980,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-074-levis');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-075-underarmour',
  'Under Armour Men''s Apparel Model 075',
  'Premium men''s apparel from Under Armour. Demo product #075 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Under Armour',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  4400000, 4400000, 640,
  4.84, 446, 3652, 14082,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-075-underarmour');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-076-underarmour',
  'Under Armour Men''s Apparel Model 076',
  'Premium men''s apparel from Under Armour. Demo product #076 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Under Armour',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  3950000, 3950000, 574,
  4.08, 325, 2271, 36434,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-076-underarmour');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-077-levis',
  'Levi''s Men''s Apparel Model 077',
  'Premium men''s apparel from Levi''s. Demo product #077 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Levi''s',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  2450000, 2450000, 535,
  4.62, 168, 1920, 19727,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-077-levis');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-078-lacoste',
  'Lacoste Men''s Apparel Model 078',
  'Premium men''s apparel from Lacoste. Demo product #078 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Lacoste',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  850000, 850000, 504,
  4.07, 37, 1251, 47546,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-078-lacoste');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-079-puma',
  'Puma Men''s Apparel Model 079',
  'Premium men''s apparel from Puma. Demo product #079 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Puma',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  3550000, 3550000, 663,
  4.34, 220, 510, 14065,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-079-puma');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-mens-080-puma',
  'Puma Men''s Apparel Model 080',
  'Premium men''s apparel from Puma. Demo product #080 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Puma',
  'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg',
  'ACTIVE',
  300000, 300000, 451,
  4.68, 487, 3116, 34375,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'mens-clothing'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-mens-080-puma');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-081-artemide',
  'Artemide Furniture Model 081',
  'Premium furniture from Artemide. Demo product #081 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Artemide',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  10500000, 10500000, 8141,
  4.78, 381, 3432, 38704,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-081-artemide');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-082-flexa',
  'Flexa Furniture Model 082',
  'Premium furniture from Flexa. Demo product #082 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Flexa',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  20750000, 20750000, 8074,
  4.36, 19, 2235, 30797,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-082-flexa');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-083-boconcept',
  'BoConcept Furniture Model 083',
  'Premium furniture from BoConcept. Demo product #083 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'BoConcept',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  11800000, 11800000, 8185,
  4.72, 70, 1352, 31982,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-083-boconcept');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-084-fursys',
  'Fursys Furniture Model 084',
  'Premium furniture from Fursys. Demo product #084 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Fursys',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  18400000, 18400000, 7956,
  4.25, 47, 4623, 48073,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-084-fursys');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-085-boconcept',
  'BoConcept Furniture Model 085',
  'Premium furniture from BoConcept. Demo product #085 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'BoConcept',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  5050000, 5050000, 8033,
  4.01, 113, 2131, 26975,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-085-boconcept');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-086-westelm',
  'West Elm Furniture Model 086',
  'Premium furniture from West Elm. Demo product #086 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'West Elm',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  11450000, 11450000, 8193,
  4.81, 430, 2279, 51562,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-086-westelm');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-087-boconcept',
  'BoConcept Furniture Model 087',
  'Premium furniture from BoConcept. Demo product #087 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'BoConcept',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  8950000, 8950000, 8193,
  4.34, 184, 4418, 7831,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-087-boconcept');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-088-ergohuman',
  'Ergohuman Furniture Model 088',
  'Premium furniture from Ergohuman. Demo product #088 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Ergohuman',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  22300000, 22300000, 7957,
  4.9, 491, 329, 49758,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-088-ergohuman');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-089-ergohuman',
  'Ergohuman Furniture Model 089',
  'Premium furniture from Ergohuman. Demo product #089 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Ergohuman',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  7200000, 7200000, 8071,
  4.51, 347, 1954, 10226,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-089-ergohuman');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-090-vinyl',
  'Vinyl Furniture Model 090',
  'Premium furniture from Vinyl. Demo product #090 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Vinyl',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  19400000, 19400000, 7992,
  4.33, 315, 2099, 26274,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-090-vinyl');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-091-fursys',
  'Fursys Furniture Model 091',
  'Premium furniture from Fursys. Demo product #091 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Fursys',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  24600000, 24600000, 8098,
  4.74, 18, 2548, 9632,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-091-fursys');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-092-songmics',
  'Songmics Furniture Model 092',
  'Premium furniture from Songmics. Demo product #092 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Songmics',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  19800000, 19800000, 8000,
  4.9, 43, 3249, 50108,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-092-songmics');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-093-fursys',
  'Fursys Furniture Model 093',
  'Premium furniture from Fursys. Demo product #093 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Fursys',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  23650000, 23650000, 8103,
  4.08, 417, 2470, 47313,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-093-fursys');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-094-vinyl',
  'Vinyl Furniture Model 094',
  'Premium furniture from Vinyl. Demo product #094 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Vinyl',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  19500000, 19500000, 8044,
  4.2, 40, 3509, 46862,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-094-vinyl');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-095-flexa',
  'Flexa Furniture Model 095',
  'Premium furniture from Flexa. Demo product #095 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Flexa',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  22250000, 22250000, 8060,
  4.78, 496, 4015, 10931,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-095-flexa');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-096-artemide',
  'Artemide Furniture Model 096',
  'Premium furniture from Artemide. Demo product #096 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Artemide',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  21850000, 21850000, 7995,
  4.33, 380, 1253, 29793,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-096-artemide');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-097-flexa',
  'Flexa Furniture Model 097',
  'Premium furniture from Flexa. Demo product #097 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Flexa',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  22350000, 22350000, 8069,
  4.73, 228, 4408, 36094,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-097-flexa');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-098-fursys',
  'Fursys Furniture Model 098',
  'Premium furniture from Fursys. Demo product #098 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Fursys',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  9500000, 9500000, 8175,
  4.08, 235, 709, 18988,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-098-fursys');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-099-ergohuman',
  'Ergohuman Furniture Model 099',
  'Premium furniture from Ergohuman. Demo product #099 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Ergohuman',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  16100000, 16100000, 8076,
  4.57, 440, 2755, 4635,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-099-ergohuman');

INSERT INTO product (seller_id, category_id, slug, name, description, brand, thumbnail_url, status,
                     min_price, max_price, weight_gram,
                     rating_avg, rating_count, sold_count, view_count, created_at, updated_at)
SELECT
  s.id,
  c.id,
  'demo-furn-100-artemide',
  'Artemide Furniture Model 100',
  'Premium furniture from Artemide. Demo product #100 created for store-front development. Features high-quality materials, modern design and reliable performance. Backed by a 12-month manufacturer warranty.',
  'Artemide',
  'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg',
  'ACTIVE',
  6600000, 6600000, 8021,
  4.21, 455, 2116, 24420,
  NOW(), NOW()
FROM seller s, category c
WHERE s.slug = 'demo-shop' AND c.slug = 'furniture'
  AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'demo-furn-100-artemide');


-- 2) VARIANTS (1 per product, 1-3 days stock each)
INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100001',
  'Default',
  3350000, NULL, 151, 0, 1143, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 176, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-001-apple'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100001');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100002',
  'Default',
  5400000, NULL, 294, 0, 767, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 209, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-002-nokia'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100002');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100003',
  'Default',
  3400000, NULL, 15, 0, 4464, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 206, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-003-motorola'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100003');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100004',
  'Default',
  11850000, NULL, 274, 0, 1307, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 258, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-004-motorola'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100004');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100005',
  'Default',
  7700000, 7700000, 107, 0, 837, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 247, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-005-vivo'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100005');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100006',
  'Default',
  30950000, NULL, 53, 0, 355, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 267, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-006-realme'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100006');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100007',
  'Default',
  33450000, NULL, 133, 0, 2962, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 199, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-007-apple'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100007');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100008',
  'Default',
  4050000, NULL, 218, 0, 653, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 371, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-008-apple'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100008');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100009',
  'Default',
  11850000, NULL, 260, 0, 1332, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 240, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-009-oneplus'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100009');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100010',
  'Default',
  26050000, NULL, 294, 0, 4990, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 193, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-010-vivo'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100010');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100011',
  'Default',
  8000000, NULL, 133, 0, 4562, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 325, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-011-oppo'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100011');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100012',
  'Default',
  10200000, NULL, 253, 0, 3286, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 166, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-012-samsung'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100012');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100013',
  'Default',
  31900000, NULL, 261, 0, 4089, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 376, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-013-motorola'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100013');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100014',
  'Default',
  7300000, 7300000, 239, 0, 4598, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 217, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-014-honor'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100014');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100015',
  'Default',
  16900000, NULL, 18, 0, 1796, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 280, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-015-motorola'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100015');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100016',
  'Default',
  27850000, 27850000, 57, 0, 1310, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 258, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-016-apple'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100016');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100017',
  'Default',
  15400000, NULL, 160, 0, 4334, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 398, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-017-apple'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100017');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100018',
  'Default',
  25350000, NULL, 123, 0, 4398, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 218, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-018-samsung'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100018');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100019',
  'Default',
  6250000, 6250000, 217, 0, 26, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 374, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-019-realme'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100019');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100020',
  'Default',
  19300000, NULL, 134, 0, 871, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 226, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-020-vivo'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100020');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100021',
  'Default',
  22950000, 22950000, 166, 0, 1323, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 394, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-021-nokia'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100021');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100022',
  'Default',
  33300000, 33300000, 198, 0, 159, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 387, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-022-nokia'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100022');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100023',
  'Default',
  10550000, 10550000, 252, 0, 645, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 337, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-023-vivo'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100023');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100024',
  'Default',
  28000000, NULL, 293, 0, 3893, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 192, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-024-apple'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100024');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100025',
  'Default',
  31750000, NULL, 281, 0, 4417, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 336, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-025-nokia'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100025');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100026',
  'Default',
  26400000, NULL, 186, 0, 3059, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 380, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-026-oppo'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100026');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100027',
  'Default',
  6550000, 6550000, 227, 0, 172, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 291, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-027-honor'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100027');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100028',
  'Default',
  9900000, 9900000, 291, 0, 482, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 167, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-028-motorola'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100028');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100029',
  'Default',
  31350000, NULL, 179, 0, 2281, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 274, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-029-samsung'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100029');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100030',
  'Default',
  6950000, NULL, 190, 0, 4720, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 212, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-030-nokia'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100030');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100031',
  'Default',
  69200000, NULL, 242, 0, 3531, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1858, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-031-razer'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100031');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100032',
  'Default',
  72950000, NULL, 148, 0, 806, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1853, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-032-razer'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100032');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100033',
  'Default',
  68750000, NULL, 166, 0, 1558, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1864, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-033-acer'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100033');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100034',
  'Default',
  27300000, 27300000, 138, 0, 617, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1956, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-034-msi'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100034');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100035',
  'Default',
  21550000, 21550000, 128, 0, 120, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1987, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-035-microsoft'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100035');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100036',
  'Default',
  26150000, NULL, 71, 0, 3285, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1792, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-036-asus'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100036');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100037',
  'Default',
  41200000, 41200000, 108, 0, 3727, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1858, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-037-apple'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100037');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100038',
  'Default',
  59400000, NULL, 171, 0, 2430, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1997, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-038-microsoft'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100038');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100039',
  'Default',
  64350000, NULL, 71, 0, 468, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1899, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-039-lg'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100039');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100040',
  'Default',
  76700000, NULL, 284, 0, 4160, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1967, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-040-microsoft'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100040');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100041',
  'Default',
  54950000, 54950000, 104, 0, 3307, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1991, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-041-dell'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100041');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100042',
  'Default',
  31500000, NULL, 108, 0, 671, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1918, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-042-lg'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100042');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100043',
  'Default',
  50100000, NULL, 120, 0, 2573, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1817, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-043-lg'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100043');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100044',
  'Default',
  60100000, NULL, 257, 0, 594, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1867, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-044-hp'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100044');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100045',
  'Default',
  21700000, 21700000, 151, 0, 2172, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1988, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-045-lg'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100045');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100046',
  'Default',
  74000000, 74000000, 278, 0, 3589, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1930, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-046-dell'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100046');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100047',
  'Default',
  69150000, NULL, 154, 0, 4543, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1988, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-047-lg'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100047');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100048',
  'Default',
  78000000, NULL, 61, 0, 876, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1891, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-048-dell'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100048');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100049',
  'Default',
  33900000, NULL, 109, 0, 1667, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1912, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-049-lenovo'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100049');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100050',
  'Default',
  48900000, NULL, 161, 0, 416, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1912, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-050-lenovo'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100050');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100051',
  'Default',
  17950000, 17950000, 126, 0, 2145, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1939, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-051-lenovo'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100051');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100052',
  'Default',
  62350000, NULL, 194, 0, 616, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1981, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-052-microsoft'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100052');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100053',
  'Default',
  17400000, NULL, 101, 0, 1213, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1782, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-053-microsoft'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100053');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100054',
  'Default',
  39450000, NULL, 164, 0, 326, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1803, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-054-lenovo'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100054');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100055',
  'Default',
  59750000, 59750000, 17, 0, 3329, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 1941, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-055-asus'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100055');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100056',
  'Default',
  1500000, NULL, 283, 0, 3377, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 495, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-056-levis'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100056');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100057',
  'Default',
  3600000, NULL, 74, 0, 2032, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 490, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-057-zara'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100057');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100058',
  'Default',
  3350000, NULL, 150, 0, 1822, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 659, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-058-adidas'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100058');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100059',
  'Default',
  3100000, NULL, 33, 0, 1864, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 456, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-059-underarmour'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100059');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100060',
  'Default',
  3500000, NULL, 37, 0, 2286, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 614, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-060-levis'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100060');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100061',
  'Default',
  2950000, NULL, 293, 0, 2139, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 598, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-061-puma'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100061');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100062',
  'Default',
  450000, 450000, 159, 0, 2569, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 605, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-062-h&m'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100062');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100063',
  'Default',
  3400000, NULL, 74, 0, 363, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 561, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-063-adidas'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100063');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100064',
  'Default',
  1800000, NULL, 261, 0, 2704, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 530, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-064-lacoste'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100064');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100065',
  'Default',
  2650000, NULL, 62, 0, 2672, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 628, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-065-adidas'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100065');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100066',
  'Default',
  1200000, 1200000, 16, 0, 3105, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 641, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-066-lacoste'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100066');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100067',
  'Default',
  2650000, NULL, 155, 0, 2489, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 503, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-067-uniqlo'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100067');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100068',
  'Default',
  2850000, NULL, 250, 0, 1750, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 571, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-068-calvinklein'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100068');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100069',
  'Default',
  850000, 850000, 255, 0, 2745, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 659, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-069-uniqlo'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100069');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100070',
  'Default',
  2750000, 2750000, 235, 0, 200, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 512, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-070-levis'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100070');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100071',
  'Default',
  750000, NULL, 184, 0, 4715, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 633, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-071-calvinklein'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100071');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100072',
  'Default',
  4300000, NULL, 104, 0, 45, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 670, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-072-puma'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100072');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100073',
  'Default',
  3700000, 3700000, 36, 0, 4243, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 462, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-073-adidas'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100073');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100074',
  'Default',
  1150000, NULL, 139, 0, 4351, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 602, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-074-levis'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100074');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100075',
  'Default',
  4400000, NULL, 254, 0, 3652, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 640, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-075-underarmour'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100075');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100076',
  'Default',
  3950000, 3950000, 68, 0, 2271, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 574, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-076-underarmour'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100076');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100077',
  'Default',
  2450000, NULL, 43, 0, 1920, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 535, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-077-levis'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100077');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100078',
  'Default',
  850000, 850000, 215, 0, 1251, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 504, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-078-lacoste'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100078');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100079',
  'Default',
  3550000, NULL, 261, 0, 510, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 663, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-079-puma'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100079');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100080',
  'Default',
  300000, NULL, 47, 0, 3116, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 451, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-080-puma'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100080');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100081',
  'Default',
  10500000, NULL, 37, 0, 3432, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8141, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-081-artemide'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100081');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100082',
  'Default',
  20750000, NULL, 87, 0, 2235, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8074, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-082-flexa'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100082');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100083',
  'Default',
  11800000, NULL, 86, 0, 1352, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8185, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-083-boconcept'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100083');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100084',
  'Default',
  18400000, 18400000, 298, 0, 4623, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 7956, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-084-fursys'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100084');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100085',
  'Default',
  5050000, NULL, 165, 0, 2131, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8033, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-085-boconcept'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100085');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100086',
  'Default',
  11450000, NULL, 53, 0, 2279, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8193, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-086-westelm'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100086');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100087',
  'Default',
  8950000, NULL, 137, 0, 4418, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8193, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-087-boconcept'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100087');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100088',
  'Default',
  22300000, 22300000, 70, 0, 329, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 7957, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-088-ergohuman'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100088');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100089',
  'Default',
  7200000, NULL, 295, 0, 1954, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8071, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-089-ergohuman'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100089');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100090',
  'Default',
  19400000, NULL, 223, 0, 2099, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 7992, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-090-vinyl'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100090');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100091',
  'Default',
  24600000, 24600000, 125, 0, 2548, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8098, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-091-fursys'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100091');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100092',
  'Default',
  19800000, NULL, 277, 0, 3249, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8000, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-092-songmics'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100092');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100093',
  'Default',
  23650000, NULL, 204, 0, 2470, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8103, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-093-fursys'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100093');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100094',
  'Default',
  19500000, NULL, 240, 0, 3509, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8044, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-094-vinyl'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100094');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100095',
  'Default',
  22250000, NULL, 236, 0, 4015, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8060, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-095-flexa'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100095');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100096',
  'Default',
  21850000, NULL, 162, 0, 1253, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 7995, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-096-artemide'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100096');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100097',
  'Default',
  22350000, 22350000, 229, 0, 4408, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8069, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-097-flexa'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100097');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100098',
  'Default',
  9500000, NULL, 166, 0, 709, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8175, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-098-fursys'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100098');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100099',
  'Default',
  16100000, NULL, 40, 0, 2755, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8076, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-099-ergohuman'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100099');

INSERT INTO product_variant (product_id, sku, variant_name, price, sale_price, stock_quantity, reserved_quantity, sold_count, image_url, weight_gram, is_active, created_at, updated_at)
SELECT
  p.id,
  'SKU-100100',
  'Default',
  6600000, NULL, 60, 0, 2116, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 8021, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-100-artemide'
  AND NOT EXISTS (SELECT 1 FROM product_variant WHERE sku = 'SKU-100100');


-- 3) PRODUCT IMAGES (1 thumbnail per product)
INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Apple Smartphone Model 001', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-001-apple'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Nokia Smartphone Model 002', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-002-nokia'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Motorola Smartphone Model 003', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-003-motorola'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Motorola Smartphone Model 004', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-004-motorola'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Vivo Smartphone Model 005', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-005-vivo'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Realme Smartphone Model 006', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-006-realme'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Apple Smartphone Model 007', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-007-apple'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Apple Smartphone Model 008', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-008-apple'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'OnePlus Smartphone Model 009', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-009-oneplus'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Vivo Smartphone Model 010', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-010-vivo'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Oppo Smartphone Model 011', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-011-oppo'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Samsung Smartphone Model 012', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-012-samsung'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Motorola Smartphone Model 013', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-013-motorola'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Honor Smartphone Model 014', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-014-honor'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Motorola Smartphone Model 015', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-015-motorola'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Apple Smartphone Model 016', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-016-apple'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Apple Smartphone Model 017', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-017-apple'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Samsung Smartphone Model 018', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-018-samsung'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Realme Smartphone Model 019', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-019-realme'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Vivo Smartphone Model 020', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-020-vivo'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Nokia Smartphone Model 021', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-021-nokia'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Nokia Smartphone Model 022', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-022-nokia'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Vivo Smartphone Model 023', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-023-vivo'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Apple Smartphone Model 024', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-024-apple'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Nokia Smartphone Model 025', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-025-nokia'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Oppo Smartphone Model 026', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-026-oppo'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Honor Smartphone Model 027', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-027-honor'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Motorola Smartphone Model 028', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-028-motorola'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Samsung Smartphone Model 029', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-029-samsung'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/42/299033/iphone-15-pro-blue-1.jpg', 'Nokia Smartphone Model 030', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-smartphone-030-nokia'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Razer Laptop Model 031', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-031-razer'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Razer Laptop Model 032', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-032-razer'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Acer Laptop Model 033', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-033-acer'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'MSI Laptop Model 034', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-034-msi'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Microsoft Laptop Model 035', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-035-microsoft'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Asus Laptop Model 036', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-036-asus'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Apple Laptop Model 037', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-037-apple'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Microsoft Laptop Model 038', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-038-microsoft'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'LG Laptop Model 039', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-039-lg'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Microsoft Laptop Model 040', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-040-microsoft'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Dell Laptop Model 041', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-041-dell'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'LG Laptop Model 042', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-042-lg'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'LG Laptop Model 043', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-043-lg'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'HP Laptop Model 044', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-044-hp'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'LG Laptop Model 045', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-045-lg'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Dell Laptop Model 046', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-046-dell'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'LG Laptop Model 047', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-047-lg'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Dell Laptop Model 048', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-048-dell'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Lenovo Laptop Model 049', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-049-lenovo'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Lenovo Laptop Model 050', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-050-lenovo'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Lenovo Laptop Model 051', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-051-lenovo'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Microsoft Laptop Model 052', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-052-microsoft'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Microsoft Laptop Model 053', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-053-microsoft'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Lenovo Laptop Model 054', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-054-lenovo'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/44/322086/macbook-pro-14-inch-m3-2023-1.jpg', 'Asus Laptop Model 055', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-laptop-055-asus'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Levi''s Men''s Apparel Model 056', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-056-levis'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Zara Men''s Apparel Model 057', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-057-zara'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Adidas Men''s Apparel Model 058', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-058-adidas'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Under Armour Men''s Apparel Model 059', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-059-underarmour'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Levi''s Men''s Apparel Model 060', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-060-levis'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Puma Men''s Apparel Model 061', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-061-puma'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'H&M Men''s Apparel Model 062', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-062-h&m'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Adidas Men''s Apparel Model 063', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-063-adidas'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Lacoste Men''s Apparel Model 064', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-064-lacoste'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Adidas Men''s Apparel Model 065', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-065-adidas'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Lacoste Men''s Apparel Model 066', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-066-lacoste'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Uniqlo Men''s Apparel Model 067', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-067-uniqlo'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Calvin Klein Men''s Apparel Model 068', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-068-calvinklein'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Uniqlo Men''s Apparel Model 069', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-069-uniqlo'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Levi''s Men''s Apparel Model 070', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-070-levis'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Calvin Klein Men''s Apparel Model 071', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-071-calvinklein'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Puma Men''s Apparel Model 072', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-072-puma'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Adidas Men''s Apparel Model 073', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-073-adidas'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Levi''s Men''s Apparel Model 074', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-074-levis'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Under Armour Men''s Apparel Model 075', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-075-underarmour'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Under Armour Men''s Apparel Model 076', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-076-underarmour'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Levi''s Men''s Apparel Model 077', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-077-levis'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Lacoste Men''s Apparel Model 078', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-078-lacoste'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Puma Men''s Apparel Model 079', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-079-puma'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/24/248503/ao-thun-nam-1.jpg', 'Puma Men''s Apparel Model 080', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-mens-080-puma'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Artemide Furniture Model 081', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-081-artemide'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Flexa Furniture Model 082', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-082-flexa'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'BoConcept Furniture Model 083', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-083-boconcept'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Fursys Furniture Model 084', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-084-fursys'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'BoConcept Furniture Model 085', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-085-boconcept'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'West Elm Furniture Model 086', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-086-westelm'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'BoConcept Furniture Model 087', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-087-boconcept'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Ergohuman Furniture Model 088', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-088-ergohuman'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Ergohuman Furniture Model 089', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-089-ergohuman'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Vinyl Furniture Model 090', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-090-vinyl'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Fursys Furniture Model 091', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-091-fursys'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Songmics Furniture Model 092', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-092-songmics'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Fursys Furniture Model 093', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-093-fursys'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Vinyl Furniture Model 094', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-094-vinyl'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Flexa Furniture Model 095', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-095-flexa'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Artemide Furniture Model 096', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-096-artemide'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Flexa Furniture Model 097', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-097-flexa'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Fursys Furniture Model 098', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-098-fursys'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Ergohuman Furniture Model 099', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-099-ergohuman'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);

INSERT INTO product_image (product_id, image_url, alt_text, sort_order, is_thumbnail, created_at, updated_at)
SELECT p.id, 'https://cdn.tgdd.vn/Products/Images/19873/19873805/ban-lam-viec-1.jpg', 'Artemide Furniture Model 100', 0, TRUE, NOW(), NOW()
FROM product p
WHERE p.slug = 'demo-furn-100-artemide'
  AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 0);


-- 4) Refresh seller.total_product counter
UPDATE seller s
   SET total_product = (SELECT COUNT(*) FROM product p WHERE p.seller_id = s.id AND p.deleted_at IS NULL),
       updated_at = NOW()
 WHERE s.slug = 'demo-shop';
