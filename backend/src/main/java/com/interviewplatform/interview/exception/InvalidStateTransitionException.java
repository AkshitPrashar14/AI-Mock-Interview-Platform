package com.interviewplatform.interview.exception;

import com.interviewplatform.interview.entity.InterviewState;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a state transition is requested that is not permitted by the state machine rules.
 *
 * <p>Examples: attempting to start an already-started interview,
 * or submitting an answer when not in {@code WAITING_FOR_RESPONSE} state.</p>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(InterviewState current, InterviewState requested) {
        super("Invalid state transition from [" + current + "] to [" + requested + "]");
    }

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
