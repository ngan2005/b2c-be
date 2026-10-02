-- =========================================================
-- V4: Seller Management
-- - user_roles join table for multi-role support
-- - seller_application table for seller application workflow
-- - Backfill: keep existing users' roles via user_roles table
-- =========================================================

-- user_roles: many-to-many join between users and roles
-- PK (user_id, role_id) serves as unique constraint preventing duplicate role grants
CREATE TABLE user_roles (
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     BIGINT NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    granted_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

-- seller_application: tracks BUYER's request to become a SELLER
CREATE TABLE seller_application (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users(id),
    shop_name         VARCHAR(150) NOT NULL,
    shop_description  TEXT,
    phone             VARCHAR(20) NOT NULL,
    address           TEXT NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason  TEXT,
    reviewed_at       TIMESTAMP,
    reviewed_by       BIGINT REFERENCES users(id),
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_seller_application_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- Backfill: grant every existing user the role already stored in users.role_id
INSERT INTO user_roles (user_id, role_id)
SELECT id, role_id FROM users;

-- Indexes for common query paths on seller_application
CREATE INDEX idx_seller_application_user_id    ON seller_application(user_id);
CREATE INDEX idx_seller_application_status    ON seller_application(status);
CREATE INDEX idx_seller_application_created_at ON seller_application(created_at DESC);
