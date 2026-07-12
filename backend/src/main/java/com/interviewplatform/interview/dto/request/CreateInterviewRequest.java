package com.interviewplatform.interview.dto.request;

import com.interviewplatform.interview.entity.DifficultyLevel;
import com.interviewplatform.interview.entity.RoleLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

/**
 * Request body for {@code POST /api/v1/interviews}.
 */
@Data
@Schema(description = "Request to create a new interview session")
public class CreateInterviewRequest {

    @Schema(description = "Optional template ID. If omitted, the other fields are used directly.")
    private UUID templateId;

    @NotBlank(message = "Domain is required (e.g. 'Java Backend', 'System Design')")
    @Size(min = 2, max = 255, message = "Domain must be between 2 and 255 characters")
    @Schema(description = "Interview domain", example = "Java Backend")
    private String domain;

    @NotNull(message = "Role level is required")
    @Schema(description = "Candidate's target seniority level")
    private RoleLevel roleLevel;

    @Min(value = 1, message = "Total questions must be at least 1")
    @Max(value = 20, message = "Total questions cannot exceed 20")
    @Schema(description = "Number of questions in this session", defaultValue = "10")
    private Integer totalQuestions = 10;

    @Min(value = 10, message = "Duration must be at least 10 minutes")
    @Max(value = 120, message = "Duration cannot exceed 120 minutes")
    @Schema(description = "Expected interview duration in minutes", defaultValue = "30")
    private Integer durationMinutes = 30;

    @Schema(description = "Starting difficulty level", defaultValue = "MEDIUM")
    private DifficultyLevel startingDifficulty = DifficultyLevel.MEDIUM;
}
