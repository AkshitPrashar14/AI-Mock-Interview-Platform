package com.interviewplatform.report.controller;

import com.interviewplatform.common.response.ApiResponse;
import com.interviewplatform.report.dto.ReportResponse;
import com.interviewplatform.report.entity.Report;
import com.interviewplatform.report.service.ReportService;
import com.interviewplatform.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for interview reports.
 *
 * <p>Base path: {@code /api/v1/reports}</p>
 *
 * <p><b>Module:</b> Module 11 — Report Compiler</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Interview assessment report retrieval")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    // =========================================================================
    // GET /api/v1/reports/{interviewId}
    // =========================================================================

    @GetMapping("/{interviewId}")
    @Operation(
        summary = "Get the report for an interview",
        description = "Returns the full assessment report for a completed interview. "
                    + "Returns 202 if the report is still generating."
    )
    public ApiResponse<ReportResponse> getReport(
            @PathVariable UUID interviewId,
            @AuthenticationPrincipal User principal) {

        Report report = reportService.getReport(interviewId, principal.getId());
        return ApiResponse.success(toResponse(report));
    }

    // =========================================================================
    // Private mapper
    // =========================================================================

    private ReportResponse toResponse(Report report) {
        return ReportResponse.builder()
                .reportId(report.getId())
                .interviewId(report.getInterview().getId())
                .finalTechnicalScore(report.getFinalTechnicalScore())
                .finalEnglishScore(report.getFinalEnglishScore())
                .finalBehavioralScore(report.getFinalBehavioralScore())
                .finalCompositeScore(report.getFinalCompositeScore())
                .finalTier(report.getFinalTier())
                .verdict(report.getVerdict())
                .executiveSummary(report.getExecutiveSummary())
                .strengthHighlights(report.getStrengthHighlights())
                .improvementAreas(report.getImprovementAreas())
                .studyPlan(report.getStudyPlan())
                .reportStatus(report.getReportStatus())
                .generatedAt(report.getGeneratedAt())
                .build();
    }
}
