package com.interviewplatform.report.dto;

import com.interviewplatform.interview.entity.PerformanceTier;
import com.interviewplatform.report.entity.ReportStatus;
import com.interviewplatform.report.entity.Verdict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for a completed interview report.
 *
 * <p><b>Module:</b> Module 11 — Report Compiler</p>
 */
@Data
@Builder
@Schema(description = "Full interview assessment report")
public class ReportResponse {

    @Schema(description = "Report ID")
    private UUID reportId;

    @Schema(description = "Interview session ID this report belongs to")
    private UUID interviewId;

    // Scores
    @Schema(description = "Final technical score (0–100)")
    private BigDecimal finalTechnicalScore;

    @Schema(description = "Final English communication score (0–100)")
    private BigDecimal finalEnglishScore;

    @Schema(description = "Final behavioral score (0–100)")
    private BigDecimal finalBehavioralScore;

    @Schema(description = "Final weighted composite score (tech*50% + eng*25% + beh*25%)")
    private BigDecimal finalCompositeScore;

    @Schema(description = "Performance tier derived from composite score")
    private PerformanceTier finalTier;

    @Schema(description = "Hire recommendation verdict (pure Java — NOT LLM generated)")
    private Verdict verdict;

    // LLM-generated narrative
    @Schema(description = "Executive summary paragraph")
    private String executiveSummary;

    @Schema(description = "JSON array of strength highlights")
    private String strengthHighlights;

    @Schema(description = "JSON array of improvement areas")
    private String improvementAreas;

    @Schema(description = "JSON array of study plan recommendations")
    private String studyPlan;

    @Schema(description = "Report generation status")
    private ReportStatus reportStatus;

    @Schema(description = "When the report was generated")
    private Instant generatedAt;
}
