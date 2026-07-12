package com.interviewplatform.orchestrator;

import com.interviewplatform.interview.entity.InterviewState;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * WebSocket payload broadcast to {@code /topic/interview/{id}/state}.
 *
 * <p>Serialised as JSON by the STOMP message converter.</p>
 *
 * <p><b>Module:</b> Module 3 — Interview Orchestrator</p>
 */
@Data
@AllArgsConstructor
public class InterviewStateEvent {

    /** The interview session ID this event belongs to. */
    private UUID interviewId;

    /** The new state after the transition. */
    private InterviewState state;

    /**
     * Optional extra payload (e.g. compositeScore, nextDifficulty).
     * May be {@code null} if there is no extra data.
     */
    private Map<String, Object> extra;

    /** Server-side event timestamp (UTC). */
    private final Instant timestamp = Instant.now();
}
