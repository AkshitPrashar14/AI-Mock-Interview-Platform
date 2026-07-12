package com.interviewplatform.dashboard.controller;

import com.interviewplatform.common.response.ApiResponse;
import com.interviewplatform.dashboard.dto.DashboardSummaryResponse;
import com.interviewplatform.dashboard.service.DashboardService;
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
 * REST controller for the candidate dashboard.
 *
 * <p>Base path: {@code /api/v1/dashboard}</p>
 *
 * <p><b>Module:</b> Module 12 — Dashboard APIs</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard summary retrieval")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(
        summary = "Get dashboard summary",
        description = "Returns the main dashboard summary including analytics, active interviews, and recent interviews."
    )
    public ApiResponse<DashboardSummaryResponse> getSummary(@AuthenticationPrincipal User principal) {
        log.debug("DashboardController.getSummary: userId={}", principal.getId());
        DashboardSummaryResponse response = dashboardService.getDashboardSummary(principal.getId());
        return ApiResponse.success(response);
    }
}
