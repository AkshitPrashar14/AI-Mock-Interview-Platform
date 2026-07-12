package com.interviewplatform.orchestrator;

import com.interviewplatform.interview.entity.DifficultyLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DifficultyManager}.
 *
 * <p><b>Module:</b> Module 3 — Interview Orchestrator</p>
 */
@DisplayName("DifficultyManager")
class DifficultyManagerTest {

    private final DifficultyManager manager = new DifficultyManager();

    // =========================================================================
    // Increase cases
    // =========================================================================

    @Nested
    @DisplayName("Difficulty increase")
    class IncreaseTests {

        @Test
        @DisplayName("EASY → MEDIUM when last 2 scores ≥ 80")
        void easyIncreasesToMedium() {
            DifficultyLevel result = manager.nextDifficulty(DifficultyLevel.EASY, List.of(85, 90));
            assertThat(result).isEqualTo(DifficultyLevel.MEDIUM);
        }

        @Test
        @DisplayName("MEDIUM → HARD when last 2 scores ≥ 80")
        void mediumIncreasesToHard() {
            DifficultyLevel result = manager.nextDifficulty(DifficultyLevel.MEDIUM, List.of(80, 88));
            assertThat(result).isEqualTo(DifficultyLevel.HARD);
        }

        @Test
        @DisplayName("EXPERT stays at EXPERT even when both scores ≥ 80")
        void expertStaysAtExpert() {
            DifficultyLevel result = manager.nextDifficulty(DifficultyLevel.EXPERT, List.of(95, 100));
            assertThat(result).isEqualTo(DifficultyLevel.EXPERT);
        }

        @Test
        @DisplayName("Considers only the last 2 scores in a longer list")
        void usesWindowOfLastTwo() {
            // Scores: [20, 20, 20, 90, 90] — last 2 are both ≥ 80 → increase
            DifficultyLevel result = manager.nextDifficulty(DifficultyLevel.EASY, List.of(20, 20, 20, 90, 90));
            assertThat(result).isEqualTo(DifficultyLevel.MEDIUM);
        }
    }

    // =========================================================================
    // Decrease cases
    // =========================================================================

    @Nested
    @DisplayName("Difficulty decrease")
    class DecreaseTests {

        @Test
        @DisplayName("HARD → MEDIUM when last 2 scores ≤ 40")
        void hardDecreasesToMedium() {
            DifficultyLevel result = manager.nextDifficulty(DifficultyLevel.HARD, List.of(30, 40));
            assertThat(result).isEqualTo(DifficultyLevel.MEDIUM);
        }

        @Test
        @DisplayName("EASY stays at EASY even when both scores ≤ 40")
        void easyStaysAtEasy() {
            DifficultyLevel result = manager.nextDifficulty(DifficultyLevel.EASY, List.of(10, 20));
            assertThat(result).isEqualTo(DifficultyLevel.EASY);
        }
    }

    // =========================================================================
    // Maintain cases
    // =========================================================================

    @Nested
    @DisplayName("Difficulty maintained")
    class MaintainTests {

        @Test
        @DisplayName("Mixed scores (one high, one low) → maintain current")
        void mixedScoresMaintains() {
            DifficultyLevel result = manager.nextDifficulty(DifficultyLevel.MEDIUM, List.of(85, 35));
            assertThat(result).isEqualTo(DifficultyLevel.MEDIUM);
        }

        @Test
        @DisplayName("Scores at 79 and 41 → maintain (not in increase or decrease range)")
        void borderlineScoresMaintain() {
            DifficultyLevel result = manager.nextDifficulty(DifficultyLevel.MEDIUM, List.of(79, 41));
            assertThat(result).isEqualTo(DifficultyLevel.MEDIUM);
        }

        @Test
        @DisplayName("Fewer than 2 scores → maintain current")
        void insufficientScoresMaintains() {
            DifficultyLevel result = manager.nextDifficulty(DifficultyLevel.HARD, List.of(95));
            assertThat(result).isEqualTo(DifficultyLevel.HARD);
        }

        @Test
        @DisplayName("Empty score list → maintain current")
        void emptyScoreListMaintains() {
            DifficultyLevel result = manager.nextDifficulty(DifficultyLevel.MEDIUM, List.of());
            assertThat(result).isEqualTo(DifficultyLevel.MEDIUM);
        }

        @Test
        @DisplayName("Null score list → maintain current")
        void nullScoreListMaintains() {
            DifficultyLevel result = manager.nextDifficulty(DifficultyLevel.EXPERT, null);
            assertThat(result).isEqualTo(DifficultyLevel.EXPERT);
        }
    }
}
