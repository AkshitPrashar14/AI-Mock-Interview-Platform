package com.interviewplatform.interview.dto.response;

import com.interviewplatform.interview.entity.DifficultyLevel;
import com.interviewplatform.interview.entity.InterviewState;
import com.interviewplatform.interview.entity.RoleLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Full interview session response.
 */
@Data
@Builder
@Schema(description = "Full interview session state")
public class InterviewResponse {

    @Schema(description = "Interview session ID")
    private UUID interviewId;

    @Schema(description = "Current state machine state")
    private InterviewState state;

    @Schema(description = "Interview domain")
    private String domain;

    @Schema(description = "Target role level")
    private RoleLevel roleLevel;

    @Schema(description = "Total number of questions in this session")
    private Integer totalQuestions;

    @Schema(description = "Current question number (0 = not started)")
    private Integer currentQuestionNumber;

    @Schema(description = "Current difficulty level")
    private DifficultyLevel currentDifficulty;

    @Schema(description = "Running composite score (0–100)")
    private BigDecimal runningCompositeScore;

    @Schema(description = "Running technical score")
    private BigDecimal runningTechnicalScore;

    @Schema(description = "Running English score")
    private BigDecimal runningEnglishScore;

    @Schema(description = "Running behavioral score")
    private BigDecimal runningBehavioralScore;

    @Schema(description = "When the interview was started")
    private Instant startedAt;

    @Schema(description = "When the interview was completed")
    private Instant completedAt;

    @Schema(description = "When the interview was created")
    private Instant createdAt;

    @Schema(description = "Current question text (if applicable)")
    private String currentQuestionText;

    @Schema(description = "Feedback for the previous question (if applicable)")
    private String lastEvaluationFeedback;
}
