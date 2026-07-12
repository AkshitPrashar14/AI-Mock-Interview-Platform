package com.interviewplatform.interview.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when attempting to start an interview that has already been started.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InterviewAlreadyStartedException extends RuntimeException {

    public InterviewAlreadyStartedException() {
        super("INTERVIEW_ALREADY_STARTED");
    }
}
