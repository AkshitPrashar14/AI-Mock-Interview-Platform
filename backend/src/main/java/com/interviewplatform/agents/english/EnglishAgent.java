package com.interviewplatform.agents.english;

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
 * English Communication Agent.
 *
 * <p>Evaluates the quality of professional communication in the candidate's answer
 * across 5 dimensions:</p>
 * <ul>
 *   <li>Grammar &amp; Syntax — 25 points</li>
 *   <li>Vocabulary — 25 points</li>
 *   <li>Fluency &amp; Coherence — 25 points</li>
 *   <li>Professional Tone — 15 points</li>
 *   <li>Filler Word Penalty — starts at 10, reduces 1 per 3 fillers</li>
 * </ul>
 *
 * <p>Filler word detection (um, uh, like, you know) is performed in Java
 * before the LLM call and injected into the prompt for transparency.</p>
 *
 * <p><b>Module:</b> Module 8 — English Communication Agent</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnglishAgent implements Agent {

    private static final String TEMPLATE   = "evaluation.md";
    private static final String AGENT_NAME = "EnglishAgent";

    /** Filler words to detect — case insensitive, word-boundary matched. */
    private static final Pattern FILLER_PATTERN =
            Pattern.compile("\\b(um|uh|like|you know)\\b", Pattern.CASE_INSENSITIVE);

    private final LlmOrchestrator llmOrchestrator;
    private final PromptLoader promptLoader;

    /**
     * Evaluates a candidate's answer for English communication quality.
     *
     * @param context agent input with transcript
     * @return English evaluation result with subscores
     */
    @Override
    @SuppressWarnings("unchecked")
    public AgentExecutionResult<?> execute(InterviewContext context) {
        log.info("{}: evaluating answer for interview={}", AGENT_NAME, context.getInterview().getId());

        int fillerCount = countFillerWords(context.getTranscript());

        try {
            Prompt prompt = promptLoader.loadPrompt("english", TEMPLATE);

            String userMessage = promptLoader.buildContent(prompt, Map.of(
                    "ROLE_LEVEL",         orEmpty(context.getInterview().getRoleLevel() != null ? context.getInterview().getRoleLevel().name() : ""),
                    "QUESTION_TEXT",      orEmpty(context.getCurrentQuestion() != null ? context.getCurrentQuestion().getQuestionText() : ""),
                    "TRANSCRIPT",         orEmpty(context.getTranscript()),
                    "FILLER_WORD_COUNT",  String.valueOf(fillerCount)
            ));

            LlmRequest llmRequest = LlmRequest.builder()
                    .agentType(AgentType.ENGLISH)
                    .systemPrompt(userMessage)
                    .userMessage("Evaluate the English communication.")
                    .schemaHint("EnglishEvaluationResult")
                    .interviewId(context.getInterview().getId())
                    .requestId(java.util.UUID.randomUUID().toString())
                    .traceId(java.util.UUID.randomUUID().toString())
                    .promptVersion(prompt.getVersion())
                    .temperature(prompt.getTemperature())
                    .maxTokens(prompt.getMaxTokens())
                    .build();

            AgentExecutionResult<EnglishEvaluationResult> executionResult = llmOrchestrator.execute(llmRequest, EnglishEvaluationResult.class);

            if (executionResult.isSuccess()) {
                EnglishEvaluationResult engResult = executionResult.getResult();
                engResult.setSuccess(true);
            }
            
            return executionResult;

        } catch (Exception ex) {
            log.error("{}: evaluation failed: {}", AGENT_NAME, ex.getMessage(), ex);
            return AgentExecutionResult.<EnglishEvaluationResult>builder()
                    .agentType(AgentType.ENGLISH)
                    .success(false)
                    .errorMessage(ex.getMessage())
                    .build();
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Counts filler word occurrences in the transcript using Java regex.
     * This is done in Java (not LLM) for accuracy and consistency.
     */
    int countFillerWords(String transcript) {
        if (transcript == null || transcript.isBlank()) return 0;
        var matcher = FILLER_PATTERN.matcher(transcript);
        int count = 0;
        while (matcher.find()) count++;
        return count;
    }

    private String orEmpty(String value) {
        return value != null ? value : "";
    }
}
