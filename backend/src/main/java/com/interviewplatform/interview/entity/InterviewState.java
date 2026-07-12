package com.interviewplatform.interview.entity;

/**
 * Represents the complete state machine for an interview session.
 *
 * <p>Valid transitions are enforced by {@link com.interviewplatform.interview.service.InterviewStateMachine}.
 * Every transition is persisted in {@code interview_state_history} for full audit trail.</p>
 *
 * <p><b>Terminal states:</b> {@code REPORT_GENERATED}, {@code ABANDONED}, {@code ERROR}</p>
 */
public enum InterviewState {

    /** Interview session record created; no configuration applied yet. */
    CREATED,

    /** Domain, duration, and difficulty configured. Ready to start. */
    CONFIGURED,

    /** Interview officially begun; first question is being generated. */
    STARTED,

    /** Interview Agent has produced the next question. */
    QUESTION_GENERATED,

    /** Question has been pushed to the frontend client. */
    QUESTION_DELIVERED,

    /** Question delivered and acknowledged; waiting for candidate to begin. */
    WAITING_FOR_RESPONSE,

    /** Frontend is actively recording the candidate's audio. */
    LISTENING,

    /** Audio submitted to STT engine; awaiting transcription result. */
    TRANSCRIBING,

    /** Valid transcript received; three evaluation agents running in parallel. */
    EVALUATING,

    /** All agent results received; Aggregator computing composite score (pure Java). */
    AGGREGATING,

    /** Difficulty adjusted; Interview Agent generating the next question. */
    GENERATING_NEXT_QUESTION,

    /** All questions answered or session ended by candidate. */
    COMPLETED,

    /** Report Compiler Agent generating narrative report. */
    REPORT_GENERATING,

    /** Full report persisted and ready for the candidate. Terminal state. */
    REPORT_GENERATED,

    /** Session dropped due to timeout, connection loss, or explicit abort. Terminal state. */
    ABANDONED,

    /** Unrecoverable system error occurred. May transition to ABANDONED on cleanup. */
    ERROR;

    /** Returns true if this is a terminal state (no further transitions possible). */
    public boolean isTerminal() {
        return this == REPORT_GENERATED || this == ABANDONED || this == ERROR;
    }
}
