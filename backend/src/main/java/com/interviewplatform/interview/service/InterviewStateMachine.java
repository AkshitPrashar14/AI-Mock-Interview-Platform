package com.interviewplatform.interview.service;

import com.interviewplatform.interview.entity.InterviewState;
import com.interviewplatform.interview.exception.InvalidStateTransitionException;

import java.util.Set;
import java.util.Map;

/**
 * Validates interview state machine transitions.
 *
 * <p>Defines the complete set of valid transitions per the
 * <a href="../../../../../../../docs/07-interview-state-machine.md">state machine design doc</a>.
 * Any transition not in this map is rejected with {@link InvalidStateTransitionException}.</p>
 */
public final class InterviewStateMachine {

    private InterviewStateMachine() {}

    /** Map of valid transitions: from-state → allowed to-states */
    private static final Map<InterviewState, Set<InterviewState>> VALID_TRANSITIONS = Map.ofEntries(
        Map.entry(InterviewState.CREATED, Set.of(
            InterviewState.CONFIGURED, InterviewState.ABANDONED)),

        Map.entry(InterviewState.CONFIGURED, Set.of(
            InterviewState.STARTED, InterviewState.ABANDONED)),

        Map.entry(InterviewState.STARTED, Set.of(
            InterviewState.QUESTION_GENERATED)),

        Map.entry(InterviewState.QUESTION_GENERATED, Set.of(
            InterviewState.QUESTION_DELIVERED)),

        Map.entry(InterviewState.QUESTION_DELIVERED, Set.of(
            InterviewState.WAITING_FOR_RESPONSE)),

        Map.entry(InterviewState.WAITING_FOR_RESPONSE, Set.of(
            InterviewState.LISTENING, InterviewState.ABANDONED)),

        Map.entry(InterviewState.LISTENING, Set.of(
            InterviewState.TRANSCRIBING, InterviewState.WAITING_FOR_RESPONSE)),

        Map.entry(InterviewState.TRANSCRIBING, Set.of(
            InterviewState.EVALUATING, InterviewState.WAITING_FOR_RESPONSE,
            InterviewState.GENERATING_NEXT_QUESTION, InterviewState.COMPLETED,
            InterviewState.ERROR)),

        Map.entry(InterviewState.EVALUATING, Set.of(
            InterviewState.AGGREGATING)),

        Map.entry(InterviewState.AGGREGATING, Set.of(
            InterviewState.GENERATING_NEXT_QUESTION, InterviewState.COMPLETED)),

        Map.entry(InterviewState.GENERATING_NEXT_QUESTION, Set.of(
            InterviewState.QUESTION_GENERATED, InterviewState.COMPLETED)),

        Map.entry(InterviewState.COMPLETED, Set.of(
            InterviewState.REPORT_GENERATING)),

        Map.entry(InterviewState.REPORT_GENERATING, Set.of(
            InterviewState.REPORT_GENERATED, InterviewState.ERROR)),

        Map.entry(InterviewState.ERROR, Set.of(
            InterviewState.ABANDONED)),

        // Terminal states — no outgoing transitions
        Map.entry(InterviewState.REPORT_GENERATED, Set.of()),
        Map.entry(InterviewState.ABANDONED, Set.of())
    );

    /**
     * Validates and asserts that the requested transition is legal.
     *
     * @param current   current interview state
     * @param requested desired new state
     * @throws InvalidStateTransitionException if the transition is not permitted
     */
    public static void assertValidTransition(InterviewState current, InterviewState requested) {
        Set<InterviewState> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(requested)) {
            throw new InvalidStateTransitionException(current, requested);
        }
    }

    /**
     * Returns true if the given transition is valid without throwing.
     */
    public static boolean isValidTransition(InterviewState current, InterviewState requested) {
        return VALID_TRANSITIONS.getOrDefault(current, Set.of()).contains(requested);
    }
}
