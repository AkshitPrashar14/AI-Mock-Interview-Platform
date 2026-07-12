package com.interviewplatform.agents.technical;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Typed result from the Technical Evaluation Agent.
 *
 * <p>Subscores sum to {@code totalScore}:
 * correctness(40%) + depth(30%) + problemSolving(20%) + completeness(10%)</p>
 *
 * <p><b>Module:</b> Module 7 — Technical Evaluation Agent</p>
 */
@Data
@Builder
public class TechnicalEvaluationResult {

    private int correctnessScore;    // 0–40
    private int depthScore;          // 0–30
    private int problemSolvingScore; // 0–20
    private int completenessScore;   // 0–10
    private int totalScore;          // 0–100

    private List<String> strengths;
    private List<String> improvements;
    private String summary;

    private boolean success;
    private String errorMessage;
}
