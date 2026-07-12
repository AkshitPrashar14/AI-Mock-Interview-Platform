package com.interviewplatform.report.service;

import java.util.UUID;

/**
 * Thrown when a report for the given interview does not exist or has not been generated yet.
 *
 * <p><b>Module:</b> Module 11 — Report Compiler</p>
 */
public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(UUID interviewId) {
        super("Report not found for interview: " + interviewId
              + ". The report may still be generating — please try again shortly.");
    }
}
