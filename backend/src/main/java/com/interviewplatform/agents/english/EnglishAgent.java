package com.interviewplatform.agents.english;

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

    private static final String TEMPLATE   = "english-agent-v1.txt";
    private static final String AGENT_NAME = "EnglishAgent";

    /** Filler words to detect — case insensitive, word-boundary matched. */
    private static final Pattern FILLER_PATTERN =
            Pattern.compile("\\b(um|uh|like|you know)\\b", Pattern.CASE_INSENSITIVE);

    private final LlmProviderFactory providerFactory;
    private final PromptBuilder promptBuilder;
    private final ResponseParser responseParser;

    /**
     * Evaluates a candidate's answer for English communication quality.
     *
     * @param context agent input with transcript
     * @return English evaluation result with subscores
     */
    @Override
    @SuppressWarnings("unchecked")
    public AgentResult execute(InterviewContext context) {
        log.info("{}: evaluating answer for interview={}", AGENT_NAME, context.getInterview().getId());

        int fillerCount = countFillerWords(context.getTranscript());

        try {
            String systemPrompt = "You are an expert English communication coach. Always respond with valid JSON only.";
            String userMessage = promptBuilder.build(TEMPLATE, Map.of(
                    "ROLE_LEVEL",         orEmpty(context.getInterview().getRoleLevel() != null ? context.getInterview().getRoleLevel().name() : ""),
                    "QUESTION_TEXT",      orEmpty(context.getCurrentQuestion() != null ? context.getCurrentQuestion().getQuestionText() : ""),
                    "TRANSCRIPT",         orEmpty(context.getTranscript()),
                    "FILLER_WORD_COUNT",  String.valueOf(fillerCount)
            ));

            String rawJson = providerFactory.getProvider()
                    .chatStructured(systemPrompt, userMessage, "EnglishEvaluationResult");

            Map<String, Object> parsed = responseParser.parse(rawJson);

            int grammar       = responseParser.getInt(parsed, "grammarScore", 18);
            int vocabulary    = responseParser.getInt(parsed, "vocabularyScore", 18);
            int fluency       = responseParser.getInt(parsed, "fluencyScore", 18);
            int profTone      = responseParser.getInt(parsed, "professionalToneScore", 10);
            int fillerRemaining = responseParser.getInt(parsed, "fillerWordPenalty",
                                      Math.max(0, 10 - fillerCount / 3));
            int total         = responseParser.getInt(parsed, "totalScore",
                                    grammar + vocabulary + fluency + profTone + fillerRemaining);

            EnglishEvaluationResult engResult = EnglishEvaluationResult.builder()
                    .grammarScore(grammar)
                    .vocabularyScore(vocabulary)
                    .fluencyScore(fluency)
                    .professionalToneScore(profTone)
                    .fillerWordPenalty(fillerRemaining)
                    .fillerWordCount(fillerCount)
                    .totalScore(Math.min(100, total))
                    .strengths((List<String>) parsed.getOrDefault("strengths", List.of()))
                    .improvements((List<String>) parsed.getOrDefault("improvements", List.of()))
                    .summary(responseParser.getString(parsed, "summary", "Communication evaluation completed."))
                    .success(true)
                    .build();
            
            return AgentResult.success(AGENT_NAME, null, engResult.getTotalScore(), engResult.getSummary());

        } catch (Exception ex) {
            log.error("{}: evaluation failed: {}", AGENT_NAME, ex.getMessage(), ex);
            return AgentResult.failure(AGENT_NAME, ex.getMessage());
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
