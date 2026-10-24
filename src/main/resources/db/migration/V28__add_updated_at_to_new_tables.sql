-- V28__add_updated_at_to_new_tables.sql
-- Fix: BaseEntity requires created_at + updated_at. Earlier migrations
-- (V25 commission_record, V25 commission_rule, V26 seller_payout,
--  V27 order_dispute) declared created_at only. Add the missing
-- updated_at columns with sane defaults so Hibernate schema validation
-- passes on a database that already has the V25/V26/V27 tables.

ALTER TABLE commission_record
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE commission_rule
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE seller_payout
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE order_dispute
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
