package com.interviewplatform.agents.behavioral;

import com.interviewplatform.agents.common.Agent;
import com.interviewplatform.agents.common.AgentExecutionResult;
import com.interviewplatform.agents.common.InterviewContext;
import com.interviewplatform.ai.prompt.Prompt;
import com.interviewplatform.ai.prompt.PromptLoader;
import com.interviewplatform.ai.provider.LlmRequest;
import com.interviewplatform.ai.provider.AgentType;
import com.interviewplatform.ai.provider.orchestration.LlmOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Behavioral Evaluation Agent.
 *
 * <p>Evaluates soft skills and behavioral competencies across 5 dimensions:</p>
 * <ul>
 *   <li>Confidence &amp; Self-Awareness — 25 points</li>
 *   <li>Leadership &amp; Initiative — 20 points</li>
 *   <li>Ownership &amp; Accountability — 20 points</li>
 *   <li>Decision Making — 20 points</li>
 *   <li>Professionalism — 15 points</li>
 * </ul>
 *
 * <p>STAR method completeness (Situation/Task/Action/Result) is detected
 * via Java heuristics before the LLM call and injected into the prompt.</p>
 *
 * <p><b>Module:</b> Module 9 — Behavioral Evaluation Agent</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BehavioralAgent implements Agent {

    private static final String TEMPLATE   = "evaluation.md";
    private static final String AGENT_NAME = "BehavioralAgent";

    // STAR keyword heuristics (lightweight detection)
    private static final Pattern SITUATION_PATTERN = Pattern.compile(
            "\\b(situation|context|background|was working|at the time|when i|we were|our team)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TASK_PATTERN = Pattern.compile(
            "\\b(task|responsibility|my role|i was asked|i needed to|challenge|objective|goal)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern ACTION_PATTERN = Pattern.compile(
            "\\b(i did|i decided|i implemented|i built|i led|i worked|i took|i started|i created|action i)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern RESULT_PATTERN = Pattern.compile(
            "\\b(result|outcome|achieved|reduced|improved|increased|saved|delivered|as a result|in the end|we managed)\\b",
            Pattern.CASE_INSENSITIVE);

    private final LlmOrchestrator llmOrchestrator;
    private final PromptLoader promptLoader;

    /**
     * Evaluates a candidate's answer for behavioral competencies.
     *
     * @param context agent input with transcript and question context
     * @return behavioral evaluation result with subscores
     */
    @Override
    @SuppressWarnings("unchecked")
    public AgentExecutionResult<?> execute(InterviewContext context) {
        log.info("{}: evaluating answer for interview={}", AGENT_NAME, context.getInterview().getId());

        String transcript = orEmpty(context.getTranscript());
        boolean hasSituation = matches(SITUATION_PATTERN, transcript);
        boolean hasTask      = matches(TASK_PATTERN, transcript);
        boolean hasAction    = matches(ACTION_PATTERN, transcript);
        boolean hasResult    = matches(RESULT_PATTERN, transcript);

        try {
            Prompt prompt = promptLoader.loadPrompt("behavioral", TEMPLATE);

            String userMessage = promptLoader.buildContent(prompt, Map.of(
                    "ROLE_LEVEL",     orEmpty(context.getInterview().getRoleLevel() != null ? context.getInterview().getRoleLevel().name() : ""),
                    "QUESTION_TEXT",  orEmpty(context.getCurrentQuestion() != null ? context.getCurrentQuestion().getQuestionText() : ""),
                    "QUESTION_TYPE",  orEmpty(context.getCurrentQuestion() != null && context.getCurrentQuestion().getQuestionType() != null ? context.getCurrentQuestion().getQuestionType().name() : ""),
                    "TRANSCRIPT",     transcript,
                    "HAS_SITUATION",  String.valueOf(hasSituation),
                    "HAS_TASK",       String.valueOf(hasTask),
                    "HAS_ACTION",     String.valueOf(hasAction),
                    "HAS_RESULT",     String.valueOf(hasResult)
            ));

            LlmRequest llmRequest = LlmRequest.builder()
                    .agentType(AgentType.BEHAVIORAL)
                    .systemPrompt(userMessage)
                    .userMessage("Evaluate the behavioral answer.")
                    .schemaHint("BehavioralEvaluationResult")
                    .interviewId(context.getInterview().getId())
                    .requestId(java.util.UUID.randomUUID().toString())
                    .traceId(java.util.UUID.randomUUID().toString())
                    .promptVersion(prompt.getVersion())
                    .temperature(prompt.getTemperature())
                    .maxTokens(prompt.getMaxTokens())
                    .build();

            AgentExecutionResult<BehavioralEvaluationResult> executionResult = llmOrchestrator.execute(llmRequest, BehavioralEvaluationResult.class);

            if (executionResult.isSuccess()) {
                BehavioralEvaluationResult behResult = executionResult.getResult();
                behResult.setSuccess(true);
            }
            
            return executionResult;

        } catch (Exception ex) {
            log.error("{}: evaluation failed: {}", AGENT_NAME, ex.getMessage(), ex);
            return AgentExecutionResult.<BehavioralEvaluationResult>builder()
                    .agentType(AgentType.BEHAVIORAL)
                    .success(false)
                    .errorMessage(ex.getMessage())
                    .build();
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private boolean matches(Pattern pattern, String text) {
        return pattern.matcher(text).find();
    }

    private String orEmpty(String value) {
        return value != null ? value : "";
    }
}
