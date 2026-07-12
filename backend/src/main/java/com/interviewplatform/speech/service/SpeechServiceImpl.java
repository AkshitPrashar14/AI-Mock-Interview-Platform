package com.interviewplatform.speech.service;

import com.interviewplatform.speech.config.SpeechServiceProperties;
import com.interviewplatform.speech.dto.TranscriptionResult;
import com.interviewplatform.speech.dto.TranscriptStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP client implementation of {@link SpeechService}.
 *
 * <h3>Fault Tolerance</h3>
 * <ul>
 *   <li>Resilience4j {@code @Retry} — 3 attempts with 2 s backoff on {@link ResourceAccessException}</li>
 *   <li>Resilience4j {@code @CircuitBreaker} — opens after 50% failure rate over 10 calls</li>
 *   <li>A fallback method returns {@link TranscriptStatus#SERVICE_UNAVAILABLE} when the
 *       circuit is open or all retries are exhausted.</li>
 * </ul>
 *
 * <h3>Python API contract</h3>
 * <pre>
 * POST /transcribe
 * Content-Type: multipart/form-data
 * Fields: audio (file bytes), format (string)
 *
 * Response (200):
 * {
 *   "transcript": "...",
 *   "confidence": 0.95,
 *   "duration_seconds": 12.5
 * }
 * </pre>
 *
 * <p><b>Module:</b> Module 4 — Speech Module Integration</p>
 */
@Slf4j
@Service
public class SpeechServiceImpl implements SpeechService {

    private static final String CB_NAME = "speech-service";

    private final RestTemplate restTemplate;
    private final SpeechServiceProperties props;

    public SpeechServiceImpl(SpeechServiceProperties props, RestTemplateBuilder builder) {
        this.props = props;
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(props.getTimeoutSeconds()))
                .build();
    }

    // =========================================================================
    // transcribe
    // =========================================================================

    @Override
    @Retry(name = CB_NAME, fallbackMethod = "transcribeFallback")
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "transcribeFallback")
    public TranscriptionResult transcribe(UUID answerId, byte[] audioBytes, String format) {
        log.info("SpeechService.transcribe: answerId={}, format={}, bytes={}",
                answerId, format, audioBytes != null ? audioBytes.length : 0);

        String url = props.getUrl() + "/transcribe";

        // Build multipart request
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
            @Override public String getFilename() { return "audio." + format; }
        };
        body.add("audio", audioResource);
        body.add("format", format);
        body.add("answer_id", answerId.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseSuccessResponse(answerId, response.getBody());
            } else {
                log.warn("SpeechService: non-2xx response: status={}", response.getStatusCode());
                return TranscriptionResult.unavailable(answerId,
                        "STT service returned: " + response.getStatusCode());
            }
        } catch (ResourceAccessException ex) {
            log.warn("SpeechService: connection error: {}", ex.getMessage());
            throw ex; // Allow Retry to retry
        } catch (Exception ex) {
            log.error("SpeechService: unexpected error: {}", ex.getMessage(), ex);
            return TranscriptionResult.unavailable(answerId, ex.getMessage());
        }
    }

    // =========================================================================
    // Fallback
    // =========================================================================

    /**
     * Called when all retry attempts are exhausted or the circuit breaker is open.
     */
    @SuppressWarnings("unused")
    public TranscriptionResult transcribeFallback(UUID answerId, byte[] audioBytes,
                                                   String format, Exception ex) {
        log.warn("SpeechService.transcribeFallback: answerId={}, reason={}", answerId, ex.getMessage());

        if (ex instanceof ResourceAccessException) {
            return TranscriptionResult.timeout(answerId);
        }
        return TranscriptionResult.unavailable(answerId,
                "Speech service is temporarily unavailable: " + ex.getMessage());
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    @SuppressWarnings("unchecked")
    private TranscriptionResult parseSuccessResponse(UUID answerId, Map<?, ?> body) {
        String transcript = (String) body.get("transcript");

        if (transcript == null || transcript.trim().length() < props.getMinTranscriptLength()) {
            log.info("SpeechService: transcript too short ({}), marking INVALID", 
                      transcript != null ? transcript.length() : 0);
            return TranscriptionResult.invalid(answerId,
                    "Transcript too short or empty — please re-record your answer");
        }

        Double confidence = body.get("confidence") instanceof Number n ? n.doubleValue() : null;
        Double duration   = body.get("duration_seconds") instanceof Number n ? n.doubleValue() : null;

        return TranscriptionResult.success(answerId, transcript.trim(),
                confidence != null ? confidence : 0.0,
                duration   != null ? duration   : 0.0);
    }
}
