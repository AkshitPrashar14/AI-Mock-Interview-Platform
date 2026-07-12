package com.interviewplatform.agents.behavioral;

import lombok.Builder;
import lombok.Data;

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
public class BehavioralEvaluationResult {

    private int confidenceScore;      // 0–25
    private int leadershipScore;      // 0–20
    private int ownershipScore;       // 0–20
    private int decisionMakingScore;  // 0–20
    private int professionalismScore; // 0–15

    /** Whether the candidate's answer covered all 4 STAR elements. */
    private boolean starComplete;

    private int totalScore;           // 0–100

    private List<String> strengths;
    private List<String> improvements;
    private String summary;

    private boolean success;
    private String errorMessage;
}
