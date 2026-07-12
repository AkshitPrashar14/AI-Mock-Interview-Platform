package com.interviewplatform.interview.entity;

/**
 * Seniority / role level for an interview session.
 *
 * <p>Drives question complexity expectations and the Interview Agent's
 * framing of the interview domain.</p>
 */
public enum RoleLevel {

    /** 0–2 years of experience — foundational concepts expected. */
    JUNIOR,

    /** 2–5 years of experience — solid applied knowledge expected. */
    MID,

    /** 5–8 years of experience — depth, trade-offs, and design expected. */
    SENIOR,

    /** 8–12 years — team leadership, architectural decisions expected. */
    LEAD,

    /** 12+ years — org-wide technical direction, complex system design expected. */
    PRINCIPAL
}
