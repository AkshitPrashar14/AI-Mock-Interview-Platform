package com.interviewplatform.orchestrator.websocket;

import com.interviewplatform.orchestrator.InterviewOrchestrator;
import com.interviewplatform.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * STOMP WebSocket controller for real-time interview interaction.
 *
 * <h3>Inbound message destinations (client → server)</h3>
 * <ul>
 *   <li>{@code /app/interview/{id}/recording-start} — candidate starts recording</li>
 *   <li>{@code /app/interview/{id}/recording-stop}  — candidate stops recording,
 *       triggering audio submission to the orchestrator</li>
 * </ul>
 *
 * <h3>Outbound broadcast destinations (server → client)</h3>
 * <ul>
 *   <li>{@code /topic/interview/{id}/state} — interview state change events</li>
 * </ul>
 *
 * <p><b>Module:</b> Module 3 — Interview Orchestrator</p>
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class InterviewWebSocketController {

    private final InterviewOrchestrator orchestrator;

    // =========================================================================
    // recording-start
    // =========================================================================

    /**
     * Client notifies the server that the candidate has started recording an audio answer.
     *
     * <p>This currently just logs the event. The server does not need to perform
     * any state transition here — recording management is client-side.</p>
     *
     * @param interviewId the active interview session ID
     * @param payload     optional payload carrying additional metadata (may be empty JSON {@code {}})
     * @param principal   the authenticated user (set by the JWT channel interceptor on CONNECT)
     */
    @MessageMapping("/interview/{id}/recording-start")
    public void handleRecordingStart(
            @DestinationVariable("id") UUID interviewId,
            @Payload(required = false) RecordingStartPayload payload,
            @AuthenticationPrincipal User principal) {

        String userId = principal != null ? principal.getId().toString() : "unknown";
        log.info("WS recording-start: interviewId={}, userId={}", interviewId, userId);

        // No server-side state change needed — the candidate's audio is captured client-side.
        // The client will send recording-stop when done.
    }

    // =========================================================================
    // recording-stop
    // =========================================================================

    /**
     * Client notifies the server that the candidate has stopped recording and the audio
     * has been uploaded (or will be uploaded) to the backend.
     *
     * <p>Triggers {@link InterviewOrchestrator#handleAudioSubmitted(UUID, UUID)} which
     * transitions the interview to {@code TRANSCRIBING} and begins STT processing.</p>
     *
     * @param interviewId the active interview session ID
     * @param payload     payload carrying the {@code answerId} of the uploaded audio answer
     * @param principal   the authenticated user
     */
    @MessageMapping("/interview/{id}/recording-stop")
    public void handleRecordingStop(
            @DestinationVariable("id") UUID interviewId,
            @Payload RecordingStopPayload payload,
            @AuthenticationPrincipal User principal) {

        String userId = principal != null ? principal.getId().toString() : "unknown";
        log.info("WS recording-stop: interviewId={}, answerId={}, userId={}",
                interviewId, payload.getAnswerId(), userId);

        orchestrator.handleAudioSubmitted(interviewId, payload.getAnswerId());
    }

    // =========================================================================
    // Payload types
    // =========================================================================

    /**
     * Optional payload for {@code recording-start} messages.
     */
    @lombok.Data
    public static class RecordingStartPayload {
        /** Client-reported recording start timestamp (ISO-8601). */
        private String startedAt;
    }

    /**
     * Required payload for {@code recording-stop} messages.
     */
    @lombok.Data
    public static class RecordingStopPayload {
        /**
         * ID of the {@code answers} record created when the audio was uploaded
         * via the HTTP audio upload endpoint.
         */
        private UUID answerId;
    }
}
