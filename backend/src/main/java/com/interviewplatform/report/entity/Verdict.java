package com.interviewplatform.report.entity;

/**
 * Hire recommendation verdict — computed deterministically by the Report Compiler (pure Java, no LLM).
 *
 * <p>Verdict thresholds:</p>
 * <ul>
 *   <li>{@code STRONGLY_CONSIDER} — composite score ≥ 80</li>
 *   <li>{@code CONSIDER}          — composite score 60–79</li>
 *   <li>{@code FURTHER_ROUNDS}    — composite score 40–59</li>
 *   <li>{@code NOT_RECOMMENDED}   — composite score &lt; 40</li>
 * </ul>
 */
public enum Verdict {

    STRONGLY_CONSIDER,
    CONSIDER,
    FURTHER_ROUNDS,
    NOT_RECOMMENDED;

    /**
     * Derives the hire verdict from a final composite score.
     *
     * @param compositeScore final weighted composite score (0–100)
     * @return corresponding verdict
     */
    public static Verdict fromScore(double compositeScore) {
        if (compositeScore >= 80) return STRONGLY_CONSIDER;
        if (compositeScore >= 60) return CONSIDER;
        if (compositeScore >= 40) return FURTHER_ROUNDS;
        return NOT_RECOMMENDED;
    }
}
