package com.interviewplatform.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Typed configuration for the AI provider layer.
 *
 * <p>Bound from {@code app.ai.*} in {@code application.yml}.</p>
 *
 * <p><b>Module:</b> Module 5 — AI Provider Abstraction</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiConfig {

    /**
     * Primary LLM provider identifier.
     * Supported values: {@code "gemini"}, {@code "openrouter"}
     */
    private String provider = "gemini";

    /**
     * Fallback provider if the primary is unavailable.
     * Leave blank to disable fallback.
     */
    private String fallbackProvider = "";

    // -------------------------------------------------------------------------
    // Gemini settings
    // -------------------------------------------------------------------------

    private Gemini gemini = new Gemini();

    @Data
    public static class Gemini {
        /** Gemini API key from Google AI Studio. */
        private String apiKey = "${GEMINI_API_KEY:}";

        /** Gemini model to use. */
        private String model = "gemini-1.5-flash";

        /** Base URL for Gemini REST API. */
        private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";

        /** Maximum tokens in the response. */
        private int maxOutputTokens = 2048;

        /** Sampling temperature (0 = deterministic, 1 = creative). */
        private double temperature = 0.7;

        /** Top-P sampling parameter. */
        private double topP = 0.95;

        /** Request timeout in seconds. */
        private int timeoutSeconds = 60;
    }

    // -------------------------------------------------------------------------
    // OpenRouter settings (Module 5 secondary provider)
    // -------------------------------------------------------------------------

    private OpenRouter openRouter = new OpenRouter();

    @Data
    public static class OpenRouter {
        /** OpenRouter API key. */
        private String apiKey = "${OPENROUTER_API_KEY:}";

        /** Model to use via OpenRouter (e.g. "openai/gpt-4o"). */
        private String model = "openai/gpt-4o-mini";

        /** Base URL for OpenRouter API. */
        private String baseUrl = "https://openrouter.ai/api/v1";

        /** Request timeout in seconds. */
        private int timeoutSeconds = 60;
    }
}
