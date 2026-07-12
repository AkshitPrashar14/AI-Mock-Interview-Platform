-- ==============================================================================
-- V11 — Create Analytics Snapshots Table
-- AI Mock Interview Platform
--
-- Module:  Module 2 — Interview Session Management
-- Date:    2026-07-09
--
-- Pre-computed analytics per candidate, updated after each interview.
-- Enables fast dashboard queries without expensive aggregations at request time.
-- ==============================================================================

CREATE TABLE analytics_snapshots
(
    id                      UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    candidate_id            UUID                     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    total_interviews        INTEGER                  NOT NULL DEFAULT 0,
    avg_technical_score     DECIMAL(5, 2)            NOT NULL DEFAULT 0.00,
    avg_english_score       DECIMAL(5, 2)            NOT NULL DEFAULT 0.00,
    avg_behavioral_score    DECIMAL(5, 2)            NOT NULL DEFAULT 0.00,
    avg_composite_score     DECIMAL(5, 2)            NOT NULL DEFAULT 0.00,
    best_performance_tier   VARCHAR(50),
    most_recent_verdict     VARCHAR(50),
    most_practiced_domain   VARCHAR(255),
    last_computed_at        TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_snapshot_total_interviews  CHECK (total_interviews >= 0),
    UNIQUE (candidate_id)
);

CREATE INDEX idx_analytics_candidate_id ON analytics_snapshots (candidate_id);
