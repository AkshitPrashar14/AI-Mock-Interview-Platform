package com.interviewplatform.agents.technical;

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

/**
 * Technical Evaluation Agent.
 *
 * <p>Evaluates the technical quality of a candidate's answer across 4 dimensions:</p>
 * <ul>
 *   <li>Correctness — 40 points</li>
 *   <li>Depth — 30 points</li>
 *   <li>Problem Solving — 20 points</li>
 *   <li>Completeness — 10 points</li>
 * </ul>
 *
 * <p><b>Module:</b> Module 7 — Technical Evaluation Agent</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TechnicalAgent implements Agent {

    private static final String TEMPLATE   = "evaluation.md";
    private static final String AGENT_NAME = "TechnicalAgent";

    private final LlmOrchestrator llmOrchestrator;
    private final PromptLoader promptLoader;

    /**
     * Evaluates a candidate's answer for technical quality.
     *
     * @param context the current state of the interview
     * @return the result of the agent's execution
     */
    @Override
    @SuppressWarnings("unchecked")
    public AgentExecutionResult<?> execute(InterviewContext context) {
        log.info("{}: evaluating answer for interview={}", AGENT_NAME, context.getInterview().getId());

        try {
            Prompt prompt = promptLoader.loadPrompt("technical", TEMPLATE);

            String userMessage = promptLoader.buildContent(prompt, Map.of(
                    "DOMAIN",        orEmpty(context.getInterview().getDomain()),
                    "ROLE_LEVEL",    orEmpty(context.getInterview().getRoleLevel() != null ? context.getInterview().getRoleLevel().name() : ""),
                    "QUESTION_TEXT", orEmpty(context.getCurrentQuestion() != null ? context.getCurrentQuestion().getQuestionText() : ""),
                    "QUESTION_TYPE", orEmpty(context.getCurrentQuestion() != null && context.getCurrentQuestion().getQuestionType() != null ? context.getCurrentQuestion().getQuestionType().name() : ""),
                    "DIFFICULTY",    orEmpty(context.getDifficulty() != null ? context.getDifficulty().name() : ""),
                    "TRANSCRIPT",    orEmpty(context.getTranscript())
            ));

            LlmRequest llmRequest = LlmRequest.builder()
                    .agentType(AgentType.TECHNICAL)
                    .systemPrompt(userMessage)
                    .userMessage("Evaluate the technical answer.")
                    .schemaHint("TechnicalEvaluationResult")
                    .interviewId(context.getInterview().getId())
                    .requestId(java.util.UUID.randomUUID().toString())
                    .traceId(java.util.UUID.randomUUID().toString())
                    .promptVersion(prompt.getVersion())
                    .temperature(prompt.getTemperature())
                    .maxTokens(prompt.getMaxTokens())
                    .build();

            AgentExecutionResult<TechnicalEvaluationResult> executionResult = llmOrchestrator.execute(llmRequest, TechnicalEvaluationResult.class);

            if (executionResult.isSuccess()) {
                TechnicalEvaluationResult techResult = executionResult.getResult();
                techResult.setSuccess(true);
            }
            
            return executionResult;

        } catch (Exception ex) {
            log.error("{}: evaluation failed: {}", AGENT_NAME, ex.getMessage(), ex);
            return AgentExecutionResult.<TechnicalEvaluationResult>builder()
                    .agentType(AgentType.TECHNICAL)
                    .success(false)
                    .errorMessage(ex.getMessage())
                    .build();
        }
    }

    private String orEmpty(String value) {
        return value != null ? value : "";
    }
}
