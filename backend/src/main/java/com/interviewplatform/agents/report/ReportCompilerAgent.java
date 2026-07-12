package com.interviewplatform.agents.report;

import com.interviewplatform.agents.aggregator.AggregatedEvaluation;
import com.interviewplatform.agents.common.Agent;
import com.interviewplatform.agents.common.AgentResult;
import com.interviewplatform.agents.common.InterviewContext;
import com.interviewplatform.agents.common.PromptBuilder;
import com.interviewplatform.agents.common.ResponseParser;
import com.interviewplatform.ai.provider.LlmProviderFactory;
import com.interviewplatform.interview.entity.Evaluation;
import com.interviewplatform.interview.entity.Interview;
import com.interviewplatform.interview.entity.PerformanceTier;
import com.interviewplatform.report.entity.Report;
import com.interviewplatform.report.entity.ReportStatus;
import com.interviewplatform.report.entity.Verdict;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Report Compiler Agent — generates the final interview assessment report.
 *
 * <h3>Design Contract</h3>
 * <ul>
 *   <li>Receives aggregated scores from the {@link com.interviewplatform.agents.aggregator.EvaluationAggregator}.</li>
 *   <li>Verdict is computed deterministically in Java — NOT by the LLM.</li>
 *   <li>The LLM generates narrative text only: executive summary, strengths,
 *       improvement areas, study plan, and hiring narrative.</li>
 * </ul>
 *
 * <p><b>Module:</b> Module 11 — Report Compiler</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportCompilerAgent implements Agent {

    private static final String TEMPLATE   = "report-compiler-v1.txt";
    private static final String AGENT_NAME = "ReportCompilerAgent";

    private final LlmProviderFactory providerFactory;
    private final PromptBuilder promptBuilder;
    private final ResponseParser responseParser;
    private final ObjectMapper objectMapper;
    private final ReportAggregator reportAggregator;

    /**
     * Compiles the final report for an interview.
     *
     * @param context the current state of the interview (must contain evaluations in metadata or linked)
     * @return a fully populated {@link Report} entity ready to be persisted, wrapped in AgentResult
     */
    @Override
    @SuppressWarnings("unchecked")
    public AgentResult execute(InterviewContext context) {
        Interview interview = context.getInterview();
        log.info("{}: compiling report for interviewId={}", AGENT_NAME, interview.getId());

        // We assume evaluations are passed in context metadata for report generation
        List<Evaluation> evaluations = (List<Evaluation>) context.getMetadata().get("evaluations");
        AggregatedEvaluation evaluation = reportAggregator.aggregateFinalScore(evaluations);

        // ── 1. Compute verdict deterministically (pure Java) ──────────────────
        Verdict verdict = Verdict.fromScore(evaluation.getCompositeScore());
        PerformanceTier tier = PerformanceTier.fromScore(evaluation.getCompositeScore());

        // ── 2. Build base report with scores ─────────────────────────────────
        Report report = Report.builder()
                .interview(interview)
                .finalTechnicalScore(BigDecimal.valueOf(evaluation.getTechnicalScore()))
                .finalEnglishScore(BigDecimal.valueOf(evaluation.getEnglishScore()))
                .finalBehavioralScore(BigDecimal.valueOf(evaluation.getBehavioralScore()))
                .finalCompositeScore(BigDecimal.valueOf(evaluation.getCompositeScore()))
                .finalTier(tier)
                .verdict(verdict)
                .reportStatus(ReportStatus.GENERATING)
                .build();

        // ── 3. Call LLM for narrative generation ─────────────────────────────
        try {
            String systemPrompt = "You are a senior hiring manager. Always respond with valid JSON only.";
            String userMessage = promptBuilder.build(TEMPLATE, Map.of(
                    "INTERVIEW_ID",       interview.getId().toString(),
                    "DOMAIN",             orEmpty(interview.getDomain()),
                    "ROLE_LEVEL",         interview.getRoleLevel() != null
                                              ? interview.getRoleLevel().name() : "N/A",
                    "TOTAL_QUESTIONS",    String.valueOf(interview.getTotalQuestions()),
                    "INTERVIEW_DATE",     interview.getCompletedAt() != null
                                              ? interview.getCompletedAt().toString() : Instant.now().toString(),
                    "TECHNICAL_SCORE",    String.valueOf(evaluation.getTechnicalScore()),
                    "ENGLISH_SCORE",      String.valueOf(evaluation.getEnglishScore()),
                    "BEHAVIORAL_SCORE",   String.valueOf(evaluation.getBehavioralScore()),
                    "COMPOSITE_SCORE",    String.valueOf(evaluation.getCompositeScore()),
                    "PERFORMANCE_TIER",   tier.name(),
                    "VERDICT",            verdict.name(),
                    "TECHNICAL_SUMMARY",  orEmpty(evaluation.getTechnicalSummary()),
                    "ENGLISH_SUMMARY",    orEmpty(evaluation.getEnglishSummary()),
                    "BEHAVIORAL_SUMMARY", orEmpty(evaluation.getBehavioralSummary())
            ));

            String rawJson = providerFactory.getProvider()
                    .chatStructured(systemPrompt, userMessage, "ReportNarrative");

            Map<String, Object> parsed = responseParser.parse(rawJson);

            report.setExecutiveSummary(responseParser.getString(parsed, "executiveSummary", "Report generated."));
            report.setStrengthHighlights(toJsonArray(parsed.get("strengths")));
            report.setImprovementAreas(toJsonArray(parsed.get("improvementAreas")));
            report.setStudyPlan(toJsonArray(parsed.get("studyPlan")));

            report.setReportStatus(ReportStatus.COMPLETED);
            report.setGeneratedAt(Instant.now());

            log.info("{}: report compiled — verdict={}, composite={}, tier={}",
                    AGENT_NAME, verdict, evaluation.getCompositeScore(), tier);
            
            return AgentResult.success(AGENT_NAME, report, evaluation.getCompositeScore(), "Report successfully compiled.");

        } catch (Exception ex) {
            log.error("{}: narrative generation failed: {}", AGENT_NAME, ex.getMessage(), ex);
            report.setExecutiveSummary("Report generation encountered an error. Scores are accurate.");
            report.setReportStatus(ReportStatus.FAILED);
            return AgentResult.failure(AGENT_NAME, ex.getMessage());
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private String toJsonArray(Object value) {
        try {
            if (value instanceof List) {
                return objectMapper.writeValueAsString(value);
            }
        } catch (Exception ex) {
            log.warn("{}: failed to serialize field: {}", AGENT_NAME, ex.getMessage());
        }
        return "[]";
    }

    private String orEmpty(String value) {
        return value != null ? value : "";
    }
}
