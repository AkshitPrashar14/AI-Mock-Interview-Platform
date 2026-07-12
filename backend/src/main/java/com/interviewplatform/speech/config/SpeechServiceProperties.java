package com.interviewplatform.speech.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Typed configuration properties for the Speech Service HTTP client.
 *
 * <p>Values are bound from the {@code speech.service.*} namespace in
 * {@code application.yml}.</p>
 *
 * <p><b>Module:</b> Module 4 — Speech Module Integration</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "speech.service")
public class SpeechServiceProperties {

    /** Base URL of the Python FastAPI speech-service. */
    private String url = "http://localhost:8001";

    /** Maximum seconds to wait for a transcription response before TIMEOUT. */
    private int timeoutSeconds = 30;

    /** Maximum number of retry attempts on transient failure. */
    private int retryMaxAttempts = 3;

    /** Milliseconds between retry attempts (exponential backoff applied externally). */
    private long retryBackoffMs = 2000;

    /**
     * Minimum transcript length in characters.
     * Transcripts shorter than this are considered INVALID.
     */
    private int minTranscriptLength = 10;
}
