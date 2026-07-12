package com.interviewplatform.interview;

import com.interviewplatform.interview.dto.request.CreateInterviewRequest;
import com.interviewplatform.interview.dto.response.InterviewResponse;
import com.interviewplatform.interview.dto.response.InterviewStartResponse;
import com.interviewplatform.interview.entity.*;
import com.interviewplatform.interview.exception.InterviewAlreadyStartedException;
import com.interviewplatform.interview.exception.InterviewNotFoundException;
import com.interviewplatform.interview.exception.InvalidStateTransitionException;
import com.interviewplatform.interview.mapper.InterviewMapper;
import com.interviewplatform.interview.repository.*;
import com.interviewplatform.interview.service.InterviewServiceImpl;
import com.interviewplatform.user.entity.Role;
import com.interviewplatform.user.entity.User;
import com.interviewplatform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link InterviewServiceImpl}.
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewServiceImpl")
class InterviewServiceImplTest {

    @Mock private InterviewRepository interviewRepository;
    @Mock private InterviewTemplateRepository templateRepository;
    @Mock private InterviewStateHistoryRepository stateHistoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private InterviewMapper interviewMapper;

    @InjectMocks
    private InterviewServiceImpl interviewService;

    private UUID candidateId;
    private UUID interviewId;
    private User candidate;
    private Interview interview;

    @BeforeEach
    void setUp() {
        candidateId = UUID.randomUUID();
        interviewId = UUID.randomUUID();

        candidate = User.builder()
                .id(candidateId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .passwordHash("$2a$10$hash")
                .role(Role.USER)
                .isActive(true)
                .build();

        interview = Interview.builder()
                .id(interviewId)
                .candidate(candidate)
                .domain("Java Backend")
                .roleLevel(RoleLevel.SENIOR)
                .totalQuestions(10)
                .state(InterviewState.CREATED)
                .currentDifficulty(DifficultyLevel.MEDIUM)
                .build();
    }

    // =========================================================================
    // State Machine
    // =========================================================================

    @Nested
    @DisplayName("State Machine Transitions")
    class StateMachineTests {

        @Test
        @DisplayName("CREATED → CONFIGURED is a valid transition")
        void createdToConfiguredIsValid() {
            assertThatNoException().isThrownBy(() ->
                com.interviewplatform.interview.service.InterviewStateMachine
                    .assertValidTransition(InterviewState.CREATED, InterviewState.CONFIGURED));
        }

        @Test
        @DisplayName("CREATED → EVALUATING is an invalid transition")
        void createdToEvaluatingIsInvalid() {
            assertThatThrownBy(() ->
                com.interviewplatform.interview.service.InterviewStateMachine
                    .assertValidTransition(InterviewState.CREATED, InterviewState.EVALUATING))
                .isInstanceOf(InvalidStateTransitionException.class);
        }

        @Test
        @DisplayName("REPORT_GENERATED is a terminal state with no outgoing transitions")
        void reportGeneratedIsTerminal() {
            assertThat(InterviewState.REPORT_GENERATED.isTerminal()).isTrue();
            assertThatThrownBy(() ->
                com.interviewplatform.interview.service.InterviewStateMachine
                    .assertValidTransition(InterviewState.REPORT_GENERATED, InterviewState.CREATED))
                .isInstanceOf(InvalidStateTransitionException.class);
        }

        @Test
        @DisplayName("ABANDONED is a terminal state")
        void abandonedIsTerminal() {
            assertThat(InterviewState.ABANDONED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("CREATED is not a terminal state")
        void createdIsNotTerminal() {
            assertThat(InterviewState.CREATED.isTerminal()).isFalse();
        }
    }

    // =========================================================================
    // Difficulty Level
    // =========================================================================

    @Nested
    @DisplayName("DifficultyLevel Adaptation")
    class DifficultyLevelTests {

        @Test
        @DisplayName("EASY increases to MEDIUM")
        void easyIncreasesToMedium() {
            assertThat(DifficultyLevel.EASY.increase()).isEqualTo(DifficultyLevel.MEDIUM);
        }

        @Test
        @DisplayName("EXPERT stays at EXPERT on increase")
        void expertStaysAtExpert() {
            assertThat(DifficultyLevel.EXPERT.increase()).isEqualTo(DifficultyLevel.EXPERT);
        }

        @Test
        @DisplayName("EASY stays at EASY on decrease")
        void easyStaysAtEasy() {
            assertThat(DifficultyLevel.EASY.decrease()).isEqualTo(DifficultyLevel.EASY);
        }

        @Test
        @DisplayName("EXPERT decreases to HARD")
        void expertDecreasesToHard() {
            assertThat(DifficultyLevel.EXPERT.decrease()).isEqualTo(DifficultyLevel.HARD);
        }
    }

    // =========================================================================
    // Performance Tier
    // =========================================================================

    @Nested
    @DisplayName("PerformanceTier.fromScore")
    class PerformanceTierTests {

        @Test @DisplayName("Score 85 → EXCELLENT")
        void score85IsExcellent() {
            assertThat(PerformanceTier.fromScore(85)).isEqualTo(PerformanceTier.EXCELLENT);
        }

        @Test @DisplayName("Score 80 → EXCELLENT (boundary)")
        void score80IsExcellent() {
            assertThat(PerformanceTier.fromScore(80)).isEqualTo(PerformanceTier.EXCELLENT);
        }

        @Test @DisplayName("Score 72 → PROFICIENT")
        void score72IsProficient() {
            assertThat(PerformanceTier.fromScore(72)).isEqualTo(PerformanceTier.PROFICIENT);
        }

        @Test @DisplayName("Score 60 → PROFICIENT (boundary)")
        void score60IsProficient() {
            assertThat(PerformanceTier.fromScore(60)).isEqualTo(PerformanceTier.PROFICIENT);
        }

        @Test @DisplayName("Score 50 → DEVELOPING")
        void score50IsDeveloping() {
            assertThat(PerformanceTier.fromScore(50)).isEqualTo(PerformanceTier.DEVELOPING);
        }

        @Test @DisplayName("Score 40 → DEVELOPING (boundary)")
        void score40IsDeveloping() {
            assertThat(PerformanceTier.fromScore(40)).isEqualTo(PerformanceTier.DEVELOPING);
        }

        @Test @DisplayName("Score 25 → NEEDS_WORK")
        void score25IsNeedsWork() {
            assertThat(PerformanceTier.fromScore(25)).isEqualTo(PerformanceTier.NEEDS_WORK);
        }

        @Test @DisplayName("Score 0 → NEEDS_WORK")
        void score0IsNeedsWork() {
            assertThat(PerformanceTier.fromScore(0)).isEqualTo(PerformanceTier.NEEDS_WORK);
        }
    }

    // =========================================================================
    // Verdict
    // =========================================================================

    @Nested
    @DisplayName("Verdict.fromScore")
    class VerdictTests {

        @Test @DisplayName("Score 82 → STRONGLY_CONSIDER")
        void score82IsStronglyConsider() {
            assertThat(com.interviewplatform.report.entity.Verdict.fromScore(82))
                .isEqualTo(com.interviewplatform.report.entity.Verdict.STRONGLY_CONSIDER);
        }

        @Test @DisplayName("Score 65 → CONSIDER")
        void score65IsConsider() {
            assertThat(com.interviewplatform.report.entity.Verdict.fromScore(65))
                .isEqualTo(com.interviewplatform.report.entity.Verdict.CONSIDER);
        }

        @Test @DisplayName("Score 45 → FURTHER_ROUNDS")
        void score45IsFurtherRounds() {
            assertThat(com.interviewplatform.report.entity.Verdict.fromScore(45))
                .isEqualTo(com.interviewplatform.report.entity.Verdict.FURTHER_ROUNDS);
        }

        @Test @DisplayName("Score 30 → NOT_RECOMMENDED")
        void score30IsNotRecommended() {
            assertThat(com.interviewplatform.report.entity.Verdict.fromScore(30))
                .isEqualTo(com.interviewplatform.report.entity.Verdict.NOT_RECOMMENDED);
        }
    }

    // =========================================================================
    // createInterview
    // =========================================================================

    @Nested
    @DisplayName("createInterview")
    class CreateInterviewTests {

        @Test
        @DisplayName("Creates interview and returns response with CREATED state")
        void createsInterview() {
            CreateInterviewRequest request = new CreateInterviewRequest();
            request.setDomain("Java Backend");
            request.setRoleLevel(RoleLevel.SENIOR);
            request.setTotalQuestions(10);
            request.setStartingDifficulty(DifficultyLevel.MEDIUM);

            when(userRepository.findById(candidateId)).thenReturn(Optional.of(candidate));
            when(interviewRepository.save(any(Interview.class))).thenAnswer(inv -> {
                Interview i = inv.getArgument(0);
                i.setId(interviewId);
                return i;
            });

            InterviewResponse expected = InterviewResponse.builder()
                    .interviewId(interviewId)
                    .state(InterviewState.CREATED)
                    .build();
            when(interviewMapper.toResponse(any())).thenReturn(expected);

            InterviewResponse result = interviewService.createInterview(request, candidateId);

            assertThat(result.getInterviewId()).isEqualTo(interviewId);
            assertThat(result.getState()).isEqualTo(InterviewState.CREATED);
            verify(interviewRepository).save(any(Interview.class));
        }
    }

    // =========================================================================
    // startInterview
    // =========================================================================

    @Nested
    @DisplayName("startInterview")
    class StartInterviewTests {

        @Test
        @DisplayName("Throws InterviewAlreadyStartedException when interview is STARTED")
        void throwsWhenAlreadyStarted() {
            interview.setState(InterviewState.STARTED);
            when(interviewRepository.findByIdWithCandidate(interviewId))
                .thenReturn(Optional.of(interview));

            assertThatThrownBy(() -> interviewService.startInterview(interviewId, candidateId))
                .isInstanceOf(InterviewAlreadyStartedException.class);
        }

        @Test
        @DisplayName("Throws InterviewNotFoundException when interview not found")
        void throwsWhenNotFound() {
            when(interviewRepository.findByIdWithCandidate(interviewId))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> interviewService.startInterview(interviewId, candidateId))
                .isInstanceOf(InterviewNotFoundException.class);
        }

        @Test
        @DisplayName("Throws InterviewNotFoundException when interview not owned by candidate")
        void throwsWhenNotOwned() {
            interview.setCandidate(User.builder().id(UUID.randomUUID()).build()); // different owner
            when(interviewRepository.findByIdWithCandidate(interviewId))
                .thenReturn(Optional.of(interview));

            assertThatThrownBy(() -> interviewService.startInterview(interviewId, candidateId))
                .isInstanceOf(InterviewNotFoundException.class);
        }
    }

    // =========================================================================
    // getInterview
    // =========================================================================

    @Nested
    @DisplayName("getInterview")
    class GetInterviewTests {

        @Test
        @DisplayName("Returns interview response for valid owner")
        void returnsInterviewForOwner() {
            when(interviewRepository.findByIdWithCandidate(interviewId))
                .thenReturn(Optional.of(interview));
            InterviewResponse expected = InterviewResponse.builder().interviewId(interviewId).build();
            when(interviewMapper.toResponse(interview)).thenReturn(expected);

            InterviewResponse result = interviewService.getInterview(interviewId, candidateId);

            assertThat(result.getInterviewId()).isEqualTo(interviewId);
        }

        @Test
        @DisplayName("Throws 404 for non-existent interview")
        void throwsForNonExistent() {
            when(interviewRepository.findByIdWithCandidate(interviewId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> interviewService.getInterview(interviewId, candidateId))
                .isInstanceOf(InterviewNotFoundException.class);
        }
    }
}
