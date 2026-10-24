-- =========================================================
-- V13 — Cloudinary public_id column
-- =========================================================
-- Store the Cloudinary public_id alongside the public URL so we can
-- delete the asset on Cloudinary when the seller removes the image.
-- The column is nullable so the migration is safe for any rows that
-- pre-date Cloudinary integration (those rows just won't be deletable
-- from Cloudinary but the URL still works).

ALTER TABLE product_image
    ADD COLUMN IF NOT EXISTS cloudinary_public_id VARCHAR(255);

-- Index to speed up the "delete asset" lookup that the storage adapter
-- performs before issuing the Cloudinary destroy call.
CREATE INDEX IF NOT EXISTS idx_product_image_public_id
    ON product_image(cloudinary_public_id)
    WHERE cloudinary_public_id IS NOT NULL;
