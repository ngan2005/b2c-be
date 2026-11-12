-- V41__add_brand_to_search_vector.sql
-- Add brand (weight C) to generated search_vector and add trigram index for brand

-- 1. Recreate search_vector column to include brand (weight C)
ALTER TABLE product DROP COLUMN IF EXISTS search_vector;

ALTER TABLE product
    ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', public.immutable_unaccent(coalesce(name, ''))), 'A') ||
        setweight(to_tsvector('simple', public.immutable_unaccent(coalesce(description, ''))), 'B') ||
        setweight(to_tsvector('simple', public.immutable_unaccent(coalesce(brand, ''))), 'C')
    ) STORED;

-- 2. Re-create GIN index on the generated search_vector
CREATE INDEX IF NOT EXISTS idx_product_search_vector
    ON product USING GIN (search_vector);

-- 3. Trigram GIN index on brand for substring/fallback search
CREATE INDEX IF NOT EXISTS idx_product_brand_trgm
    ON product USING GIN (brand gin_trgm_ops);
