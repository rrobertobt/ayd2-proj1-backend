-- ============================================================
-- V2 – Onboarding tokens for initial password setup
-- ============================================================

BEGIN;

CREATE TABLE onboarding_tokens
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(64) NOT NULL UNIQUE,
    used       BOOLEAN     NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_onboarding_token_token ON onboarding_tokens (token);
CREATE INDEX idx_onboarding_token_user  ON onboarding_tokens (user_id);

COMMIT;

