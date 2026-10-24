-- Seed an admin user for testing the admin console.
-- Password: "admin123" (BCrypt cost-10 hash)
-- Hash verified via /api/v1/__debug/bcrypt?password=admin123

INSERT INTO users (email, password_hash, full_name, phone, status, role_id, created_at, updated_at)
SELECT 'admin@b2c.local',
       '$2a$10$8xGEgPAh3aMaSxu/OjfTSunMaIAFR3DRMWf0XU3daEyTkWciMKpx.',
       'Admin User', '0900000001', 'ACTIVE', r.id, NOW(), NOW()
FROM role r WHERE r.code = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@b2c.local');

-- Also insert into user_roles join table (authoritative since V21)
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, role r
WHERE u.email = 'admin@b2c.local'
  AND r.code = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id
  );
