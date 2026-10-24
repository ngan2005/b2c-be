-- V15: backfill users.role_id to match the highest role the user
-- holds in user_roles. Before this migration, some promotion paths
-- (e.g. SellerApplicationServiceImpl.approve) only inserted into
-- user_roles and never updated users.role_id, so the legacy column
-- could lag behind reality. This brings it back in sync.
--
-- Idempotent: a no-op on rows that are already correct.
--
-- ADMIN > SELLER > BUYER priority is enforced by the WHERE clause
-- of each UPDATE — we only overwrite the legacy column if a higher
-- role is present in the join table.

UPDATE users u
SET    role_id = (
           SELECT r.id
           FROM   user_roles ur
           JOIN   role r ON r.id = ur.role_id
           WHERE  ur.user_id = u.id AND r.code = 'ADMIN'
           LIMIT  1
       )
WHERE  EXISTS (
           SELECT 1
           FROM   user_roles ur
           JOIN   role r ON r.id = ur.role_id
           WHERE  ur.user_id = u.id AND r.code = 'ADMIN'
       )
AND    (
           u.role_id IS NULL
           OR u.role_id <> (SELECT r.id FROM role r WHERE r.code = 'ADMIN')
       );

-- For users who are SELLER but not ADMIN, set role_id = SELLER.
UPDATE users u
SET    role_id = (
           SELECT r.id
           FROM   user_roles ur
           JOIN   role r ON r.id = ur.role_id
           WHERE  ur.user_id = u.id AND r.code = 'SELLER'
           LIMIT  1
       )
WHERE  NOT EXISTS (
           SELECT 1
           FROM   user_roles ur
           JOIN   role r ON r.id = ur.role_id
           WHERE  ur.user_id = u.id AND r.code = 'ADMIN'
       )
AND    EXISTS (
           SELECT 1
           FROM   user_roles ur
           JOIN   role r ON r.id = ur.role_id
           WHERE  ur.user_id = u.id AND r.code = 'SELLER'
       )
AND    (
           u.role_id IS NULL
           OR u.role_id <> (SELECT r.id FROM role r WHERE r.code = 'SELLER')
       );

-- For users who are BUYER but role_id is NULL or wrong, set to BUYER.
UPDATE users u
SET    role_id = (
           SELECT r.id
           FROM   user_roles ur
           JOIN   role r ON r.id = ur.role_id
           WHERE  ur.user_id = u.id AND r.code = 'BUYER'
           LIMIT  1
       )
WHERE  NOT EXISTS (
           SELECT 1
           FROM   user_roles ur
           JOIN   role r ON r.id = ur.role_id
           WHERE  ur.user_id = u.id
           AND    r.code IN ('ADMIN', 'SELLER')
       )
AND    EXISTS (
           SELECT 1
           FROM   user_roles ur
           JOIN   role r ON r.id = ur.role_id
           WHERE  ur.user_id = u.id AND r.code = 'BUYER'
       )
AND    u.role_id IS NULL;
