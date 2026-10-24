-- =========================================================
-- V5: Password Reset Token
-- =========================================================
-- Stores hashed (SHA-256) one-time reset tokens issued by
-- POST /api/v1/auth/forgot-password. Raw tokens are never
-- persisted; they only live in the email link and the user's
-- reset request body.
-- =========================================================

CREATE TABLE IF NOT EXISTS password_reset_token (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    token_hash      VARCHAR(255) NOT NULL UNIQUE,
    expires_at      TIMESTAMP NOT NULL,
    used            BOOLEAN NOT NULL DEFAULT FALSE,
    used_at         TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_password_reset_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_password_reset_token_user
    ON password_reset_token(user_id);

CREATE INDEX IF NOT EXISTS idx_password_reset_token_hash
    ON password_reset_token(token_hash);

CREATE INDEX IF NOT EXISTS idx_password_reset_token_expires
    ON password_reset_token(expires_at);
