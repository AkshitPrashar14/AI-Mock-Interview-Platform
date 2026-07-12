-- ==============================================================================
-- V3 — Create Refresh Tokens Table
-- AI Mock Interview Platform
--
-- Sprint:  Sprint 2 — Authentication & User Management
-- Date:    2026-07-06
--
-- Design Notes:
--   - token_hash stores SHA-256(rawToken). Raw token is NEVER persisted.
--   - device_id / device_name prepare for multi-device session management.
--   - ip_address / user_agent support login history and anomaly detection.
--   - reason field records why a token was revoked (LOGOUT, LOGOUT_ALL, EXPIRED).
-- ==============================================================================

CREATE TABLE refresh_tokens
(
    id           UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    user_id      UUID                     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash   VARCHAR(255)             NOT NULL UNIQUE,
    device_id    VARCHAR(255),
    device_name  VARCHAR(255),
    ip_address   VARCHAR(45)              NOT NULL,
    user_agent   TEXT                     NOT NULL,
    expires_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMP WITH TIME ZONE,
    revoked      BOOLEAN                  NOT NULL DEFAULT FALSE,
    revoked_at   TIMESTAMP WITH TIME ZONE,
    reason       VARCHAR(100)
);

CREATE INDEX idx_refresh_tokens_user_id    ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_revoked    ON refresh_tokens (revoked) WHERE revoked = FALSE;
