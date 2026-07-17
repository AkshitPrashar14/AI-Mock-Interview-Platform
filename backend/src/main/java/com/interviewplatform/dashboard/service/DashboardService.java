package com.interviewplatform.dashboard.service;

import com.interviewplatform.analytics.dto.AnalyticsSnapshotResponse;
import com.interviewplatform.analytics.entity.AnalyticsSnapshot;
import com.interviewplatform.analytics.service.AnalyticsService;
import com.interviewplatform.dashboard.dto.DashboardSummaryResponse;
import com.interviewplatform.interview.dto.response.InterviewSummaryResponse;
import com.interviewplatform.interview.entity.Interview;
import com.interviewplatform.interview.entity.InterviewState;
import com.interviewplatform.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service to aggregate dashboard data for candidates.
 *
 * <p><b>Module:</b> Module 12 — Dashboard APIs</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AnalyticsService analyticsService;
    private final InterviewRepository interviewRepository;

    /**
     * Gets the full dashboard summary for a candidate.
     */
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(UUID candidateId) {
        log.info("DashboardService.getDashboardSummary: candidateId={}", candidateId);

        // 1. Fetch Analytics Snapshot
        AnalyticsSnapshot snapshot = analyticsService.getSnapshot(candidateId);
        AnalyticsSnapshotResponse analyticsResponse = AnalyticsSnapshotResponse.builder()
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

        // 2. Fetch Active Interviews (anything not completed or failed)
        List<InterviewState> inactiveStates = List.of(
                InterviewState.COMPLETED, InterviewState.REPORT_GENERATING, 
                InterviewState.REPORT_GENERATED, InterviewState.ERROR, InterviewState.ABANDONED);
        List<Interview> active = interviewRepository.findByCandidateIdAndStateNotInOrderByCreatedAtDesc(candidateId, inactiveStates);
        List<InterviewSummaryResponse> activeResponses = active.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());

        // 3. Fetch Recent Completed Interviews (limit 5)
        List<Interview> recent = interviewRepository.findByCandidateIdAndStateOrderByCreatedAtDesc(
                candidateId, InterviewState.REPORT_GENERATED, PageRequest.of(0, 5)).getContent();
        List<InterviewSummaryResponse> recentResponses = recent.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());

        return DashboardSummaryResponse.builder()
                .analytics(analyticsResponse)
                .activeInterviews(activeResponses)
                .recentInterviews(recentResponses)
                .build();
    }

    private InterviewSummaryResponse toSummaryResponse(Interview interview) {
        return InterviewSummaryResponse.builder()
                .interviewId(interview.getId())
                .domain(interview.getDomain())
                .roleLevel(interview.getRoleLevel())
                .state(interview.getState())
                .totalQuestions(interview.getTotalQuestions())
                .currentQuestionNumber(interview.getCurrentQuestionNumber())
                .currentDifficulty(interview.getCurrentDifficulty())
                .runningCompositeScore(interview.getRunningCompositeScore())
                .startedAt(interview.getStartedAt())
                .completedAt(interview.getCompletedAt())
                .createdAt(interview.getCreatedAt())
                .build();
    }
}
