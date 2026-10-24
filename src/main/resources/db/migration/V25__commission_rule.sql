-- V25__commission_rule.sql
-- Platform commission engine: rule config + per-item snapshot records.

CREATE TABLE IF NOT EXISTS commission_rule (
    id              BIGSERIAL PRIMARY KEY,
    category_id     BIGINT,
    name            VARCHAR(150) NOT NULL,
    percent         DECIMAL(5,2) NOT NULL,
    min_order_value DECIMAL(15,2) DEFAULT 0,
    max_commission  DECIMAL(15,2),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    effective_to    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_commission_rule_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT chk_commission_percent CHECK (percent >= 0 AND percent <= 100)
);

CREATE TABLE IF NOT EXISTS commission_record (
    id                BIGSERIAL PRIMARY KEY,
    order_id          BIGINT NOT NULL,
    order_item_id     BIGINT NOT NULL,
    seller_id         BIGINT NOT NULL,
    category_id       BIGINT,
    rule_id           BIGINT,
    order_amount      DECIMAL(15,2) NOT NULL,
    commission_rate   DECIMAL(5,2) NOT NULL,
    commission_amount DECIMAL(15,2) NOT NULL,
    seller_net        DECIMAL(15,2) NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_commission_record_order   FOREIGN KEY (order_id)       REFERENCES orders(id),
    CONSTRAINT fk_commission_record_item    FOREIGN KEY (order_item_id)  REFERENCES order_item(id),
    CONSTRAINT fk_commission_record_seller  FOREIGN KEY (seller_id)      REFERENCES seller(id),
    CONSTRAINT fk_commission_record_category FOREIGN KEY (category_id)    REFERENCES category(id),
    CONSTRAINT fk_commission_record_rule    FOREIGN KEY (rule_id)         REFERENCES commission_rule(id),
    CONSTRAINT uq_commission_per_item UNIQUE (order_item_id)
);

CREATE INDEX IF NOT EXISTS idx_commission_record_order  ON commission_record(order_id);
CREATE INDEX IF NOT EXISTS idx_commission_record_seller ON commission_record(seller_id);
CREATE INDEX IF NOT EXISTS idx_commission_rule_active   ON commission_rule(is_active);

-- Seed: one global default rule of 5%
INSERT INTO commission_rule (category_id, name, percent, is_active)
SELECT NULL, 'Global default 5%', 5.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM commission_rule WHERE category_id IS NULL);
