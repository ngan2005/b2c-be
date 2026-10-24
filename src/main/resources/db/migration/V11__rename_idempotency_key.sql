-- V11: Rename the legacy "key" column to "idempotency_key" on idempotency_record.
--
-- The Supabase dev environment has a pre-V7 table where the unique key column
-- was named simply "key". Hibernate's ddl-auto=validate refuses to start because
-- the entity field is mapped to column name "idempotency_key". V7 already shipped
-- a guard for this case, but it only runs when V7 itself is applied for the
-- first time; on this database V7 is already in flyway_schema_history so the
-- guard is a no-op.
--
-- This V11 migration is intentionally narrow and idempotent: it only renames
-- the column if both the legacy column exists AND the new one does not.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema='public'
          AND table_name='idempotency_record'
          AND column_name='key'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema='public'
          AND table_name='idempotency_record'
          AND column_name='idempotency_key'
    ) THEN
        ALTER TABLE idempotency_record RENAME COLUMN "key" TO idempotency_key;
    END IF;
END$$;