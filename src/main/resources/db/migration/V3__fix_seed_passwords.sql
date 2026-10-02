-- V3: Patch existing seed user passwords that were inserted with the wrong BCrypt hash.
-- The original V2 hash ($2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
-- actually corresponds to "password", not "password123" as the comment claimed.
-- This migration replaces those hashes with the correct one so /auth/login works.
--
-- New hash (verified): $2a$10$cC5WBQWEpuZgMSmjFwV5fO/NKYtvoR6vHe8f8/yHGSFTmiEOanF72

UPDATE users
   SET password_hash = '$2a$10$cC5WBQWEpuZgMSmjFwV5fO/NKYtvoR6vHe8f8/yHGSFTmiEOanF72',
       updated_at    = NOW()
 WHERE email IN ('demo.buyer@example.com', 'demo.seller@example.com');
