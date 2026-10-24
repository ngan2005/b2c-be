-- Fix the V23 admin password hash (original was wrong).
-- Correct BCrypt cost-10 hash for "admin123" verified via /api/v1/__debug/bcrypt
UPDATE users
SET password_hash = '$2a$10$8xGEgPAh3aMaSxu/OjfTSunMaIAFR3DRMWf0XU3daEyTkWciMKpx.'
WHERE email = 'admin@b2c.local';
