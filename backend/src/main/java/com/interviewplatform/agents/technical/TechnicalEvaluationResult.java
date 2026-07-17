package com.interviewplatform.agents.technical;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalEvaluationResult {

    @Min(0) @Max(40)
    private int correctnessScore;    // 0–40
    
    @Min(0) @Max(30)
    private int depthScore;          // 0–30
    
    @Min(0) @Max(20)
    private int problemSolvingScore; // 0–20
    
    @Min(0) @Max(10)
    private int completenessScore;   // 0–10
    
    @Min(0) @Max(100)
    private int totalScore;          // 0–100

    @NotNull
    private List<String> strengths;
    @NotNull
    private List<String> improvements;
    @NotNull
    private String summary;

    private boolean success;
    private String errorMessage;
}
