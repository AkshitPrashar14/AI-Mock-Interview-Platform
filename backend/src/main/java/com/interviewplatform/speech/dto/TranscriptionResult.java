package com.interviewplatform.speech.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Result of a speech-to-text transcription request.
 *
 * <p><b>Module:</b> Module 4 — Speech Module Integration</p>
 */
@Data
@Builder
public class TranscriptionResult {

    /**
     * The answer record ID this transcription belongs to.
     */
    private UUID answerId;

    /**
     * Transcription outcome status.
     */
    private TranscriptStatus status;

    /**
     * The transcribed text. Non-null only when {@code status == VALID}.
     */
    private String transcript;

    /**
     * Confidence score from the Whisper model (0.0–1.0).
     * Present only when {@code status == VALID}.
     */
    private Double confidence;

    /**
     * Duration of the audio in seconds as reported by the STT service.
     */
    private Double audioDurationSeconds;

    /**
     * Raw error message if the call failed. {@code null} on success.
     */
    private String errorMessage;

    // =========================================================================
    // Factory helpers
    // =========================================================================

    /** Creates a successful transcription result. */
    public static TranscriptionResult success(UUID answerId, String transcript,
                                               double confidence, double audioDurationSeconds) {
        return TranscriptionResult.builder()
                .answerId(answerId)
                .status(TranscriptStatus.VALID)
                .transcript(transcript)
                .confidence(confidence)
                .audioDurationSeconds(audioDurationSeconds)
                .build();
    }

    /** Creates an INVALID result (audio too short or unintelligible). */
    public static TranscriptionResult invalid(UUID answerId, String reason) {
        return TranscriptionResult.builder()
                .answerId(answerId)
                .status(TranscriptStatus.INVALID)
                .errorMessage(reason)
                .build();
    }

    /** Creates a TIMEOUT result. */
    public static TranscriptionResult timeout(UUID answerId) {
        return TranscriptionResult.builder()
                .answerId(answerId)
                .status(TranscriptStatus.TIMEOUT)
                .errorMessage("STT service did not respond within the timeout window")
                .build();
    }

    /** Creates a SERVICE_UNAVAILABLE result. */
    public static TranscriptionResult unavailable(UUID answerId, String reason) {
        return TranscriptionResult.builder()
                .answerId(answerId)
                .status(TranscriptStatus.SERVICE_UNAVAILABLE)
                .errorMessage(reason)
                .build();
    }
}
