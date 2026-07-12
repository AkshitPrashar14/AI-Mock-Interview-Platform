package com.interviewplatform.agents.interview;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Result from the Interview Agent containing the generated question.
 *
 * <p><b>Module:</b> Module 6 — Interview Agent</p>
 */
@Data
@Builder
public class QuestionGenerationResult {

    private UUID interviewId;
    private int questionNumber;
    private String questionText;
    private String questionType;
    private String difficulty;
    private String rationale;
    private List<String> expectedKeyPoints;
    private boolean success;
    private String errorMessage;
}
