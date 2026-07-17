package com.interviewplatform.agents.aggregator;

import com.interviewplatform.agents.common.AgentExecutionResult;
import com.interviewplatform.agents.technical.TechnicalEvaluationResult;
import com.interviewplatform.agents.english.EnglishEvaluationResult;
import com.interviewplatform.agents.behavioral.BehavioralEvaluationResult;
import com.interviewplatform.ai.provider.AgentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EvaluationAggregator}.
 *
 * <p><b>Module:</b> Module 10 — Parallel Evaluation Aggregator</p>
 */
@DisplayName("EvaluationAggregator")
class EvaluationAggregatorTest {

    private final EvaluationAggregator aggregator = new EvaluationAggregator();

    private static AgentExecutionResult<TechnicalEvaluationResult> techSuccess(int score) {
        TechnicalEvaluationResult res = new TechnicalEvaluationResult();
        res.setTotalScore(score);
        res.setSummary("Tech summary");
        return AgentExecutionResult.<TechnicalEvaluationResult>builder()
                .agentType(AgentType.TECHNICAL)
                .success(true)
                .result(res)
                .build();
    }

    private static AgentExecutionResult<EnglishEvaluationResult> engSuccess(int score) {
        EnglishEvaluationResult res = new EnglishEvaluationResult();
        res.setTotalScore(score);
        res.setSummary("Eng summary");
        return AgentExecutionResult.<EnglishEvaluationResult>builder()
                .agentType(AgentType.ENGLISH)
                .success(true)
                .result(res)
                .build();
    }

    private static AgentExecutionResult<BehavioralEvaluationResult> behSuccess(int score) {
        BehavioralEvaluationResult res = new BehavioralEvaluationResult();
        res.setTotalScore(score);
        res.setSummary("Beh summary");
        return AgentExecutionResult.<BehavioralEvaluationResult>builder()
                .agentType(AgentType.BEHAVIORAL)
                .success(true)
                .result(res)
                .build();
    }

    private static <T> AgentExecutionResult<T> failure(AgentType type) {
        return AgentExecutionResult.<T>builder()
                .agentType(type)
                .success(false)
                .errorMessage("Agent failed")
                .build();
    }

    @Nested
    @DisplayName("Composite score calculation")
    class CompositeScoreTests {

        @Test
        @DisplayName("tech=80 eng=80 beh=80 → composite=80 (EXCELLENT)")
        void allScores80() {
            AggregatedEvaluation result = aggregator.aggregate(
                    techSuccess(80), engSuccess(80), behSuccess(80));

            assertThat(result.getCompositeScore()).isEqualTo(80);
            assertThat(result.getPerformanceTier()).isEqualTo("EXCELLENT");
        }

        @Test
        @DisplayName("tech=100 eng=100 beh=100 → composite=100")
        void allScores100() {
            AggregatedEvaluation result = aggregator.aggregate(
                    techSuccess(100), engSuccess(100), behSuccess(100));

            assertThat(result.getCompositeScore()).isEqualTo(100);
        }

        @Test
        @DisplayName("tech=60 eng=40 beh=40 → composite=50 (DEVELOPING)")
        void mixedScores() {
            // 60*0.5 + 40*0.25 + 40*0.25 = 30 + 10 + 10 = 50
            AggregatedEvaluation result = aggregator.aggregate(
                    techSuccess(60), engSuccess(40), behSuccess(40));

            assertThat(result.getCompositeScore()).isEqualTo(50);
            assertThat(result.getPerformanceTier()).isEqualTo("DEVELOPING");
        }

        @Test
        @DisplayName("tech=0 eng=0 beh=0 → composite=0 (NEEDS_WORK)")
        void allZeros() {
            AggregatedEvaluation result = aggregator.aggregate(
                    techSuccess(0), engSuccess(0), behSuccess(0));

            assertThat(result.getCompositeScore()).isEqualTo(0);
            assertThat(result.getPerformanceTier()).isEqualTo("NEEDS_WORK");
        }
    }

    @Nested
    @DisplayName("Agent failure handling")
    class AgentFailureTests {

        @Test
        @DisplayName("Failed agent uses neutral score 50 — allAgentsSucceeded=false")
        void techAgentFails() {
            AggregatedEvaluation result = aggregator.aggregate(
                    failure(AgentType.TECHNICAL), engSuccess(80), behSuccess(80));

            // tech=50(neutral)*0.5 + eng=80*0.25 + beh=80*0.25 = 25 + 20 + 20 = 65
            assertThat(result.getCompositeScore()).isEqualTo(65);
            assertThat(result.isAllAgentsSucceeded()).isFalse();
        }

        @Test
        @DisplayName("All agents fail — uses all neutral scores (50)")
        void allAgentsFail() {
            AggregatedEvaluation result = aggregator.aggregate(
                    failure(AgentType.TECHNICAL), failure(AgentType.ENGLISH), failure(AgentType.BEHAVIORAL));

            assertThat(result.getCompositeScore()).isEqualTo(50);
            assertThat(result.isAllAgentsSucceeded()).isFalse();
        }
    }

    @Nested
    @DisplayName("Score population")
    class ScorePopulationTests {

        @Test
        @DisplayName("Individual scores are preserved in the result")
        void scoresPreserved() {
            AggregatedEvaluation result = aggregator.aggregate(
                    techSuccess(75), engSuccess(65), behSuccess(55));

            assertThat(result.getTechnicalScore()).isEqualTo(75);
            assertThat(result.getEnglishScore()).isEqualTo(65);
            assertThat(result.getBehavioralScore()).isEqualTo(55);
        }
    }
}
