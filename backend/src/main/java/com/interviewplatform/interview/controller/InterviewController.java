package com.interviewplatform.interview.controller;

import com.interviewplatform.common.response.ApiResponse;
import com.interviewplatform.interview.dto.request.CreateInterviewRequest;
import com.interviewplatform.interview.dto.response.InterviewResponse;
import com.interviewplatform.interview.dto.response.InterviewStartResponse;
import com.interviewplatform.interview.dto.response.InterviewSummaryResponse;
import com.interviewplatform.interview.entity.InterviewState;
import com.interviewplatform.interview.service.InterviewService;
import com.interviewplatform.user.entity.User;
import com.interviewplatform.orchestrator.InterviewOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * REST controller for interview session lifecycle management.
 *
 * <p>Base path: {@code /api/v1/interviews}</p>
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Tag(name = "Interviews", description = "Interview session lifecycle management")
@SecurityRequirement(name = "bearerAuth")
public class InterviewController {

    private final InterviewService interviewService;
    private final InterviewOrchestrator orchestrator;

    // =========================================================================
    // POST /api/v1/interviews — Create
    // =========================================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new interview session",
               description = "Creates an interview session in CREATED state. Domain and role level are required.")
    public ApiResponse<InterviewResponse> createInterview(
            @Valid @RequestBody CreateInterviewRequest request,
            @AuthenticationPrincipal User principal) {

        InterviewResponse response = interviewService.createInterview(request, principal.getId());
        return ApiResponse.success(response);
    }

    // =========================================================================
    // POST /api/v1/interviews/{id}/start — Start
    // =========================================================================

    @PostMapping("/{id}/start")
    @Operation(summary = "Start an interview session",
               description = "Transitions the interview to STARTED state and delivers the first question.")
    public ApiResponse<InterviewStartResponse> startInterview(
            @PathVariable UUID id,
            @AuthenticationPrincipal User principal) {

        InterviewStartResponse response = interviewService.startInterview(id, principal.getId());
        return ApiResponse.success(response);
    }

    // =========================================================================
    // GET /api/v1/interviews/{id} — Get
    // =========================================================================

    @GetMapping("/{id}")
    @Operation(summary = "Get current interview state",
               description = "Returns the full interview session state including running scores.")
    public ApiResponse<InterviewResponse> getInterview(
            @PathVariable UUID id,
            @AuthenticationPrincipal User principal) {

        InterviewResponse response = interviewService.getInterview(id, principal.getId());
        return ApiResponse.success(response);
    }

    // =========================================================================
    // GET /api/v1/interviews — List
    // =========================================================================

    @GetMapping
    @Operation(summary = "List candidate's interviews",
               description = "Returns a paginated list of interviews for the authenticated candidate.")
    public ApiResponse<Page<InterviewSummaryResponse>> listInterviews(
            @AuthenticationPrincipal User principal,
            @Parameter(description = "Optional state filter")
            @RequestParam(required = false) InterviewState state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<InterviewSummaryResponse> responses = interviewService.listInterviews(principal.getId(), state, pageable);
        return ApiResponse.success(responses);
    }

    // =========================================================================
    // POST /api/v1/interviews/{id}/answer — Submit Answer
    // =========================================================================

    @PostMapping(value = "/{id}/answer", consumes = "multipart/form-data")
    @Operation(summary = "Submit an audio answer",
               description = "Uploads an audio file for the current question and triggers STT and evaluation.")
    public ApiResponse<Void> submitAnswer(
            @PathVariable UUID id,
            @RequestPart("audio") MultipartFile audio,
            @AuthenticationPrincipal User principal) {

        UUID answerId = interviewService.submitAnswer(id, principal.getId(), audio);
        orchestrator.handleAudioSubmitted(id, answerId);
        return ApiResponse.success(null);
    }
    // POST /api/v1/interviews/{id}/end — End
    // =========================================================================

    @PostMapping("/{id}/end")
    @Operation(summary = "End an interview early",
               description = "Manually ends the interview and triggers report generation.")
    public ApiResponse<InterviewResponse> endInterview(
            @PathVariable UUID id,
            @AuthenticationPrincipal User principal) {

        InterviewResponse response = interviewService.endInterview(id, principal.getId());
        return ApiResponse.success(response);
    }
}
