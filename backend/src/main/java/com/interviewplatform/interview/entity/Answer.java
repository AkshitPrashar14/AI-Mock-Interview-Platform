package com.interviewplatform.interview.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * One candidate answer (audio + transcript) for a given question.
 *
 * <p>The audio file is stored on disk (or object storage in production).
 * Only the relative path is persisted here. The transcript is produced by the
 * STT service and validated before being passed to evaluation agents.</p>
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@Entity
@Table(name = "answers")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false, updatable = false)
    private Interview interview;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false, updatable = false)
    private Question question;

    // ── Audio ─────────────────────────────────────────────────────────────────

    @Column(name = "audio_file_path", length = 1000)
    private String audioFilePath;

    @Column(name = "audio_format", length = 20)
    @Builder.Default
    private String audioFormat = "WEBM";

    @Column(name = "audio_duration_seconds")
    private Integer audioDurationSeconds;

    // ── Transcript ────────────────────────────────────────────────────────────

    @Column(name = "transcript", columnDefinition = "TEXT")
    private String transcript;

    @Enumerated(EnumType.STRING)
    @Column(name = "transcript_status", nullable = false, length = 50)
    @Builder.Default
    private TranscriptStatus transcriptStatus = TranscriptStatus.PENDING;

    @Column(name = "stt_confidence", precision = 5, scale = 4)
    private BigDecimal sttConfidence;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    // ── Timestamps ────────────────────────────────────────────────────────────

    @Column(name = "recorded_at")
    private Instant recordedAt;

    @Column(name = "transcribed_at")
    private Instant transcribedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToOne(mappedBy = "answer", cascade = CascadeType.ALL)
    private Evaluation evaluation;
}
