package com.interviewplatform.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewplatform.ai.config.AiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Google Gemini LLM provider implementation.
 *
 * <h3>API</h3>
 * Uses the Gemini REST API {@code generateContent} endpoint:
 * <pre>
 * POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}
 * </pre>
 *
 * <h3>Structured output</h3>
 * When {@link #chatStructured} is called, sets
 * {@code generationConfig.response_mime_type = "application/json"}
 * so the model outputs a JSON object directly.
 *
 * <p><b>Module:</b> Module 5 — AI Provider Abstraction</p>
 */
@Slf4j
@Component("geminiLlmProvider")
public class GeminiLlmProvider implements LlmProvider {

    private final AiConfig.Gemini config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiLlmProvider(AiConfig aiConfig, RestTemplateBuilder builder, ObjectMapper objectMapper) {
        this.config = aiConfig.getGemini();
        this.objectMapper = objectMapper;
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(15))
                .readTimeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .build();
    }

    // =========================================================================
    // LlmProvider implementation
    // =========================================================================

    @Override
    public String chat(String systemPrompt, String userMessage) {
        log.debug("Gemini.chat: model={}, userLen={}", config.getModel(), userMessage.length());
        Map<String, Object> requestBody = buildRequestBody(systemPrompt, userMessage, null);
        return callApi(requestBody);
    }

    @Override
    public String chatStructured(String systemPrompt, String userMessage, String schemaHint) {
        log.debug("Gemini.chatStructured: model={}", config.getModel());
        Map<String, Object> requestBody = buildRequestBody(systemPrompt, userMessage, schemaHint);
        return callApi(requestBody);
    }

    @Override
    public String chatWithHistory(List<Map<String, String>> messages) {
        log.debug("Gemini.chatWithHistory: turns={}", messages.size());

        // Gemini uses "user" and "model" roles
        List<Map<String, Object>> contents = messages.stream()
                .map(m -> {
                    String role = "assistant".equals(m.get("role")) ? "model" : "user";
                    return Map.<String, Object>of(
                            "role", role,
                            "parts", List.of(Map.of("text", m.get("content")))
                    );
                })
                .toList();

        Map<String, Object> requestBody = Map.of(
                "contents", contents,
                "generationConfig", buildGenerationConfig(null)
        );

        return callApi(requestBody);
    }

    @Override
    public String getModelName() {
        return config.getModel();
    }

    @Override
    public boolean isAvailable() {
        return StringUtils.hasText(config.getApiKey())
                && !config.getApiKey().contains("GEMINI_API_KEY");
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private Map<String, Object> buildRequestBody(String systemPrompt, String userMessage,
                                                  String schemaHint) {
        List<Map<String, Object>> contents = List.of(
                Map.of("role", "user",
                        "parts", List.of(Map.of("text", userMessage)))
        );

        Map<String, Object> body = new java.util.LinkedHashMap<>();

        // System instruction (Gemini 1.5 supports this field)
        if (StringUtils.hasText(systemPrompt)) {
            body.put("system_instruction", Map.of(
                    "parts", List.of(Map.of("text", systemPrompt))
            ));
        }

        body.put("contents", contents);
        body.put("generationConfig", buildGenerationConfig(schemaHint));

        return body;
    }

    private Map<String, Object> buildGenerationConfig(String schemaHint) {
        Map<String, Object> config = new java.util.LinkedHashMap<>();
        config.put("maxOutputTokens", this.config.getMaxOutputTokens());
        config.put("temperature", this.config.getTemperature());
        config.put("topP", this.config.getTopP());

        if (StringUtils.hasText(schemaHint)) {
            config.put("response_mime_type", "application/json");
        }

        return config;
    }

    @SuppressWarnings("unchecked")
    private String callApi(Map<String, Object> requestBody) {
        String url = config.getBaseUrl() + "/models/" + config.getModel()
                + ":generateContent?key=" + config.getApiKey();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return extractText(response.getBody());
            } else {
                log.warn("Gemini API returned: {}", response.getStatusCode());
                throw new LlmProviderException("Gemini API error: " + response.getStatusCode());
            }
        } catch (LlmProviderException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Gemini API call failed: {}", ex.getMessage(), ex);
            throw new LlmProviderException("Gemini API call failed: " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<?, ?> responseBody) {
        try {
            List<?> candidates = (List<?>) responseBody.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new LlmProviderException("Gemini returned no candidates");
            }
            Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content   = (Map<?, ?>) candidate.get("content");
            List<?> parts       = (List<?>) content.get("parts");
            Map<?, ?> part      = (Map<?, ?>) parts.get(0);
            return (String) part.get("text");
        } catch (LlmProviderException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to parse Gemini response: {}", ex.getMessage());
            throw new LlmProviderException("Failed to parse Gemini response", ex);
        }
    }
}
