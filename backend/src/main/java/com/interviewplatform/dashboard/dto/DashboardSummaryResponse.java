package com.interviewplatform.dashboard.dto;

import com.interviewplatform.analytics.dto.AnalyticsSnapshotResponse;
import com.interviewplatform.interview.dto.response.InterviewSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Composite response for the main dashboard view.
 *
 * <p><b>Module:</b> Module 12 — Dashboard APIs</p>
 */
@Data
@Builder
@Schema(description = "Composite response for the main candidate dashboard")
public class DashboardSummaryResponse {

    @Schema(description = "Overall analytics snapshot for the candidate")
    private AnalyticsSnapshotResponse analytics;

    @Schema(description = "Active or in-progress interviews")
    private List<InterviewSummaryResponse> activeInterviews;

    @Schema(description = "Recently completed interviews (limit 5)")
    private List<InterviewSummaryResponse> recentInterviews;
}
