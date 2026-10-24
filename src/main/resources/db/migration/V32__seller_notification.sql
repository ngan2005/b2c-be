-- V32__seller_notification.sql
-- Stores per-seller notifications when an admin creates/updates/disables a commission rule.

CREATE TABLE seller_notification (
    id            BIGSERIAL PRIMARY KEY,
    seller_id     BIGINT        NOT NULL REFERENCES seller(id) ON DELETE CASCADE,
    type          VARCHAR(50)   NOT NULL,
    title         VARCHAR(255)  NOT NULL,
    body          TEXT          NOT NULL,
    rule_id       BIGINT        NULL REFERENCES commission_rule(id) ON DELETE SET NULL,
    rule_name     VARCHAR(255)  NULL,
    rule_percent  NUMERIC(5,2)  NULL,
    is_read       BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Fast lookups for the seller's notification list
CREATE INDEX idx_seller_notification_seller_created
    ON seller_notification(seller_id, created_at DESC);

-- Fast lookups for the unread badge count
CREATE INDEX idx_seller_notification_unread
    ON seller_notification(seller_id) WHERE is_read = FALSE;
