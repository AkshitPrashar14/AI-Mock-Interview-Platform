package com.interviewplatform.orchestrator;

import com.interviewplatform.agents.aggregator.AggregatedEvaluation;
import com.interviewplatform.agents.common.AgentResult;
import com.interviewplatform.agents.common.InterviewContext;
import com.interviewplatform.agents.interview.InterviewAgent;
import com.interviewplatform.agents.interview.QuestionGenerationResult;
import com.interviewplatform.agents.orchestrator.AiOrchestrator;
import com.interviewplatform.agents.report.ReportCompilerAgent;
import com.interviewplatform.interview.entity.Answer;
import com.interviewplatform.interview.entity.DifficultyLevel;
import com.interviewplatform.interview.entity.Evaluation;
import com.interviewplatform.interview.entity.Interview;
import com.interviewplatform.interview.entity.InterviewState;
import com.interviewplatform.interview.entity.Question;
import com.interviewplatform.interview.repository.AnswerRepository;
import com.interviewplatform.interview.repository.EvaluationRepository;
import com.interviewplatform.interview.repository.InterviewRepository;
import com.interviewplatform.interview.repository.QuestionRepository;
import com.interviewplatform.interview.service.InterviewService;
import com.interviewplatform.report.entity.Report;
import com.interviewplatform.report.repository.ReportRepository;
import com.interviewplatform.speech.dto.TranscriptionResult;
import com.interviewplatform.speech.dto.TranscriptStatus;
import com.interviewplatform.speech.service.SpeechService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Central coordination hub that drives interview state transitions.
 *
 * <h3>Responsibilities</h3>
 * <ol>
 *   <li>Receive domain events from the WebSocket controller or future async listeners.</li>
 *   <li>Validate and drive state transitions via {@link InterviewService}.</li>
 *   <li>Dispatch work to downstream services (STT, AI agents) — currently stubs.</li>
 *   <li>Broadcast state updates to the candidate's WebSocket topic.</li>
 * </ol>
 *
 * <p>Downstream integrations (SpeechService, InterviewAgent, TechnicalAgent, etc.)
 * are injected as stubs in Module 3 and replaced with real implementations in
 * Modules 4–11.</p>
 *
 * <p><b>Module:</b> Module 3 — Interview Orchestrator</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewOrchestrator {

    private final InterviewService interviewService;
    private final DifficultyManager difficultyManager;
    private final SimpMessagingTemplate messagingTemplate;

    private final SpeechService speechService;
    private final AiOrchestrator aiOrchestrator;
    private final InterviewAgent interviewAgent;
    private final ReportCompilerAgent reportCompilerAgent;

    private final InterviewRepository interviewRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final EvaluationRepository evaluationRepository;
    private final ReportRepository reportRepository;

    // =========================================================================
    // Event: Audio submitted by candidate
    // =========================================================================

    /**
     * Handles the {@code AUDIO_SUBMITTED} domain event.
     *
     * <p>Transitions the interview to {@code TRANSCRIBING} state and dispatches
     * the audio bytes to the SpeechService (stubbed — Module 4 integration).</p>
     *
     * @param interviewId the active interview session ID
     * @param answerId    the ID of the answer record that holds the audio reference
     */
    public void handleAudioSubmitted(UUID interviewId, UUID answerId) {
        log.info("Orchestrator.handleAudioSubmitted: interviewId={}, answerId={}", interviewId, answerId);

        interviewService.transitionState(interviewId, InterviewState.TRANSCRIBING,
                "AUDIO_SUBMITTED", "Audio received, initiating transcription");

        broadcastState(interviewId, InterviewState.TRANSCRIBING, null);

        CompletableFuture.runAsync(() -> {
            try {
                Answer answer = answerRepository.findById(answerId).orElseThrow();
                byte[] audioBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(answer.getAudioFilePath()));
                
                // Invoke real SpeechService
                TranscriptionResult result = speechService.transcribe(answerId, audioBytes, "webm");
                
                if (result.isSuccess()) {
                    answer.setTranscript(result.getTranscript());
                    answer.setTranscriptStatus(TranscriptStatus.COMPLETED);
                    answerRepository.save(answer);
                    
                    handleTranscriptReady(interviewId, answerId, result.getTranscript());
                } else {
                    answer.setTranscriptStatus(TranscriptStatus.FAILED);
                    answerRepository.save(answer);
                    // Handle failure gracefully (e.g., transition to ERROR state)
                    interviewService.transitionState(interviewId, InterviewState.ERROR, "STT_FAILED", result.getErrorMessage());
                    broadcastState(interviewId, InterviewState.ERROR, Map.of("error", result.getErrorMessage()));
                }
            } catch (Exception ex) {
                log.error("Failed STT for answerId={}", answerId, ex);
            }
        });
    }

    // =========================================================================
    // Event: Transcript is ready
    // =========================================================================

    /**
     * Handles the {@code TRANSCRIPT_READY} domain event.
     *
     * <p>Transitions to {@code EVALUATING} and dispatches the transcript to
     * all three AI evaluation agents in parallel (stubbed — Modules 6–9).</p>
     *
     * @param interviewId the active interview session ID
     * @param answerId    the ID of the answer containing the transcript
     * @param transcript  the raw transcribed text
     */
    public void handleTranscriptReady(UUID interviewId, UUID answerId, String transcript) {
        log.info("Orchestrator.handleTranscriptReady: interviewId={}, answerId={}, transcriptLen={}",
                interviewId, answerId, transcript != null ? transcript.length() : 0);

        interviewService.transitionState(interviewId, InterviewState.EVALUATING,
                "TRANSCRIPT_READY", "Transcript received, dispatching to evaluation agents");

        broadcastState(interviewId, InterviewState.EVALUATING, null);

        CompletableFuture.runAsync(() -> {
            try {
                InterviewContext context = buildContext(interviewId, answerId);
                
                AggregatedEvaluation evalResult = aiOrchestrator.evaluate(context);
                
                // Save evaluation
                Evaluation eval = Evaluation.builder()
                        .answer(answerRepository.findById(answerId).orElseThrow())
                        .technicalScore(evalResult.getTechnicalScore())
                        .englishScore(evalResult.getEnglishScore())
                        .behavioralScore(evalResult.getBehavioralScore())
                        .compositeScore(evalResult.getCompositeScore())
                        .technicalSummary(evalResult.getTechnicalSummary())
                        .englishSummary(evalResult.getEnglishSummary())
                        .behavioralSummary(evalResult.getBehavioralSummary())
                        .build();
                        
                evaluationRepository.save(eval);

                Interview interview = context.getInterview();
                
                // Get all recent scores
                List<Integer> recentScores = evaluationRepository.findByAnswerQuestionInterviewOrderByCreatedAtAsc(interview)
                        .stream().map(Evaluation::getCompositeScore).toList();
                        
                int questionNumber = context.getCurrentQuestion().getQuestionNumber();

                handleEvaluationComplete(interviewId, evalResult.getCompositeScore(), 
                                         recentScores, context.getDifficulty(), 
                                         questionNumber, interview.getTotalQuestions());

            } catch (Exception ex) {
                log.error("Failed evaluation for answerId={}", answerId, ex);
            }
        });
    }

    // =========================================================================
    // Event: Evaluation complete
    // =========================================================================

    /**
     * Handles the {@code EVALUATION_COMPLETE} domain event.
     *
     * <p>Transitions to {@code AGGREGATING}, applies the score aggregation formula,
     * then decides whether to generate the next question or complete the interview.</p>
     *
     * @param interviewId      the active interview session ID
     * @param compositeScore   the aggregated composite score (0–100) for this answer
     * @param recentScores     ordered list of composite scores for the session so far
     * @param currentDifficulty the difficulty level of the just-answered question
     * @param questionNumber   the just-answered question number
     * @param totalQuestions   total questions in this session
     */
    public void handleEvaluationComplete(UUID interviewId, int compositeScore,
                                         List<Integer> recentScores, DifficultyLevel currentDifficulty,
                                         int questionNumber, int totalQuestions) {
        log.info("Orchestrator.handleEvaluationComplete: interviewId={}, score={}, q={}/{}",
                interviewId, compositeScore, questionNumber, totalQuestions);

        interviewService.transitionState(interviewId, InterviewState.AGGREGATING,
                "EVALUATION_COMPLETE", "Scores aggregated");

        boolean isLastQuestion = questionNumber >= totalQuestions;

        if (isLastQuestion) {
            // All questions answered → complete the interview
            interviewService.transitionState(interviewId, InterviewState.COMPLETED,
                    "ALL_QUESTIONS_ANSWERED", "All " + totalQuestions + " questions completed");

            broadcastState(interviewId, InterviewState.COMPLETED,
                    Map.of("compositeScore", compositeScore));

            // Trigger report generation
            handleInterviewComplete(interviewId);
        } else {
            // Adapt difficulty and request next question
            DifficultyLevel nextDifficulty = difficultyManager.nextDifficulty(currentDifficulty, recentScores);

            interviewService.transitionState(interviewId, InterviewState.GENERATING_NEXT_QUESTION,
                    "NEXT_QUESTION_REQUESTED", "Generating question " + (questionNumber + 1)
                            + " at difficulty " + nextDifficulty);

            broadcastState(interviewId, InterviewState.GENERATING_NEXT_QUESTION,
                    Map.of("nextDifficulty", nextDifficulty.name(), "questionNumber", questionNumber + 1));

            CompletableFuture.runAsync(() -> {
                try {
                    InterviewContext context = buildContext(interviewId, null);
                    context.setDifficulty(nextDifficulty); // override for next question
                    
                    AgentResult result = interviewAgent.execute(context);
                    
                    if (result.isSuccess() && result.getPayload() instanceof QuestionGenerationResult qResult) {
                        Question q = Question.builder()
                                .interview(context.getInterview())
                                .questionNumber(qResult.getQuestionNumber())
                                .questionText(qResult.getQuestionText())
                                .difficulty(DifficultyLevel.valueOf(qResult.getDifficulty()))
                                .askedAt(Instant.now())
                                .build();
                                
                        questionRepository.save(q);
                        
                        interviewService.transitionState(interviewId, InterviewState.WAITING_FOR_RESPONSE, 
                                "QUESTION_DELIVERED", "Next question delivered to candidate");
                        
                        broadcastState(interviewId, InterviewState.WAITING_FOR_RESPONSE, 
                                Map.of("question", q.getQuestionText()));
                    } else {
                        log.error("Failed to generate question: {}", result.getErrorMessage());
                    }
                } catch (Exception ex) {
                    log.error("Failed generating next question", ex);
                }
            });
        }
    }

    // =========================================================================
    // Event: Interview complete — start report generation
    // =========================================================================

    /**
     * Handles the {@code INTERVIEW_COMPLETE} domain event.
     *
     * <p>Transitions to {@code REPORT_GENERATING} and dispatches the report
     * compilation task (stubbed — Module 11).</p>
     *
     * @param interviewId the completed interview session ID
     */
    public void handleInterviewComplete(UUID interviewId) {
        log.info("Orchestrator.handleInterviewComplete: interviewId={}", interviewId);

        interviewService.transitionState(interviewId, InterviewState.REPORT_GENERATING,
                "REPORT_REQUESTED", "Triggering report compilation");

        broadcastState(interviewId, InterviewState.REPORT_GENERATING, null);

        CompletableFuture.runAsync(() -> {
            try {
                InterviewContext context = buildContext(interviewId, null);
                
                // Get all evaluations for the report aggregator
                List<Evaluation> allEvaluations = evaluationRepository.findByAnswerQuestionInterviewOrderByCreatedAtAsc(context.getInterview());
                context.setMetadata(Map.of("evaluations", allEvaluations));
                
                AgentResult result = reportCompilerAgent.execute(context);
                
                if (result.isSuccess() && result.getPayload() instanceof Report report) {
                    reportRepository.save(report);
                    
                    interviewService.transitionState(interviewId, InterviewState.COMPLETED, 
                            "REPORT_GENERATED", "Report available");
                            
                    broadcastState(interviewId, InterviewState.COMPLETED, Map.of("reportReady", true));
                } else {
                    log.error("Failed to generate report: {}", result.getErrorMessage());
                }
            } catch (Exception ex) {
                log.error("Failed generating report", ex);
            }
        });
    }

    // =========================================================================
    // Private helpers
    // =========================================================================
    
    private InterviewContext buildContext(UUID interviewId, UUID answerId) {
        Interview interview = interviewRepository.findById(interviewId).orElseThrow();
        Question currentQuestion = null;
        String transcript = null;
        
        if (answerId != null) {
            Answer answer = answerRepository.findById(answerId).orElseThrow();
            currentQuestion = answer.getQuestion();
            transcript = answer.getTranscript();
        } else {
            // Find latest question
            currentQuestion = questionRepository.findFirstByInterviewOrderByQuestionNumberDesc(interview).orElse(null);
        }
        
        List<Question> previousQuestions = questionRepository.findByInterviewOrderByQuestionNumberAsc(interview);
        
        return InterviewContext.builder()
                .interview(interview)
                .candidate(interview.getCandidate())
                .currentQuestion(currentQuestion)
                .previousQuestions(previousQuestions)
                .transcript(transcript)
                .difficulty(currentQuestion != null ? currentQuestion.getDifficulty() : null)
                .state(interview.getStatus())
                .metadata(Map.of("totalQuestions", interview.getTotalQuestions()))
                .build();
    }

    /**
     * Broadcasts the current interview state to the candidate's WebSocket topic.
     *
     * @param interviewId the interview session ID
     * @param state       the new state to broadcast
     * @param extra       optional extra payload fields (may be null)
     */
    private void broadcastState(UUID interviewId, InterviewState state, Map<String, Object> extra) {
        String destination = "/topic/interview/" + interviewId + "/state";

        InterviewStateEvent event = new InterviewStateEvent(interviewId, state, extra);
        messagingTemplate.convertAndSend(destination, event);

        log.debug("Orchestrator: broadcast → {} state={}", destination, state);
    }
}
