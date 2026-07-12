package com.interviewplatform.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for password strength.
 *
 * <p>Uses {@link PasswordStrengthValidator} for constraint logic and
 * references {@link PasswordPolicy} for the actual rules — the annotation
 * itself contains no business logic.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   {@code @PasswordValidator}
 *   private String password;
 * </pre>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Documented
@Constraint(validatedBy = PasswordStrengthValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordValidator {

    String message() default PasswordPolicy.MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
