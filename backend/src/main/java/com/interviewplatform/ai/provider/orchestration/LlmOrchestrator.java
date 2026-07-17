package com.interviewplatform.ai.provider.orchestration;

import com.interviewplatform.agents.common.AgentExecutionResult;
import com.interviewplatform.ai.config.AiConfig;
import com.interviewplatform.ai.provider.LlmProvider;
import com.interviewplatform.ai.provider.LlmProviderFactory;
import com.interviewplatform.ai.provider.LlmRequest;
import com.interviewplatform.ai.provider.cache.LlmCache;
import com.interviewplatform.ai.provider.validation.LlmValidationException;
import com.interviewplatform.ai.provider.validation.ResponseValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Orchestrates LLM execution with caching, validation, and exponential backoff retries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmOrchestrator {

    private final LlmProviderFactory providerFactory;
    private final ResponseValidator responseValidator;
    private final LlmCache llmCache;
    private final AiConfig aiConfig;

    public <T> AgentExecutionResult<T> execute(LlmRequest request, Class<T> responseClass) {
        Instant startedAt = Instant.now();
        LlmProvider provider = providerFactory.getProvider(request.getAgentType());
        
        String providerName = provider.getClass().getSimpleName();
        String modelName = provider.getModelName();
        double temp = request.getTemperature() != null ? request.getTemperature() : 0.7;
        
        // 1. Check Cache
        String cachedResponse = llmCache.get(
                providerName, modelName, request.getPromptVersion(),
                request.getSystemPrompt(), request.getUserMessage(),
                request.getSchemaHint(), temp
        );

        if (cachedResponse != null) {
            try {
                T result = responseValidator.validateAndParse(cachedResponse, responseClass);
                log.info("LLM Cache HIT - Agent: {}, Model: {}, TraceId: {}", request.getAgentType(), modelName, request.getTraceId());
                return buildSuccessResult(request, providerName, modelName, result, startedAt, true, 0, 0, 0, 0);
            } catch (LlmValidationException e) {
                log.warn("Cached response invalid, bypassing cache. TraceId: {}", request.getTraceId());
            }
        }

        // 2. Retry Loop with Exponential Backoff
        int maxRetries = aiConfig.getRetryCount() != null ? aiConfig.getRetryCount() : 3;
        int currentRetry = 0;
        long backoffMs = aiConfig.getRetryBackoffMs() != null ? aiConfig.getRetryBackoffMs() : 250;
        
        String currentSystemPrompt = request.getSystemPrompt();

        while (currentRetry <= maxRetries) {
            try {
                // Update prompt for retry attempts
                if (currentRetry > 0) {
                    log.warn("Retrying LLM Request. Attempt {}/{}. TraceId: {}", currentRetry, maxRetries, request.getTraceId());
                    Thread.sleep(backoffMs);
                    backoffMs *= 2; // Exponential backoff
                }

                // Temporary request for this execution attempt
                LlmRequest attemptRequest = LlmRequest.builder()
                        .agentType(request.getAgentType())
                        .systemPrompt(currentSystemPrompt)
                        .userMessage(request.getUserMessage())
                        .schemaHint(request.getSchemaHint())
                        .interviewId(request.getInterviewId())
                        .requestId(request.getRequestId())
                        .traceId(request.getTraceId())
                        .temperature(temp)
                        .maxTokens(request.getMaxTokens())
                        .build();

                String rawResponse = provider.generateJson(attemptRequest);

                // 3. Validate
                T result = responseValidator.validateAndParse(rawResponse, responseClass);
                
                // 4. Cache successful result
                llmCache.put(
                        providerName, modelName, request.getPromptVersion(),
                        request.getSystemPrompt(), request.getUserMessage(),
                        request.getSchemaHint(), temp, rawResponse
                );

                return buildSuccessResult(request, providerName, modelName, result, startedAt, false, currentRetry, 0, 0, 0);

            } catch (LlmValidationException ex) {
                currentSystemPrompt = request.getSystemPrompt() + "\n\nCRITICAL FIX: Your previous response failed validation: " + ex.getMessage() + ". You MUST fix this and respond with ONLY valid JSON.";
                if (currentRetry == maxRetries) {
                    log.error("LLM Validation failed after {} retries. TraceId: {}", maxRetries, request.getTraceId(), ex);
                    return buildFailureResult(request, providerName, modelName, "Validation failed: " + ex.getMessage(), startedAt, currentRetry);
                }
            } catch (Exception ex) {
                log.error("LLM Provider Execution failed. TraceId: {}", request.getTraceId(), ex);
                if (currentRetry == maxRetries) {
                    return buildFailureResult(request, providerName, modelName, "Execution failed: " + ex.getMessage(), startedAt, currentRetry);
                }
            }
            currentRetry++;
        }
        
        return buildFailureResult(request, providerName, modelName, "Max retries exceeded.", startedAt, currentRetry);
    }

    private <T> AgentExecutionResult<T> buildSuccessResult(LlmRequest req, String provider, String model, T result, Instant start, boolean cached, int retries, int pTok, int cTok, int tTok) {
        Instant finishedAt = Instant.now();
        return AgentExecutionResult.<T>builder()
                .agentType(req.getAgentType())
                .provider(provider)
                .model(model)
                .result(result)
                .success(true)
                .startedAt(start)
                .finishedAt(finishedAt)
                .durationMs(finishedAt.toEpochMilli() - start.toEpochMilli())
                .validationPassed(true)
                .cached(cached)
                .retryCount(retries)
                .requestId(req.getRequestId())
                .interviewId(req.getInterviewId())
                .promptTokens(pTok)
                .completionTokens(cTok)
                .totalTokens(tTok)
                .build();
    }

    private <T> AgentExecutionResult<T> buildFailureResult(LlmRequest req, String provider, String model, String error, Instant start, int retries) {
        Instant finishedAt = Instant.now();
        return AgentExecutionResult.<T>builder()
                .agentType(req.getAgentType())
                .provider(provider)
                .model(model)
                .success(false)
                .errorMessage(error)
                .startedAt(start)
                .finishedAt(finishedAt)
                .durationMs(finishedAt.toEpochMilli() - start.toEpochMilli())
                .validationPassed(false)
                .cached(false)
                .retryCount(retries)
                .requestId(req.getRequestId())
                .interviewId(req.getInterviewId())
                .build();
    }
}
