-- V31: Restore multi-role support
-- ================================================================
-- V21 enforced single-role-per-user by deleting duplicate rows from
-- user_roles. This is the inverse migration: it restores the BUYER
-- role to every user who was promoted to SELLER or ADMIN but had their
-- BUYER row stripped during the V21 / approve workflow.
--
-- V21 kept one role per user using priority ADMIN > SELLER > BUYER:
--   - BUYER + SELLER users → SELLER row kept, BUYER row deleted
--   - BUYER + ADMIN  users → ADMIN row kept, BUYER row deleted
--   - SELLER + ADMIN users → ADMIN row kept (BUYER already gone)
--   - BUYER-only          → no change
--   - ADMIN-only          → no change
--
-- V31 restores BUYER for every non-BUYER user who lost it, so that
-- approved sellers can both BUY and SELL and admins can access buyer
-- views if needed. The primary key (user_id, role_id) on user_roles
-- makes inserts idempotent — re-running this migration is safe.
--
-- ADMIN-only account admin@b2c.local is NOT affected because it
-- started with only the ADMIN row (no BUYER to restore).
-- ================================================================

-- 1. Restore BUYER role to every user who:
--    - has a non-BUYER role (SELLER or ADMIN) in user_roles
--    - does NOT already have a BUYER row in user_roles
--    This undoes the BUYER-strip logic from V21 and the old
--    SellerApplicationServiceImpl.approve() block.
INSERT INTO user_roles (user_id, role_id, granted_at)
SELECT
    ur.user_id,
    r_buyer.id,
    CURRENT_TIMESTAMP
FROM user_roles ur
JOIN role r_buyer ON r_buyer.code = 'BUYER'
WHERE NOT EXISTS (
    SELECT 1
    FROM user_roles ur2
    JOIN role r2 ON r2.id = ur2.role_id
    WHERE ur2.user_id = ur.user_id AND r2.code = 'BUYER'
)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 2. Log summary for visibility in Flyway output
DO $$
DECLARE
    restored_count INTEGER;
BEGIN
    GET DIAGNOSTICS restored_count = ROW_COUNT;
    RAISE NOTICE 'V31: Restored BUYER role to % user(s) who had it stripped.', restored_count;
END $$;
