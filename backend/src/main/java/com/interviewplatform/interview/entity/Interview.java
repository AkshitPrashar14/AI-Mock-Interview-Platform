package com.interviewplatform.interview.entity;

import com.interviewplatform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity representing one interview session.
 *
 * <p>The {@code interviewContext} column stores a JSONB document containing the
 * compressed conversation history that is passed to AI agents. It is treated as
 * an opaque {@code String} at the entity layer; serialisation/deserialisation is
 * handled by the service layer via Jackson.</p>
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@Entity
@Table(name = "interviews")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ── Relationships ─────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false, updatable = false)
    private User candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private InterviewTemplate template;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("transitionedAt ASC")
    @Builder.Default
    private List<InterviewStateHistory> stateHistory = new ArrayList<>();

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionNumber ASC")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    // ── State Machine ─────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 50)
    @Builder.Default
    private InterviewState state = InterviewState.CREATED;

    // ── Interview Configuration ───────────────────────────────────────────────

    @Column(name = "domain", nullable = false, length = 255)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_level", nullable = false, length = 50)
    private RoleLevel roleLevel;

    @Column(name = "total_questions", nullable = false)
    @Builder.Default
    private Integer totalQuestions = 10;

    @Column(name = "current_question_number", nullable = false)
    @Builder.Default
    private Integer currentQuestionNumber = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_difficulty", nullable = false, length = 50)
    @Builder.Default
    private DifficultyLevel currentDifficulty = DifficultyLevel.MEDIUM;

    // ── Interview Context (JSONB stored as String) ────────────────────────────

    /**
     * JSONB column containing the compressed conversation history.
     * Structure: {@code {"turns":[...],"topicsDiscussed":[...],"averageScore":0}}
     * Managed by the service layer — not directly modified by controllers.
     */
    @Column(name = "interview_context", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private String interviewContext = "{\"turns\":[],\"topicsDiscussed\":[],\"averageScore\":0}";

    // ── Running Scores (updated after each evaluation turn) ──────────────────

    @Column(name = "running_technical_score", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal runningTechnicalScore = BigDecimal.ZERO;

    @Column(name = "running_english_score", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal runningEnglishScore = BigDecimal.ZERO;

    @Column(name = "running_behavioral_score", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal runningBehavioralScore = BigDecimal.ZERO;

    @Column(name = "running_composite_score", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal runningCompositeScore = BigDecimal.ZERO;

    // ── Timestamps ────────────────────────────────────────────────────────────

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ── Domain Methods ────────────────────────────────────────────────────────

    /** Returns true if this interview belongs to the given candidate. */
    public boolean isOwnedBy(UUID candidateId) {
        return this.candidate != null && this.candidate.getId().equals(candidateId);
    }

    /** Returns true if more questions remain to be answered. */
    public boolean hasMoreQuestions() {
        return this.currentQuestionNumber < this.totalQuestions;
    }

    /** Increments the current question number and returns the new value. */
    public int advanceQuestion() {
        this.currentQuestionNumber++;
        return this.currentQuestionNumber;
    }
}
