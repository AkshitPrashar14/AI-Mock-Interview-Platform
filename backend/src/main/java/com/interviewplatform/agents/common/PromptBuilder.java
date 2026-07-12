package com.interviewplatform.agents.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and caches prompt templates from the classpath.
 *
 * <p>Templates are stored under {@code src/main/resources/prompts/} and loaded
 * once on first use. Placeholder replacement uses the
 * {@code {{PLACEHOLDER_NAME}}} convention.</p>
 *
 * <p><b>Module:</b> Module 6 — Interview Agent (common infrastructure)</p>
 */
@Slf4j
@Component
public class PromptBuilder {

    private static final String PROMPTS_DIR = "prompts/";
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    /**
     * Loads a prompt template by filename and replaces all placeholders.
     *
     * @param templateFileName filename under {@code resources/prompts/} (e.g. {@code "interview-agent-v1.txt"})
     * @param variables        key → value replacements for {@code {{KEY}}} tokens
     * @return the final prompt with all placeholders substituted
     */
    public String build(String templateFileName, Map<String, String> variables) {
        String template = loadTemplate(templateFileName);
        return replacePlaceholders(template, variables);
    }

    /**
     * Returns the raw template without any substitution (useful for inspection/testing).
     */
    public String loadTemplate(String templateFileName) {
        return templateCache.computeIfAbsent(templateFileName, name -> {
            String path = PROMPTS_DIR + name;
            try {
                ClassPathResource resource = new ClassPathResource(path);
                String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                log.debug("Loaded prompt template: {}", path);
                return content;
            } catch (IOException ex) {
                log.error("Failed to load prompt template: {}", path, ex);
                throw new IllegalArgumentException("Prompt template not found: " + path, ex);
            }
        });
    }

    /**
     * Replaces {@code {{KEY}}} tokens in the template with the provided values.
     * Keys are case-sensitive.
     */
    private String replacePlaceholders(String template, Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String token = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(token, value);
        }
        return result;
    }
}
