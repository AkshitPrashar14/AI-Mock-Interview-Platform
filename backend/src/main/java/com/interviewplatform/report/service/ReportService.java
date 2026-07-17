package com.interviewplatform.report.service;

import com.interviewplatform.analytics.service.AnalyticsService;
import com.interviewplatform.agents.aggregator.AggregatedEvaluation;
import com.interviewplatform.agents.report.ReportCompilerAgent;
import com.interviewplatform.interview.entity.Interview;
import com.interviewplatform.interview.exception.InterviewNotFoundException;
import com.interviewplatform.interview.repository.InterviewRepository;
import com.interviewplatform.report.entity.Report;
import com.interviewplatform.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Report service — coordinates report generation and retrieval.
 *
 * <p><b>Module:</b> Module 11 — Report Compiler</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportCompilerAgent reportCompilerAgent;
    private final ReportRepository reportRepository;
    private final InterviewRepository interviewRepository;
    private final AnalyticsService analyticsService;

    // =========================================================================
    // Generate
    // =========================================================================

    /**
     * Generates and persists the final report for a completed interview.
     *
     * <p>Called by the {@link com.interviewplatform.orchestrator.InterviewOrchestrator}
     * after the parallel evaluation aggregation completes.</p>
     *
     * @param interviewId  the completed interview session ID
     * @param evaluation   the aggregated evaluation scores
     * @return the persisted report entity
     */
    @Transactional
    public Report generateReport(UUID interviewId, AggregatedEvaluation evaluation) {
        log.info("ReportService.generateReport: interviewId={}", interviewId);

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new InterviewNotFoundException(interviewId));

        com.interviewplatform.agents.common.InterviewContext context = com.interviewplatform.agents.common.InterviewContext.builder()
                .interview(interview)
                .candidate(interview.getCandidate())
                .metadata(java.util.Map.of("evaluations", java.util.List.of(evaluation)))
                .build();

        com.interviewplatform.agents.common.AgentExecutionResult<?> result = reportCompilerAgent.execute(context);
        if (!result.isSuccess() || !(result.getResult() instanceof Report report)) {
            throw new RuntimeException("Failed to compile report");
        }

        Report saved = reportRepository.save(report);

        // Recompute user analytics based on this new report
        try {
            analyticsService.recomputeSnapshot(interview.getCandidate().getId());
        } catch (Exception ex) {
            log.error("Failed to recompute analytics snapshot for candidate {}: {}", 
                    interview.getCandidate().getId(), ex.getMessage());
            // Do not fail the report generation if analytics fail
        }

        log.info("ReportService: report saved — reportId={}, verdict={}",
                saved.getId(), saved.getVerdict());
        return saved;
    }

    // =========================================================================
    // Retrieve
    // =========================================================================

    /**
     * Retrieves the report for the given interview, performing an ownership check.
     *
     * @param interviewId  the interview session ID
     * @param candidateId  the requesting candidate's user ID
     * @return the report entity
     * @throws ReportNotFoundException     if no report exists for the interview
     * @throws InterviewNotFoundException  if the interview doesn't belong to the candidate
     */
    @Transactional(readOnly = true)
    public Report getReport(UUID interviewId, UUID candidateId) {
        // Verify ownership via interview lookup
        Interview interview = interviewRepository.findByIdWithCandidate(interviewId)
                .orElseThrow(() -> new InterviewNotFoundException(interviewId));

        if (!interview.isOwnedBy(candidateId)) {
            throw new InterviewNotFoundException(interviewId); // 404 to avoid info leak
        }

        return reportRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new ReportNotFoundException(interviewId));
    }
}
