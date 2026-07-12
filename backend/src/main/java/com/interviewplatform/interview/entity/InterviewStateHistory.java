package com.interviewplatform.interview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Append-only audit record of every interview state transition.
 *
 * <p><b>Rules:</b>
 * <ul>
 *   <li>Never UPDATE a row in this table — only INSERT</li>
 *   <li>Every call to {@link com.interviewplatform.interview.service.InterviewService#transitionState}
 *       inserts exactly one row</li>
 * </ul>
 * </p>
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@Entity
@Table(name = "interview_state_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewStateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false, updatable = false)
    private Interview interview;

    @Column(name = "previous_state", length = 50)
    private String previousState;

    @Column(name = "current_state", nullable = false, length = 50)
    private String currentState;

    @Column(name = "transition_event", nullable = false, length = 100)
    private String transitionEvent;

    @Column(name = "transitioned_by", nullable = false, length = 100)
    @Builder.Default
    private String transitionedBy = "SYSTEM";

    @Column(name = "transition_reason", columnDefinition = "TEXT")
    private String transitionReason;

    @Column(name = "transitioned_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant transitionedAt = Instant.now();
}
