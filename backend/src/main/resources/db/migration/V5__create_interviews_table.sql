-- ==============================================================================
-- V5 — Create Interviews Table
-- AI Mock Interview Platform
--
-- Module:  Module 2 — Interview Session Management
-- Date:    2026-07-09
-- ==============================================================================

CREATE TABLE interviews
(
    id                         UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    candidate_id               UUID                     NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    template_id                UUID                     REFERENCES interview_templates (id) ON DELETE SET NULL,
    state                      VARCHAR(50)              NOT NULL DEFAULT 'CREATED',
    domain                     VARCHAR(255)             NOT NULL,
    role_level                 VARCHAR(50)              NOT NULL,
    total_questions            INTEGER                  NOT NULL DEFAULT 10,
    current_question_number    INTEGER                  NOT NULL DEFAULT 0,
    current_difficulty         VARCHAR(50)              NOT NULL DEFAULT 'MEDIUM',
    interview_context          JSONB                    NOT NULL DEFAULT '{"turns":[],"topicsDiscussed":[],"averageScore":0}',
    running_technical_score    DECIMAL(5, 2)            NOT NULL DEFAULT 0.00,
    running_english_score      DECIMAL(5, 2)            NOT NULL DEFAULT 0.00,
    running_behavioral_score   DECIMAL(5, 2)            NOT NULL DEFAULT 0.00,
    running_composite_score    DECIMAL(5, 2)            NOT NULL DEFAULT 0.00,
    started_at                 TIMESTAMP WITH TIME ZONE,
    completed_at               TIMESTAMP WITH TIME ZONE,
    created_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_interview_state           CHECK (state IN (
        'CREATED','CONFIGURED','STARTED','QUESTION_GENERATED','QUESTION_DELIVERED',
        'WAITING_FOR_RESPONSE','LISTENING','TRANSCRIBING','EVALUATING','AGGREGATING',
        'GENERATING_NEXT_QUESTION','COMPLETED','REPORT_GENERATING','REPORT_GENERATED',
        'ABANDONED','ERROR'
    )),
    CONSTRAINT chk_interview_role_level      CHECK (role_level IN ('JUNIOR','MID','SENIOR','LEAD','PRINCIPAL')),
    CONSTRAINT chk_interview_difficulty      CHECK (current_difficulty IN ('EASY','MEDIUM','HARD','EXPERT')),
    CONSTRAINT chk_interview_question_number CHECK (current_question_number >= 0),
    CONSTRAINT chk_interview_scores          CHECK (
        running_technical_score  BETWEEN 0 AND 100 AND
        running_english_score    BETWEEN 0 AND 100 AND
        running_behavioral_score BETWEEN 0 AND 100 AND
        running_composite_score  BETWEEN 0 AND 100
    )
);

CREATE INDEX idx_interviews_candidate_id ON interviews (candidate_id);
CREATE INDEX idx_interviews_state        ON interviews (state);
CREATE INDEX idx_interviews_created_at   ON interviews (created_at DESC);
