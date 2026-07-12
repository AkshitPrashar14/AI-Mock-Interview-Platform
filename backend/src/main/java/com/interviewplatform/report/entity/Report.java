package com.interviewplatform.report.entity;

import com.interviewplatform.interview.entity.Interview;
import com.interviewplatform.interview.entity.PerformanceTier;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The final assessment report produced after an interview is completed.
 *
 * <p>One-to-one relationship with {@link Interview}. Created by the Report Compiler
 * Agent pipeline after the Evaluation Aggregator has produced final scores.</p>
 *
 * <p><b>Critical design rule:</b> The verdict is computed deterministically by pure
 * Java logic — NOT by the LLM. The LLM only generates narrative text fields
 * ({@code executiveSummary}, {@code strengthHighlights}, {@code improvementAreas},
 * {@code studyPlan}).</p>
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@Entity
@Table(name = "reports")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false, updatable = false)
    private Interview interview;

    // ── Final Aggregated Scores ───────────────────────────────────────────────

    @Column(name = "final_technical_score", precision = 5, scale = 2)
    private BigDecimal finalTechnicalScore;

    @Column(name = "final_english_score", precision = 5, scale = 2)
    private BigDecimal finalEnglishScore;

    @Column(name = "final_behavioral_score", precision = 5, scale = 2)
    private BigDecimal finalBehavioralScore;

    @Column(name = "final_composite_score", precision = 5, scale = 2)
    private BigDecimal finalCompositeScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_tier", length = 50)
    private PerformanceTier finalTier;

    /** Hire recommendation — computed by pure Java, not LLM. */
    @Enumerated(EnumType.STRING)
    @Column(name = "verdict", length = 50)
    private Verdict verdict;

    // ── LLM-Generated Narrative ───────────────────────────────────────────────

    @Column(name = "executive_summary", columnDefinition = "TEXT")
    private String executiveSummary;

    /** JSONB array of strength strings. E.g. {@code ["Strong Java concurrency knowledge","..."]} */
    @Column(name = "strength_highlights", columnDefinition = "jsonb")
    @Builder.Default
    private String strengthHighlights = "[]";

    /**
     * JSONB array of improvement objects.
     * E.g. {@code [{"area":"Concurrency","observation":"...","recommendation":"..."}]}
     */
    @Column(name = "improvement_areas", columnDefinition = "jsonb")
    @Builder.Default
    private String improvementAreas = "[]";

    /** JSONB array of study/practice recommendations. */
    @Column(name = "study_plan", columnDefinition = "jsonb")
    @Builder.Default
    private String studyPlan = "[]";

    @Column(name = "interviewer_notes", columnDefinition = "TEXT")
    private String interviewerNotes;

    // ── Status ────────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false, length = 50)
    @Builder.Default
    private ReportStatus reportStatus = ReportStatus.GENERATING;

    @Column(name = "generated_at")
    private Instant generatedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
