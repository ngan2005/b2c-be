-- V12: Add OAuth2 provider columns to users table.
--
-- Social login (Google, Facebook, ...) creates users that have no local
-- password. The legacy password_hash column was NOT NULL, so we relax it.
-- The provider column tracks how a user authenticated; provider_user_id
-- holds the stable id returned by the OAuth2 provider (e.g. Google "sub").
--
-- Idempotent: each ALTER guard checks information_schema so running this
-- migration twice is a no-op (Postgres + H2 in PostgreSQL mode both
-- support the same metadata query).

-- 1. Relax password_hash NOT NULL for social-only users.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = CURRENT_SCHEMA()
          AND table_name = 'users'
          AND column_name = 'password_hash'
          AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;
    END IF;
END$$;

-- 2. Add provider column (default LOCAL for existing rows).
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = CURRENT_SCHEMA()
          AND table_name = 'users'
          AND column_name = 'provider'
    ) THEN
        ALTER TABLE users ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';
    END IF;
END$$;

-- 3. Add provider_user_id (nullable; only social users have a value).
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = CURRENT_SCHEMA()
          AND table_name = 'users'
          AND column_name = 'provider_user_id'
    ) THEN
        ALTER TABLE users ADD COLUMN provider_user_id VARCHAR(255);
    END IF;
END$$;

-- 4. Add provider_linked_at (set on first social login / link).
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = CURRENT_SCHEMA()
          AND table_name = 'users'
          AND column_name = 'provider_linked_at'
    ) THEN
        ALTER TABLE users ADD COLUMN provider_linked_at TIMESTAMP;
    END IF;
END$$;

-- 5. Unique index on (provider, provider_user_id) — only when
--    provider_user_id is non-null, so many LOCAL users are unaffected.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE schemaname = CURRENT_SCHEMA()
          AND tablename = 'users'
          AND indexname = 'uq_users_provider_provider_user_id'
    ) THEN
        CREATE UNIQUE INDEX uq_users_provider_provider_user_id
            ON users (provider, provider_user_id)
            WHERE provider_user_id IS NOT NULL;
    END IF;
EXCEPTION WHEN OTHERS THEN
    -- H2 PostgreSQL mode: pg_indexes does not exist. Fall back to
    -- information_schema.statistics, which exists in both engines.
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = CURRENT_SCHEMA()
          AND table_name = 'users'
          AND index_name = 'uq_users_provider_provider_user_id'
    ) THEN
        EXECUTE 'CREATE UNIQUE INDEX uq_users_provider_provider_user_id
                 ON users (provider, provider_user_id)
                 WHERE provider_user_id IS NOT NULL';
    END IF;
END$$;
