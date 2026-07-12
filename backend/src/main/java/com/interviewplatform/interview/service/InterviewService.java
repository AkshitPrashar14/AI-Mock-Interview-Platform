package com.interviewplatform.interview.service;

import com.interviewplatform.interview.dto.request.CreateInterviewRequest;
import com.interviewplatform.interview.dto.response.InterviewResponse;
import com.interviewplatform.interview.dto.response.InterviewStartResponse;
import com.interviewplatform.interview.dto.response.InterviewSummaryResponse;
import com.interviewplatform.interview.entity.InterviewState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Business logic for interview session lifecycle management.
 *
 * <p>This service manages the pure data lifecycle of interviews (create, start, list, get, end).
 * It does NOT invoke AI agents or the STT service — those are delegated to the
 * {@link com.interviewplatform.orchestrator.InterviewOrchestrator}.</p>
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
public interface InterviewService {

    /**
     * Creates a new interview session for the given candidate.
     * The session is initialised in {@code CREATED} state.
     *
     * @param request     interview configuration parameters
     * @param candidateId authenticated candidate's user ID
     * @return the created interview response
     */
    InterviewResponse createInterview(CreateInterviewRequest request, UUID candidateId);

    /**
     * Starts an interview session, transitioning it to {@code STARTED} state.
     * In Module 2 this returns a placeholder first question;
     * in Module 6 the Interview Agent generates the real question.
     *
     * @param interviewId interview to start
     * @param candidateId authenticated candidate's user ID (ownership check)
     * @return start response with first question
     * @throws com.interviewplatform.interview.exception.InterviewNotFoundException       if not found or not owned
     * @throws com.interviewplatform.interview.exception.InterviewAlreadyStartedException if already started
     */
    InterviewStartResponse startInterview(UUID interviewId, UUID candidateId);

    /**
     * Retrieves the current state of an interview session.
     *
     * @param interviewId interview to retrieve
     * @param candidateId authenticated candidate's user ID (ownership check)
     * @return interview response
     */
    InterviewResponse getInterview(UUID interviewId, UUID candidateId);

    /**
     * Lists all interviews for the authenticated candidate, paginated.
     *
     * @param candidateId authenticated candidate's user ID
     * @param state       optional state filter (null means all states)
     * @param pageable    pagination parameters
     * @return paginated interview summaries
     */
    Page<InterviewSummaryResponse> listInterviews(UUID candidateId, InterviewState state, Pageable pageable);

    /**
     * Manually ends an interview, transitioning it to {@code COMPLETED} state.
     *
     * @param interviewId interview to end
     * @param candidateId authenticated candidate's user ID (ownership check)
     * @return updated interview response
     */
    InterviewResponse endInterview(UUID interviewId, UUID candidateId);

    /**
     * Submits an audio answer for the current question.
     *
     * @param interviewId interview to submit answer for
     * @param candidateId authenticated candidate's user ID
     * @param audio       the audio file
     * @return the UUID of the saved Answer
     */
    UUID submitAnswer(UUID interviewId, UUID candidateId, org.springframework.web.multipart.MultipartFile audio);

    /**
     * Performs a validated state machine transition and persists the audit record.
     *
     * <p>Called internally by this service and by the Orchestrator. Validates the
     * transition against {@link InterviewStateMachine} before applying it.</p>
     *
     * @param interviewId interview to transition
     * @param newState    target state
     * @param event       event name triggering the transition (logged in audit table)
     * @param reason      human-readable reason (logged in audit table)
     */
    void transitionState(UUID interviewId, InterviewState newState, String event, String reason);
}
