package com.interviewplatform.ai.provider.validation;

/**
 * Exception thrown when LLM response validation fails.
 */
public class LlmValidationException extends Exception {
    
    public LlmValidationException(String message) {
        super(message);
    }
    
    public LlmValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
