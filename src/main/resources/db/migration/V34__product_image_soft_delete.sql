-- Soft-delete column for product images so the seller UI can offer an
-- "undo" toast right after deletion. Cloudinary assets are now only
-- removed by a separate cleanup job once the row has been soft-deleted
-- for longer than the undo window.

ALTER TABLE product_image
    ADD COLUMN deleted_at TIMESTAMP NULL;

-- Existing list/sort query already orders by sort_order; we just need to
-- make the default read path filter out soft-deleted rows.
DROP INDEX IF EXISTS idx_product_image_product;
CREATE INDEX idx_product_image_product_active
    ON product_image (product_id, deleted_at);
