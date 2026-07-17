package com.interviewplatform.ai.provider;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object containing all necessary context for an LLM request.
 * Required for centralized observability, dynamic model routing, and structured output.
 *
 * <p><b>Module:</b> Module 5 — AI Provider Abstraction</p>
 */
@Data
@Builder
public class LlmRequest {

    /** The agent making this request (determines routing) */
    private AgentType agentType;

    /** System prompt or instructions */
    private String systemPrompt;

    /** Current user message */
    private String userMessage;

    /** Optional schema hint for structured JSON generation */
    private String schemaHint;

    /** Optional conversation history for multi-turn chats */
    private List<Map<String, String>> history;

    /** Correlation ID for the interview session */
    private UUID interviewId;

    /** Unique Request ID for observability logs */
    private String requestId;
    
    /** Trace ID for distributed tracing (observability) */
    private String traceId;

    /** Prompt Version used */
    private String promptVersion;

    /** LLM Temperature setting */
    private Double temperature;

    /** Max tokens limit */
    private Integer maxTokens;
}
