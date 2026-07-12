-- ==============================================================================
-- V6 — Create Interview State History Table
-- AI Mock Interview Platform
--
-- Module:  Module 2 — Interview Session Management
-- Date:    2026-07-09
--
-- Append-only audit log. Never UPDATE — only INSERT.
-- ==============================================================================

CREATE TABLE interview_state_history
(
    id               UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    interview_id     UUID                     NOT NULL REFERENCES interviews (id) ON DELETE CASCADE,
    previous_state   VARCHAR(50),
    current_state    VARCHAR(50)              NOT NULL,
    transition_event VARCHAR(100)             NOT NULL,
    transitioned_by  VARCHAR(100)             NOT NULL DEFAULT 'SYSTEM',
    transition_reason TEXT,
    transitioned_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_state_history_interview_time ON interview_state_history (interview_id, transitioned_at DESC);
