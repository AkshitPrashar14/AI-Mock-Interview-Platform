package com.interviewplatform.orchestrator;

import com.interviewplatform.interview.entity.DifficultyLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptive difficulty manager for the interview session.
 *
 * <h3>Algorithm</h3>
 * <ul>
 *   <li>Last 2 scores &ge; 80 → increase difficulty by one level</li>
 *   <li>Last 2 scores &le; 40 → decrease difficulty by one level</li>
 *   <li>Otherwise → maintain current difficulty</li>
 * </ul>
 *
 * <p>A minimum of 2 completed answers is required before any adjustment
 * is made — fewer answers always return the current difficulty unchanged.</p>
 *
 * <p><b>Module:</b> Module 3 — Interview Orchestrator</p>
 */
@Slf4j
@Component
public class DifficultyManager {

    private static final int WINDOW_SIZE   = 2;
    private static final int INCREASE_THRESHOLD = 80;
    private static final int DECREASE_THRESHOLD = 40;

    /**
     * Calculates the next difficulty level based on the candidate's recent scores.
     *
     * @param current      current difficulty level of the interview
     * @param recentScores ordered list of composite scores (0–100) for recent answers
     * @return the adjusted difficulty level (may be unchanged)
     */
    public DifficultyLevel nextDifficulty(DifficultyLevel current, List<Integer> recentScores) {
        if (recentScores == null || recentScores.size() < WINDOW_SIZE) {
            log.debug("DifficultyManager: insufficient data ({} scores), maintaining {}", 
                      recentScores == null ? 0 : recentScores.size(), current);
            return current;
        }

        // Take the last WINDOW_SIZE scores
        List<Integer> window = recentScores.subList(
                Math.max(0, recentScores.size() - WINDOW_SIZE),
                recentScores.size()
        );

        boolean allHigh = window.stream().allMatch(s -> s >= INCREASE_THRESHOLD);
        boolean allLow  = window.stream().allMatch(s -> s <= DECREASE_THRESHOLD);

        if (allHigh) {
            DifficultyLevel next = current.increase();
            log.info("DifficultyManager: scores {} → increasing {} → {}", window, current, next);
            return next;
        }

        if (allLow) {
            DifficultyLevel next = current.decrease();
            log.info("DifficultyManager: scores {} → decreasing {} → {}", window, current, next);
            return next;
        }

        log.debug("DifficultyManager: scores {} → maintaining {}", window, current);
        return current;
    }
}
