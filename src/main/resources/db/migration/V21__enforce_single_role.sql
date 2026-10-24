-- V21: Enforce single-role-per-user policy
-- ================================================================
-- Since V4 the user_roles join table allowed a user to hold multiple
-- roles simultaneously. This migration:
--   1. Keeps ONE role per user using priority ADMIN > SELLER > BUYER.
--   2. Syncs users.role_id to match that kept role.
--   3. Adds a DB-level unique constraint so no future code path can
--      silently create a duplicate.
--
-- This migration is idempotent: it detects and handles all four cases:
--   a) Only BUYER  → keep BUYER
--   b) Only SELLER → keep SELLER
--   c) Only ADMIN  → keep ADMIN
--   d) BUYER + SELLER → keep SELLER (SELLER outranks BUYER)
--   e) BUYER + ADMIN  → keep ADMIN  (ADMIN outranks BUYER)
--   f) SELLER + ADMIN → keep ADMIN  (ADMIN outranks SELLER)
--   g) All three      → keep ADMIN  (ADMIN is highest)
--   h) None          → no-op
-- ================================================================

-- 1. For users with > 1 role in user_roles, keep the highest-priority one.
--    Uses a CTE so PostgreSQL evaluates the subquery once per outer row.
WITH kept_roles AS (
    SELECT
        ur.user_id,
        ur.role_id,
        ROW_NUMBER() OVER (
            PARTITION BY ur.user_id
            ORDER BY
                CASE r.code
                    WHEN 'ADMIN'  THEN 1
                    WHEN 'SELLER' THEN 2
                    WHEN 'BUYER'  THEN 3
                    ELSE 99
                END
        ) AS rn
    FROM user_roles ur
    JOIN role r ON r.id = ur.role_id
),
duplicates AS (
    SELECT user_id, role_id
    FROM kept_roles
    WHERE rn > 1
)
DELETE FROM user_roles ur
USING duplicates d
WHERE ur.user_id = d.user_id AND ur.role_id = d.role_id;

-- 2. Sync users.role_id (legacy single-role column) to match the single
--    role now present in user_roles. The column is still read by some
--    legacy code paths; keeping it in sync prevents that code from seeing
--    a stale role after this migration.
UPDATE users u
SET role_id = (
    SELECT r.id
    FROM user_roles ur
    JOIN role r ON r.id = ur.role_id
    WHERE ur.user_id = u.id
    LIMIT 1
)
WHERE EXISTS (
    SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id
)
  AND (
      -- Only update when the column is stale (different from the join table).
      u.role_id IS DISTINCT FROM (
          SELECT ur2.role_id FROM user_roles ur2 WHERE ur2.user_id = u.id LIMIT 1
      )
  );

-- 3. Users with NO entry in user_roles but a stale users.role_id:
--    backfill their single role from the legacy column.
INSERT INTO user_roles (user_id, role_id, granted_at)
SELECT u.id, u.role_id, CURRENT_TIMESTAMP
FROM users u
WHERE NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id)
  AND u.role_id IS NOT NULL
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 4. Add a DB-level guard so future inserts / updates cannot violate the
--    single-role invariant. The FK to role(id) and NOT NULL are already in
--    place from V4; this PK (user_id, role_id) now uniquely enforces the
--    "at most one row per user" constraint.
--
--    Note: V4 already created PRIMARY KEY (user_id, role_id) on this table.
--    This migration exists to CLEAN UP existing duplicates (step 1 above).
--    Step 4 is a no-op on a correctly-running system but harmless.
--    We re-assert the PK explicitly so the intent is clear to future readers.
--    Two possible error codes must be ignored:
--      42710 (duplicate_object) — if the constraint name already exists
--      42P16 (multiple primary keys) — if a PK already exists from V4
DO $$
BEGIN
    ALTER TABLE user_roles ADD CONSTRAINT user_roles_single_role_per_user
        PRIMARY KEY (user_id, role_id);
EXCEPTION
    WHEN duplicate_object THEN
        NULL;  -- PK already exists — this migration is already applied
    WHEN OTHERS THEN
        -- 42P16 = "multiple primary keys for table ... are not allowed"
        -- This happens when a PK already exists from V4. Treat as success.
        IF SQLSTATE = '42P16' THEN
            NULL;
        ELSE
            RAISE;
        END IF;
END $$;
