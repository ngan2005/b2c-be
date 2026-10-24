-- =========================================================
-- V38 — User avatar storage reference
-- =========================================================
-- Store the Cloudinary public_id of a user's avatar alongside the
-- public URL in users.avatar_url. The public_id lets the backend
-- destroy the previous asset on Cloudinary when the user uploads a
-- replacement, so repeated avatar changes do not leak orphaned files.
--
-- The column is nullable: rows created before this migration (and
-- avatars set by other means, e.g. an OAuth2 provider) simply have no
-- public_id and are therefore not deletable from Cloudinary — the URL
-- still works.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS avatar_public_id VARCHAR(255);

-- Index to speed up the "find the asset to destroy" lookup performed
-- when a user replaces their avatar.
CREATE INDEX IF NOT EXISTS idx_users_avatar_public_id
    ON users(avatar_public_id)
    WHERE avatar_public_id IS NOT NULL;
