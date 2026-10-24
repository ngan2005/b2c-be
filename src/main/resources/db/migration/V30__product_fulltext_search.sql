-- V30__product_fulltext_search.sql
-- Full-text search for product (replaces V29, which failed with
-- "SQLSTATE 42P17: generation expression is not immutable" because
-- the `unaccent()` function is registered as STABLE in PostgreSQL
-- and therefore cannot be used inside a STORED generated column).
--
-- Fix: create a thin IMMUTABLE wrapper around unaccent() and use
-- that wrapper inside the generated column expression. This is the
-- recommended pattern from the PostgreSQL community for accent-
-- insensitive full-text search with stored tsvector.
--
-- Features kept identical to V29:
--   - accent-insensitive Vietnamese (via unaccent)
--   - case-insensitive
--   - search_vector = name (weight A) + description (weight B)
--   - GIN index on search_vector
--   - GIN trigram indexes on name / description for ILIKE fallback
--     (handles very short queries where websearch_to_tsquery fails)

-- 1. Extensions (idempotent)
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 2. IMMUTABLE wrapper around unaccent(). Safe as long as the
--    `unaccent.rules` dictionary is not changed after this migration
--    is applied. If the rules ever change, a reindex is required.
--    The wrapper is created in the public schema and uses the standard
--    text-search-dictionary `unaccent` (the same one that
--    to_tsvector('unaccent', ...) would use internally).
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_proc p
        JOIN pg_namespace n ON n.oid = p.pronamespace
        WHERE p.proname = 'immutable_unaccent'
          AND n.nspname = 'public'
    ) THEN
        EXECUTE $sql$
            CREATE OR REPLACE FUNCTION public.immutable_unaccent(text)
            RETURNS text
            LANGUAGE sql
            IMMUTABLE
            PARALLEL SAFE
            AS $body$
                SELECT public.unaccent($1)
            $body$;
        $sql$;
    END IF;
END$$;

-- 3. Generated tsvector column using the immutable wrapper.
--    Drop the column first if a previous attempt left it behind
--    (V29 may have created it before the transaction rolled back,
--    or the schema may already be in an unknown state). The IF
--    EXISTS guard makes this safe on both fresh and partially
--    migrated databases.
ALTER TABLE product DROP COLUMN IF EXISTS search_vector;

ALTER TABLE product
    ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', public.immutable_unaccent(coalesce(name, ''))), 'A') ||
        setweight(to_tsvector('simple', public.immutable_unaccent(coalesce(description, ''))), 'B')
    ) STORED;

-- 4. GIN index on the generated tsvector (primary FTS plan)
CREATE INDEX IF NOT EXISTS idx_product_search_vector
    ON product USING GIN (search_vector);

-- 5. Trigram GIN indexes for short / partial queries handled by
--    the ILIKE fallback in ProductRepository.
CREATE INDEX IF NOT EXISTS idx_product_name_trgm
    ON product USING GIN (name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_product_description_trgm
    ON product USING GIN (description gin_trgm_ops);
