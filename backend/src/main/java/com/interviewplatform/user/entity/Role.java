package com.interviewplatform.user.entity;

/**
 * User roles in the AI Mock Interview Platform.
 *
 * <p>V1 only supports {@link #USER}. {@link #ADMIN} and {@link #RECRUITER}
 * are included in the model now to avoid a schema change later.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
public enum Role {

    /** Standard candidate account — can create and take interviews. */
    USER,

    /** Platform administrator — full access to all resources. */
    ADMIN,

    /**
     * Recruiter account — planned for V3.
     * Read-only access to candidate reports they are shared on.
     */
    RECRUITER
}
