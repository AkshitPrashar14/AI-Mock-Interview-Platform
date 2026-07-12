package com.interviewplatform.interview.entity;

/**
 * The type of a generated interview question.
 *
 * <p>Used by the Interview Agent to categorize questions and by the
 * Behavioral Agent to apply STAR-method detection for behavioral questions.</p>
 */
public enum QuestionType {

    /** Technical knowledge, algorithms, system design, architecture. */
    TECHNICAL,

    /** "Tell me about a time when..." — experience and soft skills. Evaluated with STAR framework. */
    BEHAVIORAL,

    /** Hypothetical scenario — "What would you do if..." — decision-making focus. */
    SITUATIONAL
}
