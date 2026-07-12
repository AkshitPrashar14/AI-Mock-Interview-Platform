package com.interviewplatform.ai.provider;

import java.util.List;
import java.util.Map;

/**
 * Provider-agnostic contract for LLM chat completion.
 *
 * <p>All AI agent calls go through this interface so that the underlying
 * provider (Google Gemini, OpenRouter, etc.) can be swapped transparently
 * via configuration without changing agent code.</p>
 *
 * <p><b>Module:</b> Module 5 — AI Provider Abstraction</p>
 */
public interface LlmProvider {

    /**
     * Sends a chat completion request and returns the plain-text response.
     *
     * @param systemPrompt  the system-level instruction for the model
     * @param userMessage   the user turn content
     * @return the model's text response
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * Sends a structured output request, instructing the model to respond
     * with JSON that conforms to the provided schema hint.
     *
     * <p>Implementations should set {@code response_mime_type: application/json}
     * (Gemini) or equivalent provider-specific parameter.</p>
     *
     * @param systemPrompt  the system prompt (may include schema instructions)
     * @param userMessage   the user turn content
     * @param schemaHint    a description or JSON schema string that the response must follow
     * @return the model's JSON response as a raw String
     */
    String chatStructured(String systemPrompt, String userMessage, String schemaHint);

    /**
     * Sends a multi-turn chat completion with full history.
     *
     * @param messages  ordered list of messages; each map has {@code "role"} and {@code "content"} keys
     * @return the model's text response
     */
    String chatWithHistory(List<Map<String, String>> messages);

    /**
     * Returns the canonical model identifier used by this provider
     * (e.g. {@code "gemini-1.5-flash"} or {@code "openai/gpt-4o"}).
     */
    String getModelName();

    /**
     * Returns {@code true} if this provider is reachable and configured.
     * Used by the factory and health indicator.
     */
    boolean isAvailable();
}
