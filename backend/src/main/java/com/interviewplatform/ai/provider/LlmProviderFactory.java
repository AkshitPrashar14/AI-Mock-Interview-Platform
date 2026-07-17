package com.interviewplatform.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewplatform.ai.config.AiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Factory that resolves the active {@link LlmProvider} at runtime.
 *
 * <p>Reads {@code app.ai.provider} from configuration to select primary provider.
 * If the primary is unavailable and {@code app.ai.fallback-provider} is set,
 * automatically falls back to the secondary provider.</p>
 *
 * <h3>Supported providers</h3>
 * <ul>
 *   <li>{@code gemini} — Google Gemini via REST (primary for V1)</li>
 *   <li>{@code openrouter} — OpenRouter (optional fallback)</li>
 * </ul>
 *
 * <p><b>Module:</b> Module 5 — AI Provider Abstraction</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmProviderFactory {

    private final AiConfig aiConfig;
    private final GeminiLlmProvider geminiLlmProvider;
    private final RestTemplateBuilder restTemplateBuilder;
    private final ObjectMapper objectMapper;

    /**
     * Returns the active {@link LlmProvider} based on configuration and agent type.
     *
     * @param agentType the type of the agent requesting the provider
     * @return the configured LLM provider
     * @throws LlmProviderException if no provider is available
     */
    public LlmProvider getProvider(AgentType agentType) {
        String primary = aiConfig.getProvider();
        LlmProvider provider = resolveProvider(primary, agentType);

        if (provider == null || !provider.isAvailable()) {
            String fallback = aiConfig.getFallbackProvider();
            if (StringUtils.hasText(fallback)) {
                log.warn("Primary provider '{}' unavailable, falling back to '{}'", primary, fallback);
                provider = resolveProvider(fallback, agentType);
            }
        }

        if (provider == null || !provider.isAvailable()) {
            log.error("No LLM provider is available. primary={}, fallback={}",
                    primary, aiConfig.getFallbackProvider());
            throw new LlmProviderException(
                    "No LLM provider is available. Check GEMINI_API_KEY or OPENROUTER_API_KEY environment variables.");
        }

        log.debug("LlmProviderFactory: using provider={}, model={}", provider.getClass().getSimpleName(), provider.getModelName());
        return provider;
    }

    /**
     * Backward-compatible default provider resolver.
     */
    public LlmProvider getProvider() {
        return getProvider(null);
    }

    // =========================================================================
    // Private
    // =========================================================================

    private LlmProvider resolveProvider(String providerName, AgentType agentType) {
        if (!StringUtils.hasText(providerName)) {
            return null;
        }
        return switch (providerName.toLowerCase()) {
            case "gemini" -> geminiLlmProvider;
            case "openrouter" -> {
                String modelName = aiConfig.getOpenRouter().getModel();
                if (agentType != null && aiConfig.getOpenRouter().getAgentModels().containsKey(agentType)) {
                    modelName = aiConfig.getOpenRouter().getAgentModels().get(agentType);
                }
                RestTemplate restTemplate = restTemplateBuilder
                        .setConnectTimeout(Duration.ofSeconds(15))
                        .setReadTimeout(Duration.ofSeconds(aiConfig.getOpenRouter().getTimeoutSeconds()))
                        .build();
                yield new OpenRouterLlmProvider(aiConfig.getOpenRouter(), modelName, restTemplate, objectMapper);
            }
            default -> {
                log.warn("Unknown provider name: '{}', defaulting to Gemini", providerName);
                yield geminiLlmProvider;
            }
        };
    }
}
