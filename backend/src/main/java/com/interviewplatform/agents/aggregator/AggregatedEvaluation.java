package com.interviewplatform.agents.aggregator;

import lombok.Builder;
import lombok.Data;

/**
 * Result of the parallel evaluation aggregation.
 *
 * <p>Composite score formula: {@code tech*0.50 + english*0.25 + behavioral*0.25}</p>
 *
 * <p><b>Module:</b> Module 10 — Parallel Evaluation Aggregator</p>
 */
@Data
@Builder
public class AggregatedEvaluation {

    private int technicalScore;
    private int englishScore;
    private int behavioralScore;

    /** Composite score: tech*0.50 + english*0.25 + behavioral*0.25 */
    private int compositeScore;

    /** Performance tier derived from the composite score. */
    private String performanceTier;

    private String technicalSummary;
    private String englishSummary;
    private String behavioralSummary;

    /** Whether all 3 agent calls succeeded. */
    private boolean allAgentsSucceeded;
}
