-- =========================================================
-- V6: Add created_at / updated_at to order_item
-- =========================================================
-- The OrderItem JPA entity extends BaseEntity, which declares
-- created_at and updated_at columns. The V1 schema for order_item
-- was missing these columns, so Hibernate's schema validation
-- (ddl-auto=validate) fails on startup against PostgreSQL.
-- Add the two columns with the same semantics used by other tables
-- in V1: NOT NULL with CURRENT_TIMESTAMP default. Existing rows
-- (if any) are back-filled to the current timestamp.
-- =========================================================

ALTER TABLE order_item
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;