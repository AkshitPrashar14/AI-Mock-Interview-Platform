package com.interviewplatform.agents.aggregator;

import com.interviewplatform.agents.common.AgentResult;
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

    private static AgentResult success(String name, int score) {
        return AgentResult.success(name, null, score, name + " summary");
    }

    private static AgentResult failure(String name) {
        return AgentResult.failure(name, "Agent failed");
    }

    @Nested
    @DisplayName("Composite score calculation")
    class CompositeScoreTests {

        @Test
        @DisplayName("tech=80 eng=80 beh=80 → composite=80 (EXCELLENT)")
        void allScores80() {
            AggregatedEvaluation result = aggregator.aggregate(
                    success("Tech", 80), success("Eng", 80), success("Beh", 80));

            assertThat(result.getCompositeScore()).isEqualTo(80);
            assertThat(result.getPerformanceTier()).isEqualTo("EXCELLENT");
        }

        @Test
        @DisplayName("tech=100 eng=100 beh=100 → composite=100")
        void allScores100() {
            AggregatedEvaluation result = aggregator.aggregate(
                    success("Tech", 100), success("Eng", 100), success("Beh", 100));

            assertThat(result.getCompositeScore()).isEqualTo(100);
        }

        @Test
        @DisplayName("tech=60 eng=40 beh=40 → composite=50 (DEVELOPING)")
        void mixedScores() {
            // 60*0.5 + 40*0.25 + 40*0.25 = 30 + 10 + 10 = 50
            AggregatedEvaluation result = aggregator.aggregate(
                    success("Tech", 60), success("Eng", 40), success("Beh", 40));

            assertThat(result.getCompositeScore()).isEqualTo(50);
            assertThat(result.getPerformanceTier()).isEqualTo("DEVELOPING");
        }

        @Test
        @DisplayName("tech=0 eng=0 beh=0 → composite=0 (NEEDS_WORK)")
        void allZeros() {
            AggregatedEvaluation result = aggregator.aggregate(
                    success("Tech", 0), success("Eng", 0), success("Beh", 0));

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
                    failure("Tech"), success("Eng", 80), success("Beh", 80));

            // tech=50(neutral)*0.5 + eng=80*0.25 + beh=80*0.25 = 25 + 20 + 20 = 65
            assertThat(result.getCompositeScore()).isEqualTo(65);
            assertThat(result.isAllAgentsSucceeded()).isFalse();
        }

        @Test
        @DisplayName("All agents fail — uses all neutral scores (50)")
        void allAgentsFail() {
            AggregatedEvaluation result = aggregator.aggregate(
                    failure("Tech"), failure("Eng"), failure("Beh"));

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
                    success("Tech", 75), success("Eng", 65), success("Beh", 55));

            assertThat(result.getTechnicalScore()).isEqualTo(75);
            assertThat(result.getEnglishScore()).isEqualTo(65);
            assertThat(result.getBehavioralScore()).isEqualTo(55);
        }
    }
}
