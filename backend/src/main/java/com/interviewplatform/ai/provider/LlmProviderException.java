package com.interviewplatform.ai.provider;

/**
 * Thrown when an LLM provider call fails after all retries.
 *
 * <p><b>Module:</b> Module 5 — AI Provider Abstraction</p>
 */
public class LlmProviderException extends RuntimeException {

    public LlmProviderException(String message) {
        super(message);
    }

    public LlmProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
