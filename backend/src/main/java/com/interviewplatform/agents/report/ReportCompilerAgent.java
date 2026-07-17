package com.interviewplatform.agents.report;

import com.interviewplatform.agents.aggregator.AggregatedEvaluation;
import com.interviewplatform.agents.common.Agent;
import com.interviewplatform.agents.common.AgentExecutionResult;
import com.interviewplatform.agents.common.InterviewContext;
import com.interviewplatform.ai.prompt.Prompt;
import com.interviewplatform.ai.prompt.PromptLoader;
import com.interviewplatform.ai.provider.LlmRequest;
import com.interviewplatform.ai.provider.AgentType;
import com.interviewplatform.ai.provider.orchestration.LlmOrchestrator;
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
import java.util.UUID;

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

    private static final String TEMPLATE   = "generation.md";
    private static final String AGENT_NAME = "ReportCompilerAgent";

    private final LlmOrchestrator llmOrchestrator;
    private final PromptLoader promptLoader;
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
    public AgentExecutionResult<?> execute(InterviewContext context) {
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
            Prompt prompt = promptLoader.loadPrompt("report", TEMPLATE);

            String userMessage = promptLoader.buildContent(prompt, Map.ofEntries(
                    Map.entry("INTERVIEW_ID",       interview.getId().toString()),
                    Map.entry("DOMAIN",             orEmpty(interview.getDomain())),
                    Map.entry("ROLE_LEVEL",         interview.getRoleLevel() != null
                                              ? interview.getRoleLevel().name() : "N/A"),
                    Map.entry("TOTAL_QUESTIONS",    String.valueOf(interview.getTotalQuestions())),
                    Map.entry("INTERVIEW_DATE",     interview.getCompletedAt() != null
                                              ? interview.getCompletedAt().toString() : Instant.now().toString()),
                    Map.entry("TECHNICAL_SCORE",    String.valueOf(evaluation.getTechnicalScore())),
                    Map.entry("ENGLISH_SCORE",      String.valueOf(evaluation.getEnglishScore())),
                    Map.entry("BEHAVIORAL_SCORE",   String.valueOf(evaluation.getBehavioralScore())),
                    Map.entry("COMPOSITE_SCORE",    String.valueOf(evaluation.getCompositeScore())),
                    Map.entry("PERFORMANCE_TIER",   tier.name()),
                    Map.entry("VERDICT",            verdict.name()),
                    Map.entry("TECHNICAL_SUMMARY",  orEmpty(evaluation.getTechnicalSummary())),
                    Map.entry("ENGLISH_SUMMARY",    orEmpty(evaluation.getEnglishSummary())),
                    Map.entry("BEHAVIORAL_SUMMARY", orEmpty(evaluation.getBehavioralSummary()))
            ));

            LlmRequest llmRequest = LlmRequest.builder()
                    .agentType(AgentType.REPORT)
                    .systemPrompt(userMessage)
                    .userMessage("Generate the executive report.")
                    .schemaHint("ReportNarrative")
                    .interviewId(interview.getId())
                    .requestId(java.util.UUID.randomUUID().toString())
                    .traceId(java.util.UUID.randomUUID().toString())
                    .promptVersion(prompt.getVersion())
                    .temperature(prompt.getTemperature())
                    .maxTokens(prompt.getMaxTokens())
                    .build();

            AgentExecutionResult<ReportNarrative> executionResult = llmOrchestrator.execute(llmRequest, ReportNarrative.class);

            if (executionResult.isSuccess()) {
                ReportNarrative narrative = executionResult.getResult();
                report.setExecutiveSummary(narrative.getExecutiveSummary());
                report.setStrengthHighlights(toJsonArray(narrative.getStrengths()));
                report.setImprovementAreas(toJsonArray(narrative.getImprovementAreas()));
                report.setStudyPlan(toJsonArray(narrative.getStudyPlan()));

                report.setReportStatus(ReportStatus.READY);
                report.setGeneratedAt(Instant.now());

                log.info("{}: report compiled — verdict={}, composite={}, tier={}",
                        AGENT_NAME, verdict, evaluation.getCompositeScore(), tier);
                
                return AgentExecutionResult.<Report>builder()
                        .agentType(AgentType.REPORT)
                        .success(true)
                        .result(report)
                        .build();
            } else {
                throw new Exception("Narrative generation failed");
            }

        } catch (Exception ex) {
            log.error("{}: narrative generation failed: {}", AGENT_NAME, ex.getMessage(), ex);
            report.setExecutiveSummary("Report generation encountered an error. Scores are accurate.");
            report.setReportStatus(ReportStatus.FAILED);
            return AgentExecutionResult.<Report>builder()
                    .agentType(AgentType.REPORT)
                    .success(false)
                    .errorMessage(ex.getMessage())
                    .result(report)
                    .build();
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
