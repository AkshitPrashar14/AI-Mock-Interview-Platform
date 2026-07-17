package com.interviewplatform.agents.interview;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Result from the Interview Agent containing the generated question.
 *
 * <p><b>Module:</b> Module 6 — Interview Agent</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGenerationResult {

    private UUID interviewId;
    private int questionNumber;
    
    @NotNull
    private String questionText;
    @NotNull
    private String questionType;
    @NotNull
    private String difficulty;
    @NotNull
    private String rationale;
    @NotNull
    private List<String> expectedKeyPoints;
    private boolean success;
    private String errorMessage;
}
