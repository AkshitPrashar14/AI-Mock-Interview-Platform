package com.interviewplatform.interview.entity;

/**
 * Performance tier computed by the Evaluation Aggregator (pure Java — no LLM).
 *
 * <p>Tier thresholds:</p>
 * <ul>
 *   <li>{@code NEEDS_WORK}  — composite score &lt; 40</li>
 *   <li>{@code DEVELOPING}  — composite score 40–59</li>
 *   <li>{@code PROFICIENT}  — composite score 60–79</li>
 *   <li>{@code EXCELLENT}   — composite score ≥ 80</li>
 * </ul>
 */
public enum PerformanceTier {

    NEEDS_WORK,
    DEVELOPING,
    PROFICIENT,
    EXCELLENT;

    /**
     * Derives the performance tier from a composite score (0–100).
     *
     * @param score composite score
     * @return corresponding tier
     */
    public static PerformanceTier fromScore(double score) {
        if (score >= 80) return EXCELLENT;
        if (score >= 60) return PROFICIENT;
        if (score >= 40) return DEVELOPING;
        return NEEDS_WORK;
    }
}
