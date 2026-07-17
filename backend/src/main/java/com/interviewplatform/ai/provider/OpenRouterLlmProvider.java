package com.interviewplatform.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewplatform.ai.config.AiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenRouter LLM provider implementation.
 *
 * <p>Supports routing via a specifically injected model ID, allowing different
 * models for different agents. Also supports tracking tokens for observability.</p>
 *
 * <p><b>Module:</b> Module 5 — AI Provider Abstraction</p>
 */
@Slf4j
public class OpenRouterLlmProvider implements LlmProvider {

    private final AiConfig.OpenRouter config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String activeModelName;

    public OpenRouterLlmProvider(AiConfig.OpenRouter config, String modelName, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.config = config;
        this.activeModelName = modelName;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateText(LlmRequest request) {
        return executeRequest(request, false);
    }

    @Override
    public String generateJson(LlmRequest request) {
        return executeRequest(request, true);
    }



    @Override
    public String getModelName() {
        return this.activeModelName;
    }

    @Override
    public boolean isAvailable() {
        return StringUtils.hasText(config.getApiKey())
                && !config.getApiKey().contains("OPENROUTER_API_KEY");
    }

    // =========================================================================
    // Private Helpers
    // =========================================================================

    private String executeRequest(LlmRequest request, boolean isJson) {
        long startTime = System.currentTimeMillis();
        log.debug("OpenRouter.execute: agent={}, model={}, isJson={}", request.getAgentType(), activeModelName, isJson);

        try {
            Map<String, Object> requestBody = buildRequestBody(request, isJson);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getApiKey());
            headers.set("HTTP-Referer", "https://github.com/ai-mock-interview-platform"); 
            headers.set("X-Title", "AI Mock Interview Platform");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String url = config.getBaseUrl() + "/chat/completions";

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<?, ?> body = response.getBody();
                String text = extractText(body);
                logObservability(request, startTime, true, null, extractUsage(body));
                return text;
            } else {
                throw new LlmProviderException("OpenRouter API error: " + response.getStatusCode());
            }

        } catch (Exception ex) {
            logObservability(request, startTime, false, ex.getMessage(), null);
            throw new LlmProviderException("OpenRouter API call failed: " + ex.getMessage(), ex);
        }
    }

    private Map<String, Object> buildRequestBody(LlmRequest request, boolean isJson) {
        Map<String, Object> body = new LinkedHashMap<>();
        
        // Handling fallback models natively via OpenRouter if multiple are provided via comma-separated list
        if (activeModelName.contains(",")) {
            String[] models = activeModelName.split(",");
            List<String> trimmedModels = new ArrayList<>();
            for (String m : models) trimmedModels.add(m.trim());
            body.put("models", trimmedModels);
        } else {
            body.put("model", activeModelName);
        }

        List<Map<String, String>> messages = new ArrayList<>();

        if (StringUtils.hasText(request.getSystemPrompt())) {
            messages.add(Map.of("role", "system", "content", request.getSystemPrompt()));
        }
        
        if (request.getHistory() != null) {
            messages.addAll(request.getHistory());
        }

        if (StringUtils.hasText(request.getUserMessage())) {
            messages.add(Map.of("role", "user", "content", request.getUserMessage()));
        }

        body.put("messages", messages);

        if (isJson) {
            body.put("response_format", Map.of("type", "json_object"));
        }

        return body;
    }

    private String extractText(Map<?, ?> responseBody) {
        try {
            List<?> choices = (List<?>) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new LlmProviderException("OpenRouter returned no choices");
            }
            Map<?, ?> choice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) choice.get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            throw new LlmProviderException("Failed to parse OpenRouter response", e);
        }
    }

    private TokenUsage extractUsage(Map<?, ?> responseBody) {
        try {
            Map<?, ?> usage = (Map<?, ?>) responseBody.get("usage");
            if (usage != null) {
                Object pt = usage.get("prompt_tokens");
                Object ct = usage.get("completion_tokens");
                Object tt = usage.get("total_tokens");
                return new TokenUsage(
                        pt instanceof Number ? ((Number) pt).intValue() : 0,
                        ct instanceof Number ? ((Number) ct).intValue() : 0,
                        tt instanceof Number ? ((Number) tt).intValue() : 0
                );
            }
        } catch (Exception e) {
            log.warn("Failed to extract token usage from OpenRouter response");
        }
        return new TokenUsage(0, 0, 0);
    }

    private void logObservability(LlmRequest request, long startTime, boolean success, String error, TokenUsage usage) {
        long latency = System.currentTimeMillis() - startTime;
        String status = success ? "SUCCESS" : "FAILED";
        int promptTokens = usage != null ? usage.promptTokens : 0;
        int completionTokens = usage != null ? usage.completionTokens : 0;
        int totalTokens = usage != null ? usage.totalTokens : 0;

        log.info("LLM_OBSERVABILITY | Provider=OpenRouter | Agent={} | Model={} | Latency={}ms | PromptTokens={} | CompletionTokens={} | TotalTokens={} | Status={} | InterviewId={} | RequestId={} | Error={}",
                request.getAgentType(), activeModelName, latency, promptTokens, completionTokens, totalTokens,
                status, request.getInterviewId(), request.getRequestId(), error != null ? error : "none");
    }

    private record TokenUsage(int promptTokens, int completionTokens, int totalTokens) {}
}
