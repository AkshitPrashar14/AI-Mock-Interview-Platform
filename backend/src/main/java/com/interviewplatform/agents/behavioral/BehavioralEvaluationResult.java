package com.interviewplatform.agents.behavioral;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Typed result from the Behavioral Evaluation Agent.
 *
 * <p>Subscores: confidence(25%) + leadership(20%) + ownership(20%) + decisionMaking(20%) + professionalism(15%)</p>
 *
 * <p><b>Module:</b> Module 9 — Behavioral Evaluation Agent</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehavioralEvaluationResult {

    @Min(0) @Max(25)
    private int confidenceScore;      // 0–25
    @Min(0) @Max(20)
    private int leadershipScore;      // 0–20
    @Min(0) @Max(20)
    private int ownershipScore;       // 0–20
    @Min(0) @Max(20)
    private int decisionMakingScore;  // 0–20
    @Min(0) @Max(15)
    private int professionalismScore; // 0–15

    /** Whether the candidate's answer covered all 4 STAR elements. */
    private boolean starComplete;

    @Min(0) @Max(100)
    private int totalScore;           // 0–100

    @NotNull
    private List<String> strengths;
    @NotNull
    private List<String> improvements;
    @NotNull
    private String summary;

    private boolean success;
    private String errorMessage;
}
