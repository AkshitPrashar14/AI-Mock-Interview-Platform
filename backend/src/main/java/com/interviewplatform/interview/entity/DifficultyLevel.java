package com.interviewplatform.interview.entity;

/**
 * Difficulty level for interview questions.
 *
 * <p>Used by the Interview Agent to calibrate question complexity and by the
 * {@link com.interviewplatform.orchestrator.DifficultyManager} to adapt difficulty
 * based on the candidate's running score trend.</p>
 */
public enum DifficultyLevel {

    /** Fundamental definitions, basic concepts — suitable for warm-up or weak performance. */
    EASY,

    /** Applied knowledge, common patterns — the default starting difficulty. */
    MEDIUM,

    /** Advanced trade-offs, complex scenarios — triggered by consistently strong performance. */
    HARD,

    /** System design, architectural decisions, edge cases — expert-level challenge. */
    EXPERT;

    /**
     * Returns the next difficulty level up from this one.
     * Returns {@code EXPERT} if already at maximum.
     */
    public DifficultyLevel increase() {
        return switch (this) {
            case EASY   -> MEDIUM;
            case MEDIUM -> HARD;
            case HARD, EXPERT -> EXPERT;
        };
    }

    /**
     * Returns the next difficulty level down from this one.
     * Returns {@code EASY} if already at minimum.
     */
    public DifficultyLevel decrease() {
        return switch (this) {
            case EXPERT -> HARD;
            case HARD   -> MEDIUM;
            case MEDIUM, EASY -> EASY;
        };
    }
}
