-- ==============================================================================
-- V8 — Create Answers Table
-- AI Mock Interview Platform
--
-- Module:  Module 2 — Interview Session Management
-- Date:    2026-07-09
-- ==============================================================================

CREATE TABLE answers
(
    id                      UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    interview_id            UUID                     NOT NULL REFERENCES interviews (id) ON DELETE CASCADE,
    question_id             UUID                     NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    audio_file_path         VARCHAR(1000),
    audio_format            VARCHAR(20)              DEFAULT 'WEBM',
    audio_duration_seconds  INTEGER,
    transcript              TEXT,
    transcript_status       VARCHAR(50)              NOT NULL DEFAULT 'PENDING',
    stt_confidence          DECIMAL(5, 4),
    retry_count             INTEGER                  NOT NULL DEFAULT 0,
    recorded_at             TIMESTAMP WITH TIME ZONE,
    transcribed_at          TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_answer_audio_format       CHECK (audio_format IN ('WAV','WEBM','MP3','OGG')),
    CONSTRAINT chk_answer_transcript_status  CHECK (transcript_status IN ('PENDING','VALID','INVALID','SKIPPED')),
    CONSTRAINT chk_answer_stt_confidence     CHECK (stt_confidence IS NULL OR stt_confidence BETWEEN 0 AND 1),
    CONSTRAINT chk_answer_retry_count        CHECK (retry_count >= 0),
    UNIQUE (interview_id, question_id)
);

CREATE INDEX idx_answers_interview_question ON answers (interview_id, question_id);
