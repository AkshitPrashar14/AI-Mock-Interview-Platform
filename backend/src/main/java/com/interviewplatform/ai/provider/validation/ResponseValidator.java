package com.interviewplatform.ai.provider.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates LLM responses against expected schema and business constraints.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseValidator {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    /**
     * Parses and validates the raw JSON response from the LLM.
     *
     * @param rawJson The JSON string returned by the provider.
     * @param targetClass The class to parse into.
     * @param <T> The type of the target class.
     * @return The parsed and validated object.
     * @throws LlmValidationException If parsing or validation fails.
     */
    public <T> T validateAndParse(String rawJson, Class<T> targetClass) throws LlmValidationException {
        try {
            // 1. Validate JSON syntax and structure
            T parsedObject = objectMapper.readValue(rawJson, targetClass);

            // 2. Validate business constraints (required fields, ranges, etc.)
            Set<ConstraintViolation<T>> violations = validator.validate(parsedObject);

            if (!violations.isEmpty()) {
                String errorMsg = violations.stream()
                        .map(v -> v.getPropertyPath() + " " + v.getMessage())
                        .collect(Collectors.joining(", "));
                log.warn("LLM Response Validation failed: {}", errorMsg);
                throw new LlmValidationException("Validation failed: " + errorMsg);
            }

            return parsedObject;

        } catch (JsonProcessingException e) {
            log.warn("LLM JSON Parsing failed: {}", e.getMessage());
            throw new LlmValidationException("Invalid JSON syntax: " + e.getMessage(), e);
        }
    }
}
