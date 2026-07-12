package com.interviewplatform.agents.common;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Common input contract for all AI evaluation agents.
 *
 * <p>Agents receive a reference to the interview and answer, plus the
 * transcript text they must evaluate.</p>
 *
 * <p><b>Module:</b> Module 6 — Interview Agent (common infrastructure)</p>
 */
@Data
@Builder
public class AgentInput {

    /** Interview session ID. */
    private UUID interviewId;

    /** Answer record ID being evaluated. */
    private UUID answerId;

    /** Sequential question number (1-based). */
    private int questionNumber;

    /** The question text that was posed to the candidate. */
    private String questionText;

    /** The type of question (TECHNICAL, BEHAVIORAL, SITUATIONAL). */
    private String questionType;

    /** Difficulty level of this question. */
    private String difficulty;

    /** The candidate's transcribed answer text. */
    private String transcript;

    /** Interview domain (e.g. "Java Backend", "Python ML"). */
    private String domain;

    /** Role level the candidate is applying for. */
    private String roleLevel;

    /**
     * Compressed conversation history for context (may be null or empty
     * for the first question).
     */
    private String conversationHistory;
}
