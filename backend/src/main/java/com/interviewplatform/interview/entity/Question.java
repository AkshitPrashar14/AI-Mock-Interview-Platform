package com.interviewplatform.interview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * One generated interview question within a session.
 *
 * <p>{@code expectedKeyPoints} and {@code followUpHints} are JSONB arrays used by
 * the Technical Evaluation Agent to assess completeness and guide the Interview Agent
 * on potential follow-ups.</p>
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@Entity
@Table(name = "questions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false, updatable = false)
    private Interview interview;

    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 50)
    @Builder.Default
    private QuestionType questionType = QuestionType.TECHNICAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 50)
    @Builder.Default
    private DifficultyLevel difficulty = DifficultyLevel.MEDIUM;

    /**
     * JSONB array of expected answer key points.
     * Example: {@code ["garbage collection phases","generational hypothesis","stop-the-world events"]}
     */
    @Column(name = "expected_key_points", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private String expectedKeyPoints = "[]";

    /**
     * JSONB array of possible follow-up questions for this topic.
     */
    @Column(name = "follow_up_hints", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private String followUpHints = "[]";

    @Column(name = "generated_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant generatedAt = Instant.now();

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL)
    private Answer answer;
}
