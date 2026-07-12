package com.interviewplatform.agents.orchestrator;

import com.interviewplatform.agents.aggregator.AggregatedEvaluation;
import com.interviewplatform.agents.aggregator.EvaluationAggregator;
import com.interviewplatform.agents.behavioral.BehavioralAgent;
import com.interviewplatform.agents.common.AgentResult;
import com.interviewplatform.agents.common.InterviewContext;
import com.interviewplatform.agents.english.EnglishAgent;
import com.interviewplatform.agents.technical.TechnicalAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Parallel AI Orchestrator — runs all three evaluation agents concurrently.
 *
 * <h3>Concurrency Model</h3>
 * <ul>
 *   <li>Uses Java 21 virtual threads for non-blocking I/O during LLM HTTP calls.</li>
 *   <li>{@link CompletableFuture#allOf} waits for all three agents.</li>
 *   <li>A 35-second global ceiling prevents indefinite blocking.</li>
 *   <li>If any agent times out or fails, a neutral score (50) is used for that dimension.</li>
 * </ul>
 *
 * <p><b>Module:</b> Module 10 — Parallel Evaluation Aggregator</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiOrchestrator {

    /** Global timeout for parallel agent execution. */
    private static final int GLOBAL_TIMEOUT_SECONDS = 35;

    private final TechnicalAgent technicalAgent;
    private final EnglishAgent englishAgent;
    private final BehavioralAgent behavioralAgent;
    private final EvaluationAggregator aggregator;

    /** Virtual thread executor — Java 21+. One virtual thread per task. */
    private final Executor virtualThreadExecutor =
            Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Runs TechnicalAgent, EnglishAgent, and BehavioralAgent in parallel
     * and aggregates their results.
     *
     * @param context common agent input with transcript and context
     * @return aggregated evaluation with composite score
     */
    public AggregatedEvaluation evaluate(InterviewContext context) {
        log.info("AiOrchestrator: parallel evaluation started for answer");
        long start = System.currentTimeMillis();

        CompletableFuture<AgentResult> techFuture = CompletableFuture.supplyAsync(
                () -> safeEvaluate("TechnicalAgent", () -> technicalAgent.execute(context)),
                virtualThreadExecutor);

        CompletableFuture<AgentResult> engFuture = CompletableFuture.supplyAsync(
                () -> safeEvaluate("EnglishAgent", () -> englishAgent.execute(context)),
                virtualThreadExecutor);

        CompletableFuture<AgentResult> behFuture = CompletableFuture.supplyAsync(
                () -> safeEvaluate("BehavioralAgent", () -> behavioralAgent.execute(context)),
                virtualThreadExecutor);

        try {
            CompletableFuture.allOf(techFuture, engFuture, behFuture)
                    .get(GLOBAL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            log.warn("AiOrchestrator: global timeout reached after {}s — using completed results",
                     GLOBAL_TIMEOUT_SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("AiOrchestrator: interrupted", ex);
        } catch (ExecutionException ex) {
            log.error("AiOrchestrator: unexpected execution error", ex);
        }

        AgentResult techResult = getOrNeutral(techFuture, "TechnicalAgent");
        AgentResult engResult  = getOrNeutral(engFuture,  "EnglishAgent");
        AgentResult behResult  = getOrNeutral(behFuture,  "BehavioralAgent");

        long elapsed = System.currentTimeMillis() - start;
        log.info("AiOrchestrator: evaluation complete in {}ms — tech={} eng={} beh={}",
                elapsed, techResult.getScore(), engResult.getScore(), behResult.getScore());

        return aggregator.aggregate(techResult, engResult, behResult);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private AgentResult safeEvaluate(String agentName, java.util.function.Supplier<AgentResult> supplier) {
        try {
            return supplier.get();
        } catch (Exception ex) {
            log.error("AiOrchestrator: {} threw exception: {}", agentName, ex.getMessage(), ex);
            return AgentResult.failure(agentName, ex.getMessage());
        }
    }

    private AgentResult getOrNeutral(CompletableFuture<AgentResult> future, String agentName) {
        try {
            return future.getNow(AgentResult.failure(agentName, "Did not complete within timeout"));
        } catch (Exception ex) {
            log.warn("AiOrchestrator: could not get result for {}: {}", agentName, ex.getMessage());
            return AgentResult.failure(agentName, ex.getMessage());
        }
    }
}
