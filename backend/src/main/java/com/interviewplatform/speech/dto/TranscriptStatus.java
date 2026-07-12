package com.interviewplatform.speech.dto;

/**
 * Status of a speech transcription attempt.
 *
 * <p><b>Module:</b> Module 4 — Speech Module Integration</p>
 */
public enum TranscriptStatus {

    /**
     * Audio was successfully transcribed and the transcript text is non-empty.
     */
    VALID,

    /**
     * Audio was transcribed but the result is too short or contains no intelligible speech.
     */
    INVALID,

    /**
     * The STT service did not respond within the configured timeout window.
     */
    TIMEOUT,

    /**
     * The STT service is temporarily unavailable (circuit breaker open or HTTP 5xx).
     */
    SERVICE_UNAVAILABLE
}
