package com.interviewplatform.ai.prompt;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a loaded AI prompt including its metadata and raw template.
 */
@Data
@Builder
public class Prompt {
    private String name;
    private String version;
    private String author;
    private Double temperature;
    private Integer maxTokens;
    private String output;
    private String templateContent;
}
