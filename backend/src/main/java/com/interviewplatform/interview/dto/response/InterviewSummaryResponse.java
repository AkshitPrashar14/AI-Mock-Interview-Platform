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
 * Condensed interview summary for list views (dashboard, history).
 */
@Data
@Builder
@Schema(description = "Condensed interview summary for list views")
public class InterviewSummaryResponse {

    private UUID interviewId;
    private String domain;
    private RoleLevel roleLevel;
    private InterviewState state;
    private Integer totalQuestions;
    private Integer currentQuestionNumber;
    private DifficultyLevel currentDifficulty;
    private BigDecimal runningCompositeScore;
    private Instant startedAt;
    private Instant completedAt;
    private Instant createdAt;
}
