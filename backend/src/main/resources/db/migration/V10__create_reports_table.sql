-- ==============================================================================
-- V10 — Create Reports Table
-- AI Mock Interview Platform
--
-- Module:  Module 2 — Interview Session Management
-- Date:    2026-07-09
-- ==============================================================================

CREATE TABLE reports
(
    id                      UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    interview_id            UUID                     NOT NULL REFERENCES interviews (id) ON DELETE CASCADE,

    -- Final aggregated scores
    final_technical_score   DECIMAL(5, 2),
    final_english_score     DECIMAL(5, 2),
    final_behavioral_score  DECIMAL(5, 2),
    final_composite_score   DECIMAL(5, 2),
    final_tier              VARCHAR(50),
    verdict                 VARCHAR(50),

    -- LLM-generated narrative
    executive_summary       TEXT,
    strength_highlights     JSONB                    DEFAULT '[]',
    improvement_areas       JSONB                    DEFAULT '[]',
    study_plan              JSONB                    DEFAULT '[]',
    interviewer_notes       TEXT,

    report_status           VARCHAR(50)              NOT NULL DEFAULT 'GENERATING',
    generated_at            TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_report_tier           CHECK (final_tier IS NULL OR final_tier IN ('NEEDS_WORK','DEVELOPING','PROFICIENT','EXCELLENT')),
    CONSTRAINT chk_report_verdict        CHECK (verdict IS NULL OR verdict IN ('STRONGLY_CONSIDER','CONSIDER','FURTHER_ROUNDS','NOT_RECOMMENDED')),
    CONSTRAINT chk_report_status         CHECK (report_status IN ('GENERATING','READY','FAILED')),
    UNIQUE (interview_id)
);

CREATE INDEX idx_reports_interview_id ON reports (interview_id);
CREATE INDEX idx_reports_status       ON reports (report_status);
