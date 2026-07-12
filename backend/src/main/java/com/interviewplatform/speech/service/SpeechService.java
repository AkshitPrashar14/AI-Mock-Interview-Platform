package com.interviewplatform.speech.service;

import com.interviewplatform.speech.dto.TranscriptionResult;

import java.util.UUID;

/**
 * Contract for the speech-to-text transcription service.
 *
 * <p>The default implementation ({@link SpeechServiceImpl}) calls the Python
 * FastAPI {@code speech-service} via HTTP with Resilience4j fault tolerance.
 * A stub implementation can be swapped in for local development without the
 * Python service running.</p>
 *
 * <p><b>Module:</b> Module 4 — Speech Module Integration</p>
 */
public interface SpeechService {

    /**
     * Transcribes the audio for the specified answer record.
     *
     * @param answerId   the ID of the {@code answers} record holding the audio
     * @param audioBytes raw audio bytes to transcribe
     * @param format     audio format hint (e.g. {@code "webm"}, {@code "wav"}, {@code "mp3"})
     * @return transcription result with status and text
     */
    TranscriptionResult transcribe(UUID answerId, byte[] audioBytes, String format);
}
