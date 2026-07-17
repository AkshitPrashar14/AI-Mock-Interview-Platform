package com.interviewplatform.ai.prompt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads AI prompts from the classpath, parses metadata (frontmatter),
 * and supports variable substitution.
 */
@Slf4j
@Component
public class PromptLoader {

    private static final String PROMPTS_BASE_DIR = "prompts/";
    private final Map<String, Prompt> promptCache = new ConcurrentHashMap<>();

    @Value("${app.ai.prompt.version:latest}")
    private String defaultVersion;

    /**
     * Loads a prompt by domain/agent name, automatically resolving the version.
     * Example path structure: prompts/{domain}/{version}/{filename}.md
     *
     * @param domain   e.g., "interview", "technical"
     * @param filename e.g., "generation.md", "evaluation.md"
     * @return The parsed Prompt object containing metadata and content.
     */
    public Prompt loadPrompt(String domain, String filename) {
        String path = String.format("%s%s/%s/%s", PROMPTS_BASE_DIR, domain, defaultVersion, filename);
        return promptCache.computeIfAbsent(path, this::readAndParsePrompt);
    }

    /**
     * Replaces {{KEY}} placeholders in the prompt's template content.
     */
    public String buildContent(Prompt prompt, Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return prompt.getTemplateContent();
        }
        String result = prompt.getTemplateContent();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String token = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(token, value);
        }
        return result;
    }

    private Prompt readAndParsePrompt(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            String rawContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            log.debug("Loaded prompt from: {}", path);
            return parseFrontmatter(rawContent);
        } catch (IOException ex) {
            log.error("Failed to load prompt: {}", path, ex);
            throw new IllegalArgumentException("Prompt template not found: " + path, ex);
        }
    }

    private Prompt parseFrontmatter(String rawContent) {
        Prompt.PromptBuilder builder = Prompt.builder();
        String content = rawContent;

        if (rawContent.startsWith("---")) {
            int endIndex = rawContent.indexOf("---", 3);
            if (endIndex != -1) {
                String frontmatter = rawContent.substring(3, endIndex).trim();
                content = rawContent.substring(endIndex + 3).trim();

                for (String line : frontmatter.split("\\r?\\n")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        applyMetadata(builder, key, value);
                    }
                }
            }
        }
        
        builder.templateContent(content);
        return builder.build();
    }

    private void applyMetadata(Prompt.PromptBuilder builder, String key, String value) {
        try {
            switch (key) {
                case "name" -> builder.name(value);
                case "version" -> builder.version(value);
                case "author" -> builder.author(value);
                case "temperature" -> builder.temperature(Double.parseDouble(value));
                case "maxTokens" -> builder.maxTokens(Integer.parseInt(value));
                case "output" -> builder.output(value);
                default -> log.debug("Unknown prompt metadata key: {}", key);
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse numeric metadata for key {}: {}", key, value);
        }
    }
}
