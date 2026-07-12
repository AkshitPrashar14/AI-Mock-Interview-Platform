package com.interviewplatform.analytics.dto;

import com.interviewplatform.interview.entity.PerformanceTier;
import com.interviewplatform.report.entity.Verdict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO representing a candidate's aggregated analytics.
 *
 * <p><b>Module:</b> Module 12 — Dashboard APIs</p>
 */
@Data
@Builder
@Schema(description = "Aggregated analytics snapshot for a candidate")
public class AnalyticsSnapshotResponse {

    @Schema(description = "Total number of completed interviews")
    private Integer totalInterviews;

    @Schema(description = "Average technical score across all interviews")
    private BigDecimal avgTechnicalScore;

    @Schema(description = "Average English communication score across all interviews")
    private BigDecimal avgEnglishScore;

    @Schema(description = "Average behavioral score across all interviews")
    private BigDecimal avgBehavioralScore;

    @Schema(description = "Average composite score across all interviews")
    private BigDecimal avgCompositeScore;

    @Schema(description = "Best performance tier achieved")
    private PerformanceTier bestPerformanceTier;

    @Schema(description = "Verdict from the most recent interview")
    private Verdict mostRecentVerdict;

    @Schema(description = "Most frequently practiced domain")
    private String mostPracticedDomain;

    @Schema(description = "Timestamp when these analytics were last computed")
    private Instant lastComputedAt;
}
