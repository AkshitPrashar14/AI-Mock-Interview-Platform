package com.interviewplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for the AI Mock Interview Platform.
 *
 * <p>This is a Modular Monolith. Each top-level sub-package represents
 * an isolated domain module:
 * <ul>
 *   <li>{@code config}        — cross-cutting Spring configuration</li>
 *   <li>{@code security}      — security filters and configuration</li>
 *   <li>{@code auth}          — authentication module</li>
 *   <li>{@code user}          — user management module</li>
 *   <li>{@code interview}     — interview session module</li>
 *   <li>{@code orchestrator}  — interview flow orchestration</li>
 *   <li>{@code speech}        — speech-to-text abstraction layer</li>
 *   <li>{@code ai}            — AI provider abstraction layer</li>
 *   <li>{@code agents}        — specialized AI evaluator agents</li>
 *   <li>{@code report}        — report generation module</li>
 *   <li>{@code analytics}     — analytics module</li>
 *   <li>{@code dashboard}     — dashboard aggregation module</li>
 *   <li>{@code common}        — shared utilities, exceptions, helpers</li>
 * </ul>
 * </p>
 *
 * <p><b>NOTE:</b> No business logic is implemented in this scaffold.</p>
 */
@SpringBootApplication
public class InterviewPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewPlatformApplication.class, args);
    }
}
