package com.interviewplatform.interview.entity;

/**
 * Status of the transcription result for a candidate's audio answer.
 */
public enum TranscriptStatus {

    /** Awaiting STT processing. */
    PENDING,

    /** Transcript meets quality threshold — passed to evaluation agents. */
    VALID,

    /** Transcript below quality threshold or empty — candidate prompted to re-answer. */
    INVALID,

    /** Candidate reached maximum retries; turn was skipped. */
    SKIPPED
}
