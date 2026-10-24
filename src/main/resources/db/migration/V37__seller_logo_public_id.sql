-- =========================================================
-- V37 — Seller logo storage reference
-- =========================================================
-- Store the Cloudinary public_id of the shop avatar alongside the
-- public URL in seller.logo_url. The public_id lets the backend
-- destroy the previous asset on Cloudinary when the seller uploads
-- a replacement, so repeated avatar changes do not leak orphaned
-- files in the storage bucket.
--
-- The column is nullable: rows created before this migration (and
-- logos set by other means, e.g. a seed script) simply have no
-- public_id and are therefore not deletable from Cloudinary — the
-- URL still works.
ALTER TABLE seller
    ADD COLUMN IF NOT EXISTS logo_public_id VARCHAR(255);

-- Index to speed up the "find the asset to destroy" lookup performed
-- when a seller replaces their avatar.
CREATE INDEX IF NOT EXISTS idx_seller_logo_public_id
    ON seller(logo_public_id)
    WHERE logo_public_id IS NOT NULL;
