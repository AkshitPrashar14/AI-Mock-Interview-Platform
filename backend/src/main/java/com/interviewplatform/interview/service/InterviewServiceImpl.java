package com.interviewplatform.interview.service;

import com.interviewplatform.interview.dto.request.CreateInterviewRequest;
import com.interviewplatform.interview.dto.response.InterviewResponse;
import com.interviewplatform.interview.dto.response.InterviewStartResponse;
import com.interviewplatform.interview.dto.response.InterviewSummaryResponse;
import com.interviewplatform.interview.entity.*;
import com.interviewplatform.interview.exception.InterviewAlreadyStartedException;
import com.interviewplatform.interview.exception.InterviewNotFoundException;
import com.interviewplatform.interview.mapper.InterviewMapper;
import com.interviewplatform.interview.repository.*;
import com.interviewplatform.user.entity.User;
import com.interviewplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Default implementation of {@link InterviewService}.
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewTemplateRepository templateRepository;
    private final InterviewStateHistoryRepository stateHistoryRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;
    private final InterviewMapper interviewMapper;

    // =========================================================================
    // Create
    // =========================================================================

    @Override
    @Transactional
    public InterviewResponse createInterview(CreateInterviewRequest request, UUID candidateId) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalStateException("Candidate not found: " + candidateId));

        InterviewTemplate template = resolveTemplate(request);

        Interview interview = Interview.builder()
                .candidate(candidate)
                .template(template)
                .domain(request.getDomain())
                .roleLevel(request.getRoleLevel())
                .totalQuestions(request.getTotalQuestions())
                .currentDifficulty(request.getStartingDifficulty())
                .state(InterviewState.CREATED)
                .build();

        interview = interviewRepository.save(interview);

        // Persist initial state history
        appendStateHistory(interview, null, InterviewState.CREATED, "SESSION_CREATED", "Interview session initialised");

        log.info("Interview created: id={}, candidateId={}, domain={}, roleLevel={}",
                interview.getId(), candidateId, interview.getDomain(), interview.getRoleLevel());

        return interviewMapper.toResponse(interview);
    }

    // =========================================================================
    // Start
    // =========================================================================

    @Override
    @Transactional
    public InterviewStartResponse startInterview(UUID interviewId, UUID candidateId) {
        Interview interview = loadAndVerifyOwnership(interviewId, candidateId);

        if (interview.getState() != InterviewState.CREATED &&
            interview.getState() != InterviewState.CONFIGURED) {
            throw new InterviewAlreadyStartedException();
        }

        // Transition: CREATED → CONFIGURED → STARTED (collapse into single start flow)
        if (interview.getState() == InterviewState.CREATED) {
            applyTransition(interview, InterviewState.CONFIGURED, "CONFIGURE", "Auto-configured on start");
        }
        applyTransition(interview, InterviewState.STARTED, "START_INTERVIEW", "Candidate initiated interview");

        interview.setStartedAt(Instant.now());

        // Generate placeholder first question (Interview Agent integration in Module 6)
        Question firstQuestion = Question.builder()
                .interview(interview)
                .questionNumber(1)
                .questionText("Please introduce yourself and describe your most recent technical project.")
                .questionType(QuestionType.BEHAVIORAL)
                .difficulty(interview.getCurrentDifficulty())
                .build();

        interview.getQuestions().add(firstQuestion);
        interview.setCurrentQuestionNumber(1);

        applyTransition(interview, InterviewState.QUESTION_GENERATED, "QUESTION_GENERATED", "First question ready");
        applyTransition(interview, InterviewState.QUESTION_DELIVERED, "QUESTION_DELIVERED", "Question delivered to client");
        applyTransition(interview, InterviewState.WAITING_FOR_RESPONSE, "WAITING_FOR_RESPONSE", "Awaiting candidate response");

        interview = interviewRepository.save(interview);

        log.info("Interview started: id={}, candidateId={}", interviewId, candidateId);

        return InterviewStartResponse.builder()
                .interviewId(interview.getId())
                .state(interview.getState())
                .question(InterviewStartResponse.QuestionDetail.builder()
                        .id(firstQuestion.getId() != null ? firstQuestion.getId() : UUID.randomUUID())
                        .number(firstQuestion.getQuestionNumber())
                        .text(firstQuestion.getQuestionText())
                        .type(firstQuestion.getQuestionType())
                        .difficulty(firstQuestion.getDifficulty())
                        .totalQuestions(interview.getTotalQuestions())
                        .build())
                .build();
    }

    // =========================================================================
    // Get
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public InterviewResponse getInterview(UUID interviewId, UUID candidateId) {
        Interview interview = loadAndVerifyOwnership(interviewId, candidateId);
        return interviewMapper.toResponse(interview);
    }

    // =========================================================================
    // List
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<InterviewSummaryResponse> listInterviews(UUID candidateId, InterviewState state, Pageable pageable) {
        Page<Interview> page = (state != null)
                ? interviewRepository.findByCandidateIdAndStateOrderByCreatedAtDesc(candidateId, state, pageable)
                : interviewRepository.findByCandidateIdOrderByCreatedAtDesc(candidateId, pageable);

        return page.map(interviewMapper::toSummaryResponse);
    }

    // =========================================================================
    // End
    // =========================================================================

    @Override
    @Transactional
    public InterviewResponse endInterview(UUID interviewId, UUID candidateId) {
        Interview interview = loadAndVerifyOwnership(interviewId, candidateId);

        if (interview.getState().isTerminal()) {
            throw new com.interviewplatform.interview.exception.InvalidStateTransitionException(
                    "Interview is already in a terminal state: " + interview.getState());
        }
        if (interview.getState() == InterviewState.COMPLETED ||
            interview.getState() == InterviewState.REPORT_GENERATING) {
            throw new com.interviewplatform.interview.exception.InvalidStateTransitionException(
                    "Interview is already completed or generating report");
        }

        applyTransition(interview, InterviewState.COMPLETED, "END_INTERVIEW", "Manually ended by candidate");
        interview.setCompletedAt(Instant.now());
        interview = interviewRepository.save(interview);

        log.info("Interview ended: id={}, candidateId={}", interviewId, candidateId);
        return interviewMapper.toResponse(interview);
    }

    // =========================================================================
    // Submit Answer
    // =========================================================================

    @Override
    @Transactional
    public UUID submitAnswer(UUID interviewId, UUID candidateId, org.springframework.web.multipart.MultipartFile audio) {
        Interview interview = loadAndVerifyOwnership(interviewId, candidateId);

        if (interview.getState() != InterviewState.WAITING_FOR_RESPONSE) {
            throw new com.interviewplatform.interview.exception.InvalidStateTransitionException(
                    "Interview is not waiting for a response. Current state: " + interview.getState());
        }

        // Get the current question
        Question currentQuestion = interview.getQuestions().stream()
                .filter(q -> q.getQuestionNumber().equals(interview.getCurrentQuestionNumber()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Current question not found"));

        // Save audio to temp file (in production, use AWS S3)
        String audioPath = "/tmp/" + UUID.randomUUID() + ".webm";
        try {
            java.io.File dest = new java.io.File(audioPath);
            dest.getParentFile().mkdirs();
            audio.transferTo(dest);
        } catch (java.io.IOException e) {
            log.error("Failed to save audio file", e);
            throw new RuntimeException("Could not store audio file", e);
        }

        Answer answer = Answer.builder()
                .interview(interview)
                .question(currentQuestion)
                .audioFilePath(audioPath)
                .audioFormat("WEBM")
                .recordedAt(Instant.now())
                .transcriptStatus(TranscriptStatus.PENDING)
                .build();

        answerRepository.save(answer);
        
        applyTransition(interview, InterviewState.AUDIO_SUBMITTED, "AUDIO_UPLOADED", "Candidate submitted audio answer");
        interviewRepository.save(interview);
        log.info("Audio submitted for interviewId={}, answerId={}", interviewId, answer.getId());
        
        // Note: Orchestrator should be invoked here, but to avoid circular dependencies, 
        // the Controller will invoke Orchestrator.handleAudioSubmitted() directly.
        return answer.getId();
    }

    // =========================================================================
    // transitionState (called by Orchestrator)
    // =========================================================================

    @Override
    @Transactional
    public void transitionState(UUID interviewId, InterviewState newState, String event, String reason) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new InterviewNotFoundException(interviewId));

        applyTransition(interview, newState, event, reason);
        interviewRepository.save(interview);

        log.debug("State transition: interviewId={} → {}", interviewId, newState);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private Interview loadAndVerifyOwnership(UUID interviewId, UUID candidateId) {
        Interview interview = interviewRepository.findByIdWithCandidate(interviewId)
                .orElseThrow(() -> new InterviewNotFoundException(interviewId));

        if (!interview.isOwnedBy(candidateId)) {
            throw new InterviewNotFoundException(interviewId); // 404 to avoid info leak
        }
        return interview;
    }

    private void applyTransition(Interview interview, InterviewState newState, String event, String reason) {
        InterviewState previousState = interview.getState();
        InterviewStateMachine.assertValidTransition(previousState, newState);
        interview.setState(newState);
        appendStateHistory(interview, previousState, newState, event, reason);
    }

    private void appendStateHistory(Interview interview, InterviewState previous, InterviewState current,
                                    String event, String reason) {
        InterviewStateHistory history = InterviewStateHistory.builder()
                .interview(interview)
                .previousState(previous != null ? previous.name() : null)
                .currentState(current.name())
                .transitionEvent(event)
                .transitionedBy("SYSTEM")
                .transitionReason(reason)
                .build();
        interview.getStateHistory().add(history);
    }

    private InterviewTemplate resolveTemplate(CreateInterviewRequest request) {
        if (request.getTemplateId() != null) {
            return templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Template not found: " + request.getTemplateId()));
        }
        return null;
    }
}
