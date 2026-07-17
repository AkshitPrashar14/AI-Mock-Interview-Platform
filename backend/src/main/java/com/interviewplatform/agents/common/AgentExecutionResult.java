package com.interviewplatform.agents.common;

import com.interviewplatform.ai.provider.AgentType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Standardized execution result for all AI agents.
 * Replaces the old AgentResult to provide comprehensive observability.
 *
 * @param <T> the specific payload type returned by the agent
 */
@Data
@Builder
public class AgentExecutionResult<T> {
    private AgentType agentType;
    private String provider;
    private String model;
    
    private T result;
    private boolean success;
    private String errorMessage;
    
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    
    private Instant startedAt;
    private Instant finishedAt;
    private long durationMs;
    
    private String providerRequestId;
    private boolean validationPassed;
    private boolean fallbackUsed;
    private boolean cached;
    private int retryCount;
    
    private String requestId;
    private UUID interviewId;
}
