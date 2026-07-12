-- ==============================================================================
-- V4 — Create Interview Templates Table
-- AI Mock Interview Platform
--
-- Module:  Module 2 — Interview Session Management
-- Date:    2026-07-09
-- ==============================================================================

CREATE TABLE interview_templates
(
    id                  UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    name                VARCHAR(255)             NOT NULL,
    domain              VARCHAR(255)             NOT NULL,
    role_level          VARCHAR(50)              NOT NULL,
    total_questions     INTEGER                  NOT NULL DEFAULT 10,
    duration_minutes    INTEGER                  NOT NULL DEFAULT 30,
    default_difficulty  VARCHAR(50)              NOT NULL DEFAULT 'MEDIUM',
    weight_config       JSONB                    NOT NULL DEFAULT '{"technical":0.50,"english":0.25,"behavioral":0.25}',
    is_active           BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_template_role_level      CHECK (role_level IN ('JUNIOR','MID','SENIOR','LEAD','PRINCIPAL')),
    CONSTRAINT chk_template_difficulty      CHECK (default_difficulty IN ('EASY','MEDIUM','HARD','EXPERT')),
    CONSTRAINT chk_template_total_questions CHECK (total_questions BETWEEN 1 AND 20),
    CONSTRAINT chk_template_duration        CHECK (duration_minutes BETWEEN 10 AND 120)
);

CREATE INDEX idx_templates_domain       ON interview_templates (domain);
CREATE INDEX idx_templates_role_level   ON interview_templates (role_level);
CREATE INDEX idx_templates_is_active    ON interview_templates (is_active);
