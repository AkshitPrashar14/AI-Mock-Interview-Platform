package com.interviewplatform.agents.interview;

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

    private static final String TEMPLATE = "generation.md";
    private static final String AGENT_NAME = "InterviewAgent";

    private final LlmOrchestrator llmOrchestrator;
    private final PromptLoader promptLoader;

    /**
     * Generates the next interview question for the session.
     *
     * @param context agent input carrying session context
     * @return the generated question result
     */
    @Override
    @SuppressWarnings("unchecked")
    public AgentExecutionResult<?> execute(InterviewContext context) {
        // We will default to 5 total questions if metadata is missing
        int totalQuestions = context.getMetadata() != null && context.getMetadata().containsKey("totalQuestions")
                ? (int) context.getMetadata().get("totalQuestions") : 5;
        
        int questionNumber = context.getPreviousQuestions() == null ? 1 : context.getPreviousQuestions().size() + 1;

        log.info("{}: generating question {} for interviewId={}", 
                  AGENT_NAME, questionNumber, context.getInterview().getId());

        try {
            Prompt prompt = promptLoader.loadPrompt("interview", TEMPLATE);
            
            String userMessage = promptLoader.buildContent(prompt, Map.of(
                    "DOMAIN", orEmpty(context.getInterview().getDomain()),
                    "ROLE_LEVEL", orEmpty(context.getInterview().getRoleLevel() != null ? context.getInterview().getRoleLevel().name() : ""),
                    "QUESTION_NUMBER", String.valueOf(questionNumber),
                    "TOTAL_QUESTIONS", String.valueOf(totalQuestions),
                    "DIFFICULTY", orEmpty(context.getDifficulty() != null ? context.getDifficulty().name() : ""),
                    "QUESTION_TYPE", "TECHNICAL", // Could be dynamic
                    "CONVERSATION_HISTORY", orEmpty(context.getConversationHistory())
            ));

            LlmRequest llmRequest = LlmRequest.builder()
                    .agentType(AgentType.INTERVIEW)
                    .systemPrompt(userMessage) // The prompt is just one file for Interview Gen
                    .userMessage("Generate the question.")
                    .schemaHint("QuestionGenerationResult")
                    .interviewId(context.getInterview().getId())
                    .requestId(UUID.randomUUID().toString())
                    .promptVersion(prompt.getVersion())
                    .temperature(prompt.getTemperature())
                    .maxTokens(prompt.getMaxTokens())
                    .traceId(UUID.randomUUID().toString())
                    .build();

            AgentExecutionResult<QuestionGenerationResult> executionResult = llmOrchestrator.execute(llmRequest, QuestionGenerationResult.class);
            
            if (executionResult.isSuccess()) {
                QuestionGenerationResult qResult = executionResult.getResult();
                qResult.setInterviewId(context.getInterview().getId());
                qResult.setQuestionNumber(questionNumber);
                qResult.setSuccess(true);
            }
            
            return executionResult;

        } catch (Exception ex) {
            log.error("{}: question generation failed: {}", AGENT_NAME, ex.getMessage(), ex);
            QuestionGenerationResult fallback = QuestionGenerationResult.builder()
                    .interviewId(context.getInterview().getId())
                    .questionNumber(questionNumber)
                    .success(false)
                    .errorMessage(ex.getMessage())
                    .questionText("Can you describe a challenging technical problem you solved recently?")
                    .questionType("TECHNICAL")
                    .difficulty(orEmpty(context.getDifficulty() != null ? context.getDifficulty().name() : ""))
                    .build();
            
            return AgentExecutionResult.<QuestionGenerationResult>builder()
                    .agentType(AgentType.INTERVIEW)
                    .success(false)
                    .errorMessage(ex.getMessage())
                    .result(fallback)
                    .build();
        }
    }

    private String orEmpty(String value) {
        return value != null ? value : "";
    }
}
