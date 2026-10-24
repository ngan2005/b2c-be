-- V27__order_dispute.sql
CREATE TABLE IF NOT EXISTS order_dispute (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    opened_by       BIGINT NOT NULL,  -- user id
    dispute_type    VARCHAR(30) NOT NULL
        CHECK (dispute_type IN ('NOT_RECEIVED','WRONG_ITEM','DAMAGED','OTHER')),
    description     TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN'
        CHECK (status IN ('OPEN','INVESTIGATING','RESOLVED_REFUND','RESOLVED_REJECT','RESOLVED_PARTIAL','CLOSED')),
    resolution_note TEXT,
    refund_amount   DECIMAL(15,2),
    resolved_by     BIGINT,           -- admin user id
    resolved_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dispute_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_dispute_opener FOREIGN KEY (opened_by) REFERENCES users(id),
    CONSTRAINT fk_dispute_resolver FOREIGN KEY (resolved_by) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_dispute_order ON order_dispute(order_id);
CREATE INDEX IF NOT EXISTS idx_dispute_status ON order_dispute(status);
CREATE INDEX IF NOT EXISTS idx_dispute_created ON order_dispute(created_at);
