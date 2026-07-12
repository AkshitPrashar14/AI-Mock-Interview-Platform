package com.interviewplatform.agents.english;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Typed result from the English Communication Agent.
 *
 * <p>Subscores: grammar(25%) + vocabulary(25%) + fluency(25%) + professional(15%) + fillerPenalty(10%)</p>
 *
 * <p><b>Module:</b> Module 8 — English Communication Agent</p>
 */
@Data
@Builder
public class EnglishEvaluationResult {

    private int grammarScore;           // 0–25
    private int vocabularyScore;        // 0–25
    private int fluencyScore;           // 0–25
    private int professionalToneScore;  // 0–15
    private int fillerWordPenalty;      // remaining points 0–10
    private int fillerWordCount;        // raw filler word count detected in Java

    private int totalScore;             // 0–100

    private List<String> strengths;
    private List<String> improvements;
    private String summary;

    private boolean success;
    private String errorMessage;
}
