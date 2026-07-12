package com.interviewplatform.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementation of the {@link PasswordValidator} constraint.
 *
 * <p>Delegates all policy rules to {@link PasswordPolicy} —
 * this class only applies the rules, never defines them.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
public class PasswordStrengthValidator
        implements ConstraintValidator<PasswordValidator, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return false; // @NotBlank handles the null/blank message separately
        }
        if (password.length() > PasswordPolicy.MAX_LENGTH) {
            return false;
        }
        return password.matches(PasswordPolicy.PATTERN);
    }
}
