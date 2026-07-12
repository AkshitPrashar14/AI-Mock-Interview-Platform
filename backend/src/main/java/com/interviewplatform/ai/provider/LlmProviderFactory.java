package com.interviewplatform.ai.provider;

import com.interviewplatform.ai.config.AiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

    /**
     * Returns the active {@link LlmProvider} based on configuration.
     *
     * <p>If the primary provider is unavailable and a fallback is configured,
     * the fallback provider is returned instead.</p>
     *
     * @return the configured LLM provider
     * @throws LlmProviderException if no provider is available
     */
    public LlmProvider getProvider() {
        String primary = aiConfig.getProvider();
        LlmProvider provider = resolveProvider(primary);

        if (provider == null || !provider.isAvailable()) {
            String fallback = aiConfig.getFallbackProvider();
            if (StringUtils.hasText(fallback)) {
                log.warn("Primary provider '{}' unavailable, falling back to '{}'", primary, fallback);
                provider = resolveProvider(fallback);
            }
        }

        if (provider == null || !provider.isAvailable()) {
            log.error("No LLM provider is available. primary={}, fallback={}",
                    primary, aiConfig.getFallbackProvider());
            throw new LlmProviderException(
                    "No LLM provider is available. Check GEMINI_API_KEY or OPENROUTER_API_KEY environment variables.");
        }

        log.debug("LlmProviderFactory: using provider={}", provider.getModelName());
        return provider;
    }

    // =========================================================================
    // Private
    // =========================================================================

    private LlmProvider resolveProvider(String providerName) {
        if (!StringUtils.hasText(providerName)) {
            return null;
        }
        return switch (providerName.toLowerCase()) {
            case "gemini" -> geminiLlmProvider;
            // OpenRouter provider will be added in a future PR if needed
            default -> {
                log.warn("Unknown provider name: '{}', defaulting to Gemini", providerName);
                yield geminiLlmProvider;
            }
        };
    }
}
