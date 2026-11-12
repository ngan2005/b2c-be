-- =========================================================
-- V40: Mobile Password Reset OTP
-- =========================================================
-- Dedicated table for 6-digit numeric OTP forgot-password flow
-- used by mobile applications. Keeps the web URL-token flow
-- completely untouched.
-- =========================================================

CREATE TABLE IF NOT EXISTS mobile_password_reset_otp (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    otp_hash        VARCHAR(255) NOT NULL,
    reset_token     VARCHAR(255),
    expires_at      TIMESTAMP NOT NULL,
    verified        BOOLEAN NOT NULL DEFAULT FALSE,
    used            BOOLEAN NOT NULL DEFAULT FALSE,
    failed_attempts INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mobile_password_reset_otp_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_mobile_otp_user
    ON mobile_password_reset_otp(user_id);

CREATE INDEX IF NOT EXISTS idx_mobile_otp_reset_token
    ON mobile_password_reset_otp(reset_token);
