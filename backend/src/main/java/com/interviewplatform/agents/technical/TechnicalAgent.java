package com.interviewplatform.agents.technical;

import com.interviewplatform.agents.common.Agent;
import com.interviewplatform.agents.common.AgentResult;
import com.interviewplatform.agents.common.InterviewContext;
import com.interviewplatform.agents.common.PromptBuilder;
import com.interviewplatform.agents.common.ResponseParser;
import com.interviewplatform.ai.provider.LlmProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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

    private static final String TEMPLATE   = "technical-agent-v1.txt";
    private static final String AGENT_NAME = "TechnicalAgent";

    private final LlmProviderFactory providerFactory;
    private final PromptBuilder promptBuilder;
    private final ResponseParser responseParser;

    /**
     * Evaluates a candidate's answer for technical quality.
     *
     * @param context the current state of the interview
     * @return the result of the agent's execution
     */
    @Override
    @SuppressWarnings("unchecked")
    public AgentResult execute(InterviewContext context) {
        log.info("{}: evaluating answer for interview={}", AGENT_NAME, context.getInterview().getId());

        try {
            String systemPrompt = "You are a senior technical interviewer. Always respond with valid JSON only.";
            String userMessage = promptBuilder.build(TEMPLATE, Map.of(
                    "DOMAIN",        orEmpty(context.getInterview().getDomain()),
                    "ROLE_LEVEL",    orEmpty(context.getInterview().getRoleLevel() != null ? context.getInterview().getRoleLevel().name() : ""),
                    "QUESTION_TEXT", orEmpty(context.getCurrentQuestion() != null ? context.getCurrentQuestion().getQuestionText() : ""),
                    "QUESTION_TYPE", orEmpty(context.getCurrentQuestion() != null && context.getCurrentQuestion().getQuestionType() != null ? context.getCurrentQuestion().getQuestionType().name() : ""),
                    "DIFFICULTY",    orEmpty(context.getDifficulty() != null ? context.getDifficulty().name() : ""),
                    "TRANSCRIPT",    orEmpty(context.getTranscript())
            ));

            String rawJson = providerFactory.getProvider()
                    .chatStructured(systemPrompt, userMessage, "TechnicalEvaluationResult");

            Map<String, Object> parsed = responseParser.parse(rawJson);

            int correctness   = responseParser.getInt(parsed, "correctnessScore", 20);
            int depth         = responseParser.getInt(parsed, "depthScore", 15);
            int problemSolving= responseParser.getInt(parsed, "problemSolvingScore", 10);
            int completeness  = responseParser.getInt(parsed, "completenessScore", 5);
            int total         = responseParser.getInt(parsed, "totalScore",
                                    correctness + depth + problemSolving + completeness);

            TechnicalEvaluationResult techResult = TechnicalEvaluationResult.builder()
                    .correctnessScore(correctness)
                    .depthScore(depth)
                    .problemSolvingScore(problemSolving)
                    .completenessScore(completeness)
                    .totalScore(Math.min(100, total))
                    .strengths((List<String>) parsed.getOrDefault("strengths", List.of()))
                    .improvements((List<String>) parsed.getOrDefault("improvements", List.of()))
                    .summary(responseParser.getString(parsed, "summary", "Technical evaluation completed."))
                    .success(true)
                    .build();
            
            return AgentResult.success(AGENT_NAME, null, techResult.getTotalScore(), techResult.getSummary());

        } catch (Exception ex) {
            log.error("{}: evaluation failed: {}", AGENT_NAME, ex.getMessage(), ex);
            return AgentResult.failure(AGENT_NAME, ex.getMessage());
        }
    }

    private String orEmpty(String value) {
        return value != null ? value : "";
    }
}
