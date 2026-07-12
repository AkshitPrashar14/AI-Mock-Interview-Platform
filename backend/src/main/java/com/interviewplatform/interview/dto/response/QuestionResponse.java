package com.interviewplatform.interview.dto.response;

import com.interviewplatform.interview.entity.DifficultyLevel;
import com.interviewplatform.interview.entity.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for a single interview question.
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@Data
@Builder
@Schema(description = "A single interview question")
public class QuestionResponse {

    @Schema(description = "Question ID")
    private UUID id;

    @Schema(description = "Sequential question number within the session (1-based)")
    private Integer questionNumber;

    @Schema(description = "Question text displayed to the candidate")
    private String questionText;

    @Schema(description = "Question type (TECHNICAL, BEHAVIORAL, SITUATIONAL)")
    private QuestionType questionType;

    @Schema(description = "Difficulty level at the time this question was generated")
    private DifficultyLevel difficulty;

    @Schema(description = "Whether the candidate has already answered this question")
    private boolean answered;

    @Schema(description = "When this question was created")
    private Instant createdAt;
}
