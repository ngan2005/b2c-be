-- V7: Add optimistic locking version to product_variant + idempotency record table
-- Supports E0/E1/E2/E3 concurrency experiment framework

-- 1. Add version column for optimistic locking (E1, E3)
ALTER TABLE product_variant
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- 2. Audit timestamp for last stock reservation
ALTER TABLE product_variant
    ADD COLUMN last_reserved_at TIMESTAMP;

-- 3. Index for low-stock lookups
CREATE INDEX idx_variant_stock_active ON product_variant(stock_quantity, is_active);

-- 4. Idempotency record table for Order create + Payment callback
CREATE TABLE idempotency_record (
    id BIGSERIAL PRIMARY KEY,
    scope VARCHAR(64) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_payload TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT uq_idempotency_scope_key UNIQUE (scope, idempotency_key),
    CONSTRAINT chk_idempotency_status CHECK (status IN ('IN_PROGRESS', 'DONE', 'FAILED'))
);

-- 4a. Guard for environments where V7 has already been applied with the
--     legacy column name "key" — rename to idempotency_key and add updated_at.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'idempotency_record' AND column_name = 'key'
    ) THEN
        ALTER TABLE idempotency_record RENAME COLUMN "key" TO idempotency_key;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'idempotency_record' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE idempotency_record
            ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
END$$;

CREATE INDEX idx_idempotency_scope ON idempotency_record(scope);
CREATE INDEX idx_idempotency_created_at ON idempotency_record(created_at);

-- 5. Add idempotency_key column to orders table
ALTER TABLE orders
    ADD COLUMN idempotency_key VARCHAR(128);

CREATE UNIQUE INDEX uq_orders_buyer_idempotency
    ON orders(buyer_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;

-- 6. Add idempotency_key column to payment table
ALTER TABLE payment
    ADD COLUMN idempotency_key VARCHAR(128);

CREATE UNIQUE INDEX uq_payment_order_txn
    ON payment(order_id, transaction_id)
    WHERE transaction_id IS NOT NULL;

-- 7. Make sure load_test_result has updated_at (mirror of BaseEntity)
ALTER TABLE load_test_result
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
