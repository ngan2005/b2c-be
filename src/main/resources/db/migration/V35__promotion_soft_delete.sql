-- Soft-delete column for promotions so the admin UI can offer a short
-- "undo" action after deletion. The row is hidden from listings and
-- validation queries but kept (along with usage history) so a restore
-- is just a flag flip.
ALTER TABLE promotion
    ADD COLUMN deleted_at TIMESTAMP NULL;

CREATE INDEX idx_promotion_deleted_at ON promotion (deleted_at);
