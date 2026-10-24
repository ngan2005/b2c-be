-- =========================================================
-- V14 — product_image audit columns
-- =========================================================
-- ProductImage does NOT extend com.example.B2C.common.entity.BaseEntity,
-- but the entity was just refactored to expose created_at / updated_at
-- directly so it can carry the same audit metadata as every other
-- domain table.
--
-- This migration:
--   1. Adds the two columns idempotently.
--   2. Backfills NOW() for any pre-existing rows so the NOT NULL
--      constraint is satisfied immediately (no surprise DEFAULT
--      application past the column-add step).
--   3. Adds a CHECK constraint guaranteeing future rows also have
--      updated_at >= created_at, mirroring what the rest of the
--      schema does.
--
-- V10 intentionally excluded product_image because the entity had
-- no audit fields back then; this migration is the follow-up.

-- 1. Add columns
ALTER TABLE product_image
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE product_image
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- 2. Backfill any pre-existing rows that may have NULL values
--    (the DEFAULT above already covers this on PostgreSQL 11+, but
--    we are defensive for older deployments).
UPDATE product_image
   SET created_at = CURRENT_TIMESTAMP
 WHERE created_at IS NULL;

UPDATE product_image
   SET updated_at = CURRENT_TIMESTAMP
 WHERE updated_at IS NULL;

-- 3. Constraint: updated_at must never precede created_at.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'chk_product_image_audit_order'
    ) THEN
        ALTER TABLE product_image
            ADD CONSTRAINT chk_product_image_audit_order
            CHECK (updated_at >= created_at);
    END IF;
END$$;
