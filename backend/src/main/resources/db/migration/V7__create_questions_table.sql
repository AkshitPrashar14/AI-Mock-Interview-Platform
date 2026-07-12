-- ==============================================================================
-- V7 — Create Questions Table
-- AI Mock Interview Platform
--
-- Module:  Module 2 — Interview Session Management
-- Date:    2026-07-09
-- ==============================================================================

CREATE TABLE questions
(
    id                  UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    interview_id        UUID                     NOT NULL REFERENCES interviews (id) ON DELETE CASCADE,
    question_number     INTEGER                  NOT NULL,
    question_text       TEXT                     NOT NULL,
    question_type       VARCHAR(50)              NOT NULL DEFAULT 'TECHNICAL',
    difficulty          VARCHAR(50)              NOT NULL DEFAULT 'MEDIUM',
    expected_key_points JSONB                    NOT NULL DEFAULT '[]',
    follow_up_hints     JSONB                    NOT NULL DEFAULT '[]',
    generated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    delivered_at        TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_question_type       CHECK (question_type IN ('TECHNICAL','BEHAVIORAL','SITUATIONAL')),
    CONSTRAINT chk_question_difficulty CHECK (difficulty IN ('EASY','MEDIUM','HARD','EXPERT')),
    CONSTRAINT chk_question_number     CHECK (question_number >= 1),
    UNIQUE (interview_id, question_number)
);

CREATE INDEX idx_questions_interview_id ON questions (interview_id, question_number);
