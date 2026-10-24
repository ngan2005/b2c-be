-- =========================================================
-- V26: Platform wallet + seller payout ledger
-- =========================================================

CREATE TABLE IF NOT EXISTS platform_wallet (
    id              BIGSERIAL PRIMARY KEY,
    balance         DECIMAL(18,2) NOT NULL DEFAULT 0,
    total_credited  DECIMAL(18,2) NOT NULL DEFAULT 0,
    total_debited   DECIMAL(18,2) NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_platform_wallet_balance CHECK (balance >= 0)
);

-- Singleton row (id = 1). UPSERT so Flyway replay is safe.
INSERT INTO platform_wallet (id, balance) VALUES (1, 0) ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS platform_wallet_txn (
    id              BIGSERIAL PRIMARY KEY,
    wallet_id       BIGINT NOT NULL DEFAULT 1,
    direction       VARCHAR(10) NOT NULL CHECK (direction IN ('CREDIT','DEBIT')),
    amount          DECIMAL(18,2) NOT NULL,
    balance_after   DECIMAL(18,2) NOT NULL,
    reason          VARCHAR(50) NOT NULL,   -- COMMISSION | PAYOUT | ADJUSTMENT | REFUND
    ref_order_id    BIGINT,
    ref_seller_id   BIGINT,
    note            TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pwallet_txn_wallet FOREIGN KEY (wallet_id) REFERENCES platform_wallet(id),
    CONSTRAINT fk_pwallet_txn_order  FOREIGN KEY (ref_order_id) REFERENCES orders(id),
    CONSTRAINT fk_pwallet_txn_seller FOREIGN KEY (ref_seller_id) REFERENCES seller(id)
);

CREATE INDEX IF NOT EXISTS idx_pwallet_txn_created ON platform_wallet_txn(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_pwallet_txn_seller  ON platform_wallet_txn(ref_seller_id);

CREATE TABLE IF NOT EXISTS seller_payout (
    id                BIGSERIAL PRIMARY KEY,
    seller_id         BIGINT NOT NULL,
    order_id          BIGINT,                    -- nullable: single-order payout; null means aggregated
    gross_amount      DECIMAL(15,2) NOT NULL,
    commission_amount  DECIMAL(15,2) NOT NULL DEFAULT 0,
    net_amount        DECIMAL(15,2) NOT NULL,    -- gross - commission
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','READY','PAID','CANCELLED')),
    bank_reference    VARCHAR(100),              -- filled by admin when marking paid
    paid_at           TIMESTAMP,
    paid_by           BIGINT,                    -- admin user id
    period_start      TIMESTAMP,                 -- optional aggregation window
    period_end        TIMESTAMP,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seller_payout_seller FOREIGN KEY (seller_id) REFERENCES seller(id),
    CONSTRAINT fk_seller_payout_order  FOREIGN KEY (order_id)  REFERENCES orders(id),
    CONSTRAINT fk_seller_payout_paid_by FOREIGN KEY (paid_by)   REFERENCES users(id),
    CONSTRAINT chk_seller_payout_net   CHECK (net_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_seller_payout_seller ON seller_payout(seller_id);
CREATE INDEX IF NOT EXISTS idx_seller_payout_status ON seller_payout(status);
CREATE INDEX IF NOT EXISTS idx_seller_payout_order  ON seller_payout(order_id);
