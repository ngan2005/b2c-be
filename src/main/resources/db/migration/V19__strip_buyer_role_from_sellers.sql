-- V19: strip BUYER role from any user who also holds SELLER.
-- A user with SELLER should not also be a BUYER; they have separate product spaces.
-- Historical order rows (orders.buyer_id) are NOT touched.

-- 1) Sync legacy users.role_id to SELLER for users who have SELLER in user_roles
UPDATE users u
SET role_id = (SELECT id FROM role WHERE code = 'SELLER')
WHERE EXISTS (
  SELECT 1 FROM user_roles ur
  JOIN role r ON r.id = ur.role_id
  WHERE ur.user_id = u.id AND r.code = 'SELLER'
)
AND u.role_id IS DISTINCT FROM (SELECT id FROM role WHERE code = 'SELLER');

-- 2) Remove BUYER from user_roles for any user that also has SELLER
DELETE FROM user_roles
WHERE role_id = (SELECT id FROM role WHERE code = 'BUYER')
  AND user_id IN (
    SELECT ur.user_id
    FROM user_roles ur
    JOIN role r ON r.id = ur.role_id
    WHERE r.code = 'SELLER'
  );
