package com.interviewplatform.agents.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Parses and validates JSON responses from LLM agents.
 *
 * <p>LLMs occasionally return JSON wrapped in markdown code fences
 * ({@code ```json ... ```}). This parser strips that wrapper before
 * attempting to deserialise. If parsing fails, the corrective retry
 * caller is responsible for re-prompting the model.</p>
 *
 * <p><b>Module:</b> Module 6 — Interview Agent (common infrastructure)</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * Parses a JSON string (potentially wrapped in markdown fences) into a Map.
     *
     * @param rawResponse the raw string from the LLM
     * @return parsed key-value map
     * @throws ResponseParseException if the string cannot be parsed as JSON
     */
    public Map<String, Object> parse(String rawResponse) {
        String cleaned = stripMarkdownFences(rawResponse);
        try {
            return objectMapper.readValue(cleaned, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            log.warn("ResponseParser: failed to parse LLM response: {}", ex.getMessage());
            log.debug("Raw response was: {}", rawResponse);
            throw new ResponseParseException("Could not parse LLM JSON response: " + ex.getMessage(), ex);
        }
    }

    /**
     * Attempts to parse the response; returns null if parsing fails
     * (caller decides whether to retry).
     */
    public Map<String, Object> parseOrNull(String rawResponse) {
        try {
            return parse(rawResponse);
        } catch (ResponseParseException ex) {
            return null;
        }
    }

    /**
     * Extracts an integer field from a parsed response map, returning a default
     * if the field is absent or not a number.
     */
    public int getInt(Map<String, Object> parsed, String field, int defaultValue) {
        Object value = parsed.get(field);
        if (value instanceof Number n) {
            int result = n.intValue();
            return Math.max(0, Math.min(100, result)); // clamp to 0–100
        }
        log.warn("ResponseParser: field '{}' missing or not a number, using default {}", field, defaultValue);
        return defaultValue;
    }

    /**
     * Extracts a String field from a parsed response map.
     */
    public String getString(Map<String, Object> parsed, String field, String defaultValue) {
        Object value = parsed.get(field);
        return (value instanceof String s) ? s : defaultValue;
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private String stripMarkdownFences(String raw) {
        if (raw == null) return "{}";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    /**
     * Thrown when LLM output cannot be parsed as valid JSON.
     */
    public static class ResponseParseException extends RuntimeException {
        public ResponseParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
