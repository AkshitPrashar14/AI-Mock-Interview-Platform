-- ==============================================================================
-- V2 — Create Users Table
-- AI Mock Interview Platform
--
-- Sprint:  Sprint 2 — Authentication & User Management
-- Date:    2026-07-06
-- ==============================================================================

CREATE TABLE users
(
    id                    UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    first_name            VARCHAR(100)             NOT NULL,
    last_name             VARCHAR(100)             NOT NULL,
    email                 VARCHAR(255)             NOT NULL UNIQUE,
    password_hash         VARCHAR(255)             NOT NULL,
    role                  VARCHAR(50)              NOT NULL DEFAULT 'USER',
    is_active             BOOLEAN                  NOT NULL DEFAULT TRUE,
    is_email_verified     BOOLEAN                  NOT NULL DEFAULT FALSE,
    profile_picture_url   TEXT,
    failed_login_attempts INTEGER                  NOT NULL DEFAULT 0,
    account_locked        BOOLEAN                  NOT NULL DEFAULT FALSE,
    account_locked_until  TIMESTAMP WITH TIME ZONE,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_login_at         TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_users_email ON users (email);
