package com.interviewplatform.interview.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Reusable interview configuration template.
 *
 * <p>Templates define the default parameters (domain, role level, question count,
 * duration, scoring weights) for an interview session. A candidate can choose a
 * template or configure manually.</p>
 *
 * <p>{@code weightConfig} JSONB structure:
 * {@code {"technical":0.50,"english":0.25,"behavioral":0.25}}</p>
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@Entity
@Table(name = "interview_templates")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "domain", nullable = false, length = 255)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_level", nullable = false, length = 50)
    private RoleLevel roleLevel;

    @Column(name = "total_questions", nullable = false)
    @Builder.Default
    private Integer totalQuestions = 10;

    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private Integer durationMinutes = 30;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_difficulty", nullable = false, length = 50)
    @Builder.Default
    private DifficultyLevel defaultDifficulty = DifficultyLevel.MEDIUM;

    /**
     * JSONB scoring weight configuration.
     * Defaults: technical=0.50, english=0.25, behavioral=0.25
     */
    @Column(name = "weight_config", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private String weightConfig = "{\"technical\":0.50,\"english\":0.25,\"behavioral\":0.25}";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
