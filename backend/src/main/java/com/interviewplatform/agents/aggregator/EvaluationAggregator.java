package com.interviewplatform.agents.aggregator;

import com.interviewplatform.agents.common.AgentResult;
import com.interviewplatform.interview.entity.PerformanceTier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Pure-Java evaluation score aggregator — no LLM involved.
 *
 * <h3>Formula</h3>
 * <pre>
 * composite = (technicalScore * 0.50)
 *           + (englishScore   * 0.25)
 *           + (behavioralScore * 0.25)
 * </pre>
 *
 * <h3>Performance Tiers</h3>
 * <ul>
 *   <li>EXCELLENT ≥ 80</li>
 *   <li>PROFICIENT 60–79</li>
 *   <li>DEVELOPING 40–59</li>
 *   <li>NEEDS_WORK &lt; 40</li>
 * </ul>
 *
 * <p><b>Module:</b> Module 10 — Parallel Evaluation Aggregator</p>
 */
@Slf4j
@Component
public class EvaluationAggregator {

    /** Weight for the technical score component. */
    private static final double TECH_WEIGHT      = 0.50;
    /** Weight for the English communication score component. */
    private static final double ENGLISH_WEIGHT   = 0.25;
    /** Weight for the behavioral score component. */
    private static final double BEHAVIORAL_WEIGHT = 0.25;

    /**
     * Aggregates individual agent results into a composite evaluation.
     *
     * @param technicalResult  result from TechnicalAgent
     * @param englishResult    result from EnglishAgent
     * @param behavioralResult result from BehavioralAgent
     * @return aggregated evaluation with composite score and performance tier
     */
    public AggregatedEvaluation aggregate(AgentResult technicalResult,
                                           AgentResult englishResult,
                                           AgentResult behavioralResult) {

        int techScore  = technicalResult.isSuccess()  ? technicalResult.getScore()  : 50;
        int engScore   = englishResult.isSuccess()    ? englishResult.getScore()    : 50;
        int behScore   = behavioralResult.isSuccess() ? behavioralResult.getScore() : 50;

        int composite = (int) Math.round(
                (techScore  * TECH_WEIGHT)
              + (engScore   * ENGLISH_WEIGHT)
              + (behScore   * BEHAVIORAL_WEIGHT)
        );

        // Clamp to 0–100
        composite = Math.max(0, Math.min(100, composite));

        PerformanceTier tier = PerformanceTier.fromScore(composite);
        boolean allSucceeded = technicalResult.isSuccess()
                            && englishResult.isSuccess()
                            && behavioralResult.isSuccess();

        log.info("EvaluationAggregator: tech={}, eng={}, beh={} → composite={} tier={}",
                techScore, engScore, behScore, composite, tier);

        return AggregatedEvaluation.builder()
                .technicalScore(techScore)
                .englishScore(engScore)
                .behavioralScore(behScore)
                .compositeScore(composite)
                .performanceTier(tier.name())
                .technicalSummary(technicalResult.getSummary())
                .englishSummary(englishResult.getSummary())
                .behavioralSummary(behavioralResult.getSummary())
                .allAgentsSucceeded(allSucceeded)
                .build();
    }
}
