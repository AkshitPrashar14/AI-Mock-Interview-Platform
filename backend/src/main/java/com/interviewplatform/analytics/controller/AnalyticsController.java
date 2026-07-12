package com.interviewplatform.analytics.controller;

import com.interviewplatform.analytics.dto.AnalyticsSnapshotResponse;
import com.interviewplatform.analytics.entity.AnalyticsSnapshot;
import com.interviewplatform.analytics.service.AnalyticsService;
import com.interviewplatform.common.response.ApiResponse;
import com.interviewplatform.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for retrieving analytics snapshots.
 *
 * <p>Base path: {@code /api/v1/analytics}</p>
 *
 * <p><b>Module:</b> Module 12 — Dashboard APIs</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Candidate analytics snapshot retrieval")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/snapshot")
    @Operation(
        summary = "Get analytics snapshot",
        description = "Returns the pre-computed analytics snapshot for the current candidate."
    )
    public ApiResponse<AnalyticsSnapshotResponse> getSnapshot(@AuthenticationPrincipal User principal) {
        log.debug("AnalyticsController.getSnapshot: userId={}", principal.getId());
        AnalyticsSnapshot snapshot = analyticsService.getSnapshot(principal.getId());
        
        AnalyticsSnapshotResponse response = AnalyticsSnapshotResponse.builder()
                .totalInterviews(snapshot.getTotalInterviews())
                .avgTechnicalScore(snapshot.getAvgTechnicalScore())
                .avgEnglishScore(snapshot.getAvgEnglishScore())
                .avgBehavioralScore(snapshot.getAvgBehavioralScore())
                .avgCompositeScore(snapshot.getAvgCompositeScore())
                .bestPerformanceTier(snapshot.getBestPerformanceTier())
                .mostRecentVerdict(snapshot.getMostRecentVerdict())
                .mostPracticedDomain(snapshot.getMostPracticedDomain())
                .lastComputedAt(snapshot.getLastComputedAt())
                .build();
                
        return ApiResponse.success(response);
    }
}
