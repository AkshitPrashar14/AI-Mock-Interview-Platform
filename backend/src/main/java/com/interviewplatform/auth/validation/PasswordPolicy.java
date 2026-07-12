package com.interviewplatform.auth.validation;

/**
 * Centralized password policy constants.
 *
 * <p>All password strength rules live here. Updating a rule here automatically
 * affects all validators that reference these constants — no DTO changes needed.</p>
 *
 * <p><b>Current policy:</b></p>
 * <ul>
 *   <li>Minimum 8 characters</li>
 *   <li>At least one uppercase letter (A-Z)</li>
 *   <li>At least one digit (0-9)</li>
 *   <li>At least one special character (@$!%*?&amp;#^)</li>
 * </ul>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
public final class PasswordPolicy {

    private PasswordPolicy() {
        // Utility class — no instantiation
    }

    /** Minimum password length. */
    public static final int MIN_LENGTH = 8;

    /** Maximum password length (prevents bcrypt truncation issues at 72 chars). */
    public static final int MAX_LENGTH = 72;

    /**
     * Password strength regex pattern.
     *
     * <p>Requires:</p>
     * <ul>
     *   <li>At least one uppercase letter</li>
     *   <li>At least one digit</li>
     *   <li>At least one special character</li>
     *   <li>Minimum {@link #MIN_LENGTH} characters total</li>
     * </ul>
     */
    public static final String PATTERN =
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^])[A-Za-z\\d@$!%*?&#^]{" + MIN_LENGTH + ",}$";

    /** User-facing error message when validation fails. */
    public static final String MESSAGE =
            "Password must be at least 8 characters and contain at least " +
            "one uppercase letter, one digit, and one special character (@$!%*?&#^)";
}
