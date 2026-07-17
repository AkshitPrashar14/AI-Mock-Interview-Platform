-- ==============================================================================
-- V13 — Rename Evaluation Feedback to Summary
-- AI Mock Interview Platform
--
-- Date:    2026-07-17
-- ==============================================================================

ALTER TABLE evaluations RENAME COLUMN technical_feedback TO technical_summary;
ALTER TABLE evaluations RENAME COLUMN english_feedback TO english_summary;
ALTER TABLE evaluations RENAME COLUMN behavioral_feedback TO behavioral_summary;
