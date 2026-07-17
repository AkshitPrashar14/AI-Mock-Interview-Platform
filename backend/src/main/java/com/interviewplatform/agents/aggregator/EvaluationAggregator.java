package com.interviewplatform.agents.aggregator;

import com.interviewplatform.agents.common.AgentExecutionResult;
import com.interviewplatform.agents.technical.TechnicalEvaluationResult;
import com.interviewplatform.agents.english.EnglishEvaluationResult;
import com.interviewplatform.agents.behavioral.BehavioralEvaluationResult;
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
    public AggregatedEvaluation aggregate(AgentExecutionResult<?> technicalResult,
                                           AgentExecutionResult<?> englishResult,
                                           AgentExecutionResult<?> behavioralResult) {

        int techScore  = 50;
        String techSummary = "Evaluation failed";
        if (technicalResult.isSuccess() && technicalResult.getResult() instanceof TechnicalEvaluationResult t) {
            techScore = t.getTotalScore();
            techSummary = t.getSummary();
        }

        int engScore   = 50;
        String engSummary = "Evaluation failed";
        if (englishResult.isSuccess() && englishResult.getResult() instanceof EnglishEvaluationResult e) {
            engScore = e.getTotalScore();
            engSummary = e.getSummary();
        }

        int behScore   = 50;
        String behSummary = "Evaluation failed";
        if (behavioralResult.isSuccess() && behavioralResult.getResult() instanceof BehavioralEvaluationResult b) {
            behScore = b.getTotalScore();
            behSummary = b.getSummary();
        }

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
                .technicalSummary(techSummary)
                .englishSummary(engSummary)
                .behavioralSummary(behSummary)
                .allAgentsSucceeded(allSucceeded)
                .build();
    }
}
