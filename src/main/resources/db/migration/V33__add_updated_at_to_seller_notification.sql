-- V33__add_updated_at_to_seller_notification.sql
-- seller_notification extends BaseEntity which adds created_at + updated_at;
-- the initial V32 migration was missing updated_at.

ALTER TABLE seller_notification
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT NOW();
