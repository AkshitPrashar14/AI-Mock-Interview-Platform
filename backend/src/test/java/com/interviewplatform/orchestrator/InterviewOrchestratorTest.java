package com.interviewplatform.orchestrator;

import com.interviewplatform.interview.entity.DifficultyLevel;
import com.interviewplatform.interview.entity.InterviewState;
import com.interviewplatform.interview.service.InterviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link InterviewOrchestrator}.
 *
 * <p><b>Module:</b> Module 3 — Interview Orchestrator</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewOrchestrator")
class InterviewOrchestratorTest {

    @Mock private InterviewService interviewService;
    @Mock private DifficultyManager difficultyManager;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private InterviewOrchestrator orchestrator;

    private final UUID interviewId = UUID.randomUUID();
    private final UUID answerId    = UUID.randomUUID();

    // =========================================================================
    // handleAudioSubmitted
    // =========================================================================

    @Nested
    @DisplayName("handleAudioSubmitted")
    class HandleAudioSubmittedTests {

        @Test
        @DisplayName("Transitions to TRANSCRIBING and broadcasts state")
        void transitionsToTranscribing() {
            orchestrator.handleAudioSubmitted(interviewId, answerId);

            verify(interviewService).transitionState(
                    eq(interviewId), eq(InterviewState.TRANSCRIBING), eq("AUDIO_SUBMITTED"), anyString());

            ArgumentCaptor<String> destCaptor = ArgumentCaptor.forClass(String.class);
            verify(messagingTemplate).convertAndSend(destCaptor.capture(), any(InterviewStateEvent.class));
            assertThat(destCaptor.getValue()).isEqualTo("/topic/interview/" + interviewId + "/state");
        }
    }

    // =========================================================================
    // handleTranscriptReady
    // =========================================================================

    @Nested
    @DisplayName("handleTranscriptReady")
    class HandleTranscriptReadyTests {

        @Test
        @DisplayName("Transitions to EVALUATING and broadcasts state")
        void transitionsToEvaluating() {
            orchestrator.handleTranscriptReady(interviewId, answerId, "My answer text");

            verify(interviewService).transitionState(
                    eq(interviewId), eq(InterviewState.EVALUATING), eq("TRANSCRIPT_READY"), anyString());

            verify(messagingTemplate).convertAndSend(anyString(), any(InterviewStateEvent.class));
        }
    }

    // =========================================================================
    // handleEvaluationComplete — last question
    // =========================================================================

    @Nested
    @DisplayName("handleEvaluationComplete — last question")
    class HandleEvaluationCompleteLastQuestionTests {

        @Test
        @DisplayName("Transitions to COMPLETED then REPORT_GENERATING when all questions answered")
        void completesInterviewOnLastQuestion() {
            int totalQuestions = 5;
            int currentQuestion = 5; // last question

            orchestrator.handleEvaluationComplete(
                    interviewId, 85, List.of(80, 85), DifficultyLevel.HARD,
                    currentQuestion, totalQuestions);

            verify(interviewService).transitionState(
                    eq(interviewId), eq(InterviewState.AGGREGATING), anyString(), anyString());
            verify(interviewService).transitionState(
                    eq(interviewId), eq(InterviewState.COMPLETED), anyString(), anyString());
            verify(interviewService).transitionState(
                    eq(interviewId), eq(InterviewState.REPORT_GENERATING), anyString(), anyString());

            // DifficultyManager should NOT be called on the last question
            verifyNoInteractions(difficultyManager);
        }
    }

    // =========================================================================
    // handleEvaluationComplete — not last question
    // =========================================================================

    @Nested
    @DisplayName("handleEvaluationComplete — not last question")
    class HandleEvaluationCompleteNotLastTests {

        @Test
        @DisplayName("Transitions to GENERATING_NEXT_QUESTION and adapts difficulty")
        void generatesNextQuestionWithAdaptedDifficulty() {
            when(difficultyManager.nextDifficulty(any(), any())).thenReturn(DifficultyLevel.EXPERT);

            orchestrator.handleEvaluationComplete(
                    interviewId, 90, List.of(90, 90), DifficultyLevel.HARD,
                    2, 5); // question 2 of 5

            verify(interviewService).transitionState(
                    eq(interviewId), eq(InterviewState.AGGREGATING), anyString(), anyString());
            verify(interviewService).transitionState(
                    eq(interviewId), eq(InterviewState.GENERATING_NEXT_QUESTION), anyString(), anyString());

            verify(difficultyManager).nextDifficulty(eq(DifficultyLevel.HARD), eq(List.of(90, 90)));

            // Should NOT complete yet
            verify(interviewService, never()).transitionState(
                    eq(interviewId), eq(InterviewState.COMPLETED), anyString(), anyString());
        }
    }

    // =========================================================================
    // handleInterviewComplete
    // =========================================================================

    @Nested
    @DisplayName("handleInterviewComplete")
    class HandleInterviewCompleteTests {

        @Test
        @DisplayName("Transitions to REPORT_GENERATING and broadcasts state")
        void transitionsToReportGenerating() {
            orchestrator.handleInterviewComplete(interviewId);

            verify(interviewService).transitionState(
                    eq(interviewId), eq(InterviewState.REPORT_GENERATING), eq("REPORT_REQUESTED"), anyString());

            verify(messagingTemplate).convertAndSend(anyString(), any(InterviewStateEvent.class));
        }
    }
}
