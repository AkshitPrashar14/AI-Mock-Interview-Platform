package com.interviewplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA Auditing across the application.
 *
 * <p>Once this configuration is active, fields annotated with
 * {@code @CreatedDate} and {@code @LastModifiedDate} are automatically
 * populated by Hibernate on insert and update respectively.
 * Manual timestamp assignment in entities or services is no longer needed.</p>
 *
 * <p><b>Usage in entities:</b></p>
 * <pre>
 *   {@code @EntityListeners(AuditingEntityListener.class)}
 *   public class User {
 *       {@code @CreatedDate}
 *       private Instant createdAt;
 *       {@code @LastModifiedDate}
 *       private Instant updatedAt;
 *   }
 * </pre>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // No additional configuration required.
    // @EnableJpaAuditing registers the AuditingEntityListener globally.
}
