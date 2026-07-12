package com.interviewplatform.report.entity;

/**
 * Lifecycle status of a report.
 */
public enum ReportStatus {

    /** Report generation is in progress (Report Compiler Agent running). */
    GENERATING,

    /** Report has been persisted and is ready for the candidate to view. */
    READY,

    /** Report generation failed after maximum retries. Manual review required. */
    FAILED
}
