-- ==============================================================================
-- V9 — Create Evaluations Table
-- AI Mock Interview Platform
--
-- Module:  Module 2 — Interview Session Management
-- Date:    2026-07-09
-- ==============================================================================

CREATE TABLE evaluations
(
    id                          UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    interview_id                UUID                     NOT NULL REFERENCES interviews (id) ON DELETE CASCADE,
    answer_id                   UUID                     NOT NULL REFERENCES answers (id) ON DELETE CASCADE,

    -- Technical dimension
    technical_score             INTEGER,
    technical_subscores         JSONB                    DEFAULT '{}',
    technical_feedback          TEXT,

    -- English dimension
    english_score               INTEGER,
    english_subscores           JSONB                    DEFAULT '{}',
    english_feedback            TEXT,

    -- Behavioral dimension
    behavioral_score            INTEGER,
    behavioral_subscores        JSONB                    DEFAULT '{}',
    behavioral_feedback         TEXT,

    -- Aggregated
    composite_score             DECIMAL(5, 2),
    performance_tier            VARCHAR(50),
    is_degraded                 BOOLEAN                  NOT NULL DEFAULT FALSE,

    -- Performance metrics
    technical_processing_ms     INTEGER,
    english_processing_ms       INTEGER,
    behavioral_processing_ms    INTEGER,

    evaluated_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_eval_technical_score   CHECK (technical_score IS NULL  OR technical_score  BETWEEN 0 AND 100),
    CONSTRAINT chk_eval_english_score     CHECK (english_score IS NULL    OR english_score    BETWEEN 0 AND 100),
    CONSTRAINT chk_eval_behavioral_score  CHECK (behavioral_score IS NULL OR behavioral_score BETWEEN 0 AND 100),
    CONSTRAINT chk_eval_composite_score   CHECK (composite_score IS NULL  OR composite_score  BETWEEN 0 AND 100),
    CONSTRAINT chk_eval_performance_tier  CHECK (performance_tier IS NULL OR performance_tier IN ('NEEDS_WORK','DEVELOPING','PROFICIENT','EXCELLENT')),
    UNIQUE (answer_id)
);

CREATE INDEX idx_evaluations_interview_id ON evaluations (interview_id);
CREATE INDEX idx_evaluations_answer_id    ON evaluations (answer_id);
