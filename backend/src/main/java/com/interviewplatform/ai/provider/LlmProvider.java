package com.interviewplatform.ai.provider;

/**
 * Provider-agnostic contract for LLM completion.
 *
 * <p>All AI agent calls go through this interface so that the underlying
 * provider (Google Gemini, OpenRouter, etc.) can be swapped transparently
 * via configuration without changing agent code.</p>
 *
 * <p><b>Module:</b> Module 5 — AI Provider Abstraction</p>
 */
public interface LlmProvider {

    /**
     * Sends a text completion request and returns the plain-text response.
     *
     * @param request the LLM request context including agent type and payload
     * @return the model's text response
     */
    String generateText(LlmRequest request);

    /**
     * Sends a structured output request, instructing the model to respond
     * with JSON that conforms to the provided schema hint.
     *
     * @param request the LLM request context including agent type and payload
     * @return the model's JSON response as a raw String
     */
    String generateJson(LlmRequest request);

    /**
     * Returns the canonical model identifier used by this provider instance
     * or the default if dynamically routed.
     */
    String getModelName();

    /**
     * Returns {@code true} if this provider is reachable and configured.
     * Used by the factory and health indicator.
     */
    boolean isAvailable();
}
