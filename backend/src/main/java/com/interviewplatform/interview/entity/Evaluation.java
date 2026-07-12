package com.interviewplatform.interview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * AI evaluation result for one answer — produced by the three parallel agents and
 * deterministically aggregated by the {@code EvaluationAggregator}.
 *
 * <p>Each {@code *_subscores} column is a JSONB map, e.g.:
 * {@code {"correctness":80,"depth":70,"problemSolving":65,"completeness":55}}</p>
 *
 * <p>{@code isDegraded} is set to {@code true} when at least one agent failed or
 * timed out. The report will be flagged with {@code confidence=LOW} in that case.</p>
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@Entity
@Table(name = "evaluations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false, updatable = false)
    private Interview interview;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_id", nullable = false, updatable = false)
    private Answer answer;

    // ── Technical Agent Output ────────────────────────────────────────────────

    @Column(name = "technical_score")
    private Integer technicalScore;

    @Column(name = "technical_subscores", columnDefinition = "jsonb")
    @Builder.Default
    private String technicalSubscores = "{}";

    @Column(name = "technical_feedback", columnDefinition = "TEXT")
    private String technicalFeedback;

    // ── English Agent Output ──────────────────────────────────────────────────

    @Column(name = "english_score")
    private Integer englishScore;

    @Column(name = "english_subscores", columnDefinition = "jsonb")
    @Builder.Default
    private String englishSubscores = "{}";

    @Column(name = "english_feedback", columnDefinition = "TEXT")
    private String englishFeedback;

    // ── Behavioral Agent Output ───────────────────────────────────────────────

    @Column(name = "behavioral_score")
    private Integer behavioralScore;

    @Column(name = "behavioral_subscores", columnDefinition = "jsonb")
    @Builder.Default
    private String behavioralSubscores = "{}";

    @Column(name = "behavioral_feedback", columnDefinition = "TEXT")
    private String behavioralFeedback;

    // ── Aggregated Result (pure Java — no LLM) ────────────────────────────────

    @Column(name = "composite_score", precision = 5, scale = 2)
    private BigDecimal compositeScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "performance_tier", length = 50)
    private PerformanceTier performanceTier;

    @Column(name = "is_degraded", nullable = false)
    @Builder.Default
    private Boolean isDegraded = false;

    // ── Performance Metrics ───────────────────────────────────────────────────

    @Column(name = "technical_processing_ms")
    private Integer technicalProcessingMs;

    @Column(name = "english_processing_ms")
    private Integer englishProcessingMs;

    @Column(name = "behavioral_processing_ms")
    private Integer behavioralProcessingMs;

    @Column(name = "evaluated_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant evaluatedAt = Instant.now();
}
