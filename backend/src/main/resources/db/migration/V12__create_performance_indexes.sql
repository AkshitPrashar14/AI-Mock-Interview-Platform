-- ==============================================================================
-- V12 — Create Performance Indexes
-- AI Mock Interview Platform
--
-- Module:  Module 2 — Interview Session Management
-- Date:    2026-07-09
--
-- Additional composite and partial indexes beyond those in table creation scripts.
-- ==============================================================================

-- Interviews: candidate's active sessions (dashboard query)
CREATE INDEX idx_interviews_candidate_active
    ON interviews (candidate_id, created_at DESC)
    WHERE state NOT IN ('REPORT_GENERATED', 'ABANDONED', 'ERROR');

-- Interviews: completed sessions for analytics
CREATE INDEX idx_interviews_completed
    ON interviews (candidate_id, completed_at DESC)
    WHERE state = 'REPORT_GENERATED';

-- Questions: covering index for interview room queries
CREATE INDEX idx_questions_interview_ordered
    ON questions (interview_id, question_number ASC);

-- Answers: lookup by interview for report generation
CREATE INDEX idx_answers_interview_id
    ON answers (interview_id);

-- Evaluations: fetch all evaluations for a completed interview
CREATE INDEX idx_evaluations_interview_ordered
    ON evaluations (interview_id, evaluated_at ASC);

-- State history: most recent transition lookup
CREATE INDEX idx_state_history_interview_latest
    ON interview_state_history (interview_id, transitioned_at DESC);
