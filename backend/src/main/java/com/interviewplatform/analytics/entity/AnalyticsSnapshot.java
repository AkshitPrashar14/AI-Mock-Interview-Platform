package com.interviewplatform.analytics.entity;

import com.interviewplatform.interview.entity.PerformanceTier;
import com.interviewplatform.report.entity.Verdict;
import com.interviewplatform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Pre-computed analytics snapshot per candidate.
 *
 * <p>Updated after every interview is completed (not computed on the fly at request time).
 * One-to-one with a user. Enables fast dashboard queries without expensive aggregations.</p>
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@Entity
@Table(name = "analytics_snapshots")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false, updatable = false)
    private User candidate;

    @Column(name = "total_interviews", nullable = false)
    @Builder.Default
    private Integer totalInterviews = 0;

    @Column(name = "avg_technical_score", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal avgTechnicalScore = BigDecimal.ZERO;

    @Column(name = "avg_english_score", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal avgEnglishScore = BigDecimal.ZERO;

    @Column(name = "avg_behavioral_score", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal avgBehavioralScore = BigDecimal.ZERO;

    @Column(name = "avg_composite_score", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal avgCompositeScore = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "best_performance_tier", length = 50)
    private PerformanceTier bestPerformanceTier;

    @Enumerated(EnumType.STRING)
    @Column(name = "most_recent_verdict", length = 50)
    private Verdict mostRecentVerdict;

    @Column(name = "most_practiced_domain", length = 255)
    private String mostPracticedDomain;

    @Column(name = "last_computed_at")
    private Instant lastComputedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
