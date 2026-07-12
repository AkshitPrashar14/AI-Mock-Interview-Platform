package com.interviewplatform.interview.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Thrown when an interview session cannot be found or does not belong to the requesting candidate.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class InterviewNotFoundException extends RuntimeException {

    public InterviewNotFoundException(UUID interviewId) {
        super("Interview not found: " + interviewId);
    }
}
