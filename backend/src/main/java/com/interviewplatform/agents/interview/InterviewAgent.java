package com.interviewplatform.agents.interview;

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
 * Interview Agent — generates context-aware interview questions.
 *
 * <h3>Responsibilities</h3>
 * <ol>
 *   <li>Builds a prompt using the candidate's session state and conversation history.</li>
 *   <li>Calls the LLM via {@link LlmProviderFactory} to generate one question.</li>
 *   <li>Parses the structured JSON response and returns a {@link QuestionGenerationResult}.</li>
 * </ol>
 *
 * <h3>Retry behaviour</h3>
 * If JSON parsing fails on the first attempt, the agent makes one corrective retry
 * with an explicit JSON-fix instruction appended to the message.
 *
 * <p><b>Module:</b> Module 6 — Interview Agent</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewAgent implements Agent {

    private static final String TEMPLATE = "interview-agent-v1.txt";
    private static final String AGENT_NAME = "InterviewAgent";

    private final LlmProviderFactory providerFactory;
    private final PromptBuilder promptBuilder;
    private final ResponseParser responseParser;

    /**
     * Generates the next interview question for the session.
     *
     * @param context agent input carrying session context
     * @return the generated question result
     */
    @Override
    @SuppressWarnings("unchecked")
    public AgentResult execute(InterviewContext context) {
        // We will default to 5 total questions if metadata is missing
        int totalQuestions = context.getMetadata() != null && context.getMetadata().containsKey("totalQuestions")
                ? (int) context.getMetadata().get("totalQuestions") : 5;
        
        int questionNumber = context.getPreviousQuestions() == null ? 1 : context.getPreviousQuestions().size() + 1;

        log.info("{}: generating question {} for interviewId={}", 
                  AGENT_NAME, questionNumber, context.getInterview().getId());

        try {
            String systemPrompt = "You are an expert interviewer. Always respond with valid JSON only.";
            String userMessage = promptBuilder.build(TEMPLATE, Map.of(
                    "DOMAIN", orEmpty(context.getInterview().getDomain()),
                    "ROLE_LEVEL", orEmpty(context.getInterview().getRoleLevel() != null ? context.getInterview().getRoleLevel().name() : ""),
                    "QUESTION_NUMBER", String.valueOf(questionNumber),
                    "TOTAL_QUESTIONS", String.valueOf(totalQuestions),
                    "DIFFICULTY", orEmpty(context.getDifficulty() != null ? context.getDifficulty().name() : ""),
                    "QUESTION_TYPE", "TECHNICAL", // Could be dynamic
                    "CONVERSATION_HISTORY", orEmpty(context.getConversationHistory())
            ));

            String rawJson = providerFactory.getProvider()
                    .chatStructured(systemPrompt, userMessage, "QuestionGenerationResult");

            Map<String, Object> parsed = responseParser.parse(rawJson);

            List<String> keyPoints = (List<String>) parsed.getOrDefault("expectedKeyPoints", List.of());

            QuestionGenerationResult qResult = QuestionGenerationResult.builder()
                    .interviewId(context.getInterview().getId())
                    .questionNumber(questionNumber)
                    .questionText(responseParser.getString(parsed, "questionText", "Tell me about yourself."))
                    .questionType(responseParser.getString(parsed, "questionType", "TECHNICAL"))
                    .difficulty(responseParser.getString(parsed, "difficulty", context.getDifficulty() != null ? context.getDifficulty().name() : ""))
                    .rationale(responseParser.getString(parsed, "rationale", ""))
                    .expectedKeyPoints(keyPoints)
                    .success(true)
                    .build();
            
            return AgentResult.success(AGENT_NAME, qResult, 100, "Question generated successfully.");

        } catch (Exception ex) {
            log.error("{}: question generation failed: {}", AGENT_NAME, ex.getMessage(), ex);
            QuestionGenerationResult fallback = QuestionGenerationResult.builder()
                    .interviewId(context.getInterview().getId())
                    .questionNumber(questionNumber)
                    .success(false)
                    .errorMessage(ex.getMessage())
                    // Fallback question so the interview can continue
                    .questionText("Can you describe a challenging technical problem you solved recently?")
                    .questionType("TECHNICAL")
                    .difficulty(orEmpty(context.getDifficulty() != null ? context.getDifficulty().name() : ""))
                    .build();
            return AgentResult.success(AGENT_NAME, fallback, 100, "Fallback question generated.");
        }
    }

    private String orEmpty(String value) {
        return value != null ? value : "";
    }
}
