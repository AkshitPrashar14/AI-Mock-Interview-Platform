package com.interviewplatform.agents.behavioral;

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

    private static final String TEMPLATE   = "behavioral-agent-v1.txt";
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

    private final LlmProviderFactory providerFactory;
    private final PromptBuilder promptBuilder;
    private final ResponseParser responseParser;

    /**
     * Evaluates a candidate's answer for behavioral competencies.
     *
     * @param context agent input with transcript and question context
     * @return behavioral evaluation result with subscores
     */
    @Override
    @SuppressWarnings("unchecked")
    public AgentResult execute(InterviewContext context) {
        log.info("{}: evaluating answer for interview={}", AGENT_NAME, context.getInterview().getId());

        String transcript = orEmpty(context.getTranscript());
        boolean hasSituation = matches(SITUATION_PATTERN, transcript);
        boolean hasTask      = matches(TASK_PATTERN, transcript);
        boolean hasAction    = matches(ACTION_PATTERN, transcript);
        boolean hasResult    = matches(RESULT_PATTERN, transcript);

        try {
            String systemPrompt = "You are an expert behavioral interviewer. Always respond with valid JSON only.";
            String userMessage = promptBuilder.build(TEMPLATE, Map.of(
                    "ROLE_LEVEL",     orEmpty(context.getInterview().getRoleLevel() != null ? context.getInterview().getRoleLevel().name() : ""),
                    "QUESTION_TEXT",  orEmpty(context.getCurrentQuestion() != null ? context.getCurrentQuestion().getQuestionText() : ""),
                    "QUESTION_TYPE",  orEmpty(context.getCurrentQuestion() != null && context.getCurrentQuestion().getQuestionType() != null ? context.getCurrentQuestion().getQuestionType().name() : ""),
                    "TRANSCRIPT",     transcript,
                    "HAS_SITUATION",  String.valueOf(hasSituation),
                    "HAS_TASK",       String.valueOf(hasTask),
                    "HAS_ACTION",     String.valueOf(hasAction),
                    "HAS_RESULT",     String.valueOf(hasResult)
            ));

            String rawJson = providerFactory.getProvider()
                    .chatStructured(systemPrompt, userMessage, "BehavioralEvaluationResult");

            Map<String, Object> parsed = responseParser.parse(rawJson);

            int confidence    = responseParser.getInt(parsed, "confidenceScore", 15);
            int leadership    = responseParser.getInt(parsed, "leadershipScore", 12);
            int ownership     = responseParser.getInt(parsed, "ownershipScore", 12);
            int decision      = responseParser.getInt(parsed, "decisionMakingScore", 12);
            int professionalism = responseParser.getInt(parsed, "professionalismScore", 9);
            boolean starComplete = Boolean.TRUE.equals(parsed.get("starComplete"));
            int total         = responseParser.getInt(parsed, "totalScore",
                                    confidence + leadership + ownership + decision + professionalism);

            BehavioralEvaluationResult behResult = BehavioralEvaluationResult.builder()
                    .confidenceScore(confidence)
                    .leadershipScore(leadership)
                    .ownershipScore(ownership)
                    .decisionMakingScore(decision)
                    .professionalismScore(professionalism)
                    .starComplete(starComplete)
                    .totalScore(Math.min(100, total))
                    .strengths((List<String>) parsed.getOrDefault("strengths", List.of()))
                    .improvements((List<String>) parsed.getOrDefault("improvements", List.of()))
                    .summary(responseParser.getString(parsed, "summary", "Behavioral evaluation completed."))
                    .success(true)
                    .build();
            
            return AgentResult.success(AGENT_NAME, null, behResult.getTotalScore(), behResult.getSummary());

        } catch (Exception ex) {
            log.error("{}: evaluation failed: {}", AGENT_NAME, ex.getMessage(), ex);
            return AgentResult.failure(AGENT_NAME, ex.getMessage());
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
