package com.interviewplatform.agents.english;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor
@AllArgsConstructor
public class EnglishEvaluationResult {

    @Min(0) @Max(25)
    private int grammarScore;           // 0–25
    @Min(0) @Max(25)
    private int vocabularyScore;        // 0–25
    @Min(0) @Max(25)
    private int fluencyScore;           // 0–25
    @Min(0) @Max(15)
    private int professionalToneScore;  // 0–15
    @Min(0) @Max(10)
    private int fillerWordPenalty;      // remaining points 0–10
    private int fillerWordCount;        // raw filler word count detected in Java

    @Min(0) @Max(100)
    private int totalScore;             // 0–100

    @NotNull
    private List<String> strengths;
    @NotNull
    private List<String> improvements;
    @NotNull
    private String summary;

    private boolean success;
    private String errorMessage;
}
