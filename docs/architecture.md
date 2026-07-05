# Architecture Overview

> **Project:** AI Mock Interview & Assessment Platform
> **Version:** V1 — Audio First
> **Architecture Style:** Modular Monolith
> **Last Updated:** Technology Stack Frozen — TDR created (doc 17)
> **Status:** Design Complete — Technology Stack Frozen for V1

---

## Quick Reference

This file is the entry point for all architecture documentation. Each major concern is fully documented in its own dedicated file.

| # | Document | Contents |
|---|---|---|
| 01 | [System Design](./01-system-design.md) | Problem statement, system context, technology stack, quality attributes |
| 02 | [High-Level Architecture](./02-high-level-architecture.md) | Layer breakdown, revised stack diagram, Python STT separation, AI Orchestration Layer, Context Engine |
| 03 | [Component Diagram](./03-component-diagram.md) | Backend + frontend module internals, component responsibilities |
| 04 | [Interview Sequence](./04-interview-sequence.md) | End-to-end interaction sequences — init, audio, evaluation, report |
| 05 | [AI Agent Architecture](./05-ai-agent-architecture.md) | All 5 agents, AI Orchestration Layer, resilience strategy, LLM abstraction |
| 06 | [Parallel Evaluation](./06-parallel-evaluation.md) | Concurrency model, timeout strategy, fallback, aggregation formula |
| 07 | [Interview State Machine](./07-interview-state-machine.md) | All 16 states, transition table, session recovery, timeout policies |
| 08 | [Database Design](./08-database-design.md) | Full ER diagram, all 9 tables, JSONB schemas, index strategy |
| 09 | [API Design](./09-api-design.md) | All REST endpoints, WebSocket channels, error codes, rate limits |
| 10 | [Security Design](./10-security-design.md) | JWT auth, authorization model, OWASP controls, data privacy |
| 11 | [Deployment Architecture](./11-deployment-architecture.md) | Docker Compose (5 services incl. Python STT), CI/CD pipeline, full observability, K8s roadmap |
| 12 | [Future Roadmap](./12-future-roadmap.md) | V2–V5 capabilities, microservices migration plan, technology evolution |
| 13 | [Prompt Architecture](./13-prompt-architecture.md) | **NEW** — Prompt lifecycle, versioning, context injection, schema enforcement, best practices |
| 14 | [AI JSON Contracts](./14-ai-json-contracts.md) | **NEW** — Full JSON request/response schemas for all 5 agents with validation rules |
| 15 | [Interview Context](./15-interview-context.md) | **NEW** — InterviewContext object, field descriptions, lifecycle, memory strategy, difficulty progression |
| 16 | [Report Schema](./16-report-schema.md) | Complete report JSON schema, verdict mapping, example report, storage strategy |
| 17 | [Technology Decisions](./17-technology-decisions.md) | **TDR** — Full technology stack, freeze status, compatibility matrix, integration map |

---

## Architecture in One Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                   Candidate Browser (Next.js)                     │
│         Audio Recording · WebSocket · REST · Report Viewer        │
└─────────────────────────────┬────────────────────────────────────┘
                              │ HTTPS / WSS
                    ┌─────────▼─────────┐
                    │  Nginx (TLS · RL) │
                    └─────────┬─────────┘
                              │
┌─────────────────────────────▼────────────────────────────────────┐
│                    Spring Boot 3 — Modular Monolith               │
│                                                                   │
│   Auth │ User │ Interview │ Report │ Dashboard │ Analytics        │
│                                                                   │
│   ┌─────────────────────────────────────────────────────────┐    │
│   │            Interview Orchestrator ★ (Central Hub)        │    │
│   │         State Machine · Pipeline · Coordination          │    │
│   └──────┬──────────────────────────────────────────────────┘    │
│          │                                                        │
│   ┌──────▼──────────────────────────────────────────────────┐    │
│   │              Interview Context Engine                    │    │
│   │  History · Difficulty · Topics · Evaluations · State    │    │
│   └──────┬──────────────────────────────────────────────────┘    │
│          │                                                        │
│   ┌──────▼──────────────────────────────────────────────────┐    │
│   │              AI Orchestration Layer                      │    │
│   │  Prompt Manager · Builder · Context Injector             │    │
│   │  Response Parser · Schema Validator                      │    │
│   └──────┬──────────────────────────────────────────────────┘    │
│          │                                                        │
│   ┌──────▼──────────────────────────────────────────────────┐    │
│   │              Parallel AI Evaluation                      │    │
│   │   Technical Agent ║ English Agent ║ Behavioral Agent     │    │
│   │              ↓ CompletableFuture.allOf() ↓               │    │
│   │              Evaluation Aggregator (Java only)            │    │
│   └──────┬──────────────────────────────────────────────────┘    │
│          │                                                        │
│   ┌──────▼──────────────────────────────────────────────────┐    │
│   │   Difficulty Manager → Interview Agent → Next Question   │    │
│   └─────────────────────────────────────────────────────────┘    │
│                                                                   │
│   Speech Interface ──► Python FastAPI :8001 ──► Faster-Whisper   │
│                                                                   │
│                        PostgreSQL 15                              │
└──────────────────────────────────────────────────────────────────┘
```

---

## Core Design Decisions (Summary)

| Decision | Choice | Rationale |
|---|---|---|
| Architecture | Modular Monolith | Team velocity now; microservice extraction later |
| Audio Only (V1) | No video | Reduce complexity; focus on core value |
| STT Runtime | Python FastAPI + Faster-Whisper (separate container) | ML inference is Python-native; decouples from JVM |
| STT Abstraction | `SpeechServiceClient` interface in Spring Boot | Swappable provider without touching orchestrator |
| AI Orchestration | Dedicated AI Orchestration Layer | Encapsulates all prompt engineering and agent dispatching |
| Interview Context | Independent Context Engine | Single source of truth for all session intelligence |
| Prompt Management | Versioned templates in classpath JSON | Prompt changes without code changes; A/B testing ready |
| LLM | Abstracted via factory | Swap providers without code changes |
| Scoring | Backend Aggregator (no LLM) | Deterministic, auditable, reproducible scores |
| Concurrency | Java 21 Virtual Threads | Efficient parallel agent execution |
| Coordination | Interview Orchestrator | Zero direct module-to-module coupling |
| AI Resilience | Resilience4j (circuit breaker + retry + timeout) | Graceful degradation when LLM or STT fails |
| Report Narrative | Report Compiler Agent (LLM) | Structured JSON only — no Markdown |
| Database | PostgreSQL + Flyway | ACID, relational model, migration-controlled |
| Security | Spring Security + RS256 JWT | Industry standard; refresh token rotation |
| Observability | Structured JSON logs + Prometheus + OpenTelemetry | Full metrics, tracing, and log correlation |

---

## Technology Stack Status

> The technology stack is **frozen for V1**. Full decision rationale, compatibility matrix, and freeze register are documented in [17 — Technology Decisions](./17-technology-decisions.md).

| Frozen | Deferred |
|---|---|
| Next.js, React, TypeScript, TailwindCSS, shadcn/ui | Faster-Whisper model size |
| Java 21, Spring Boot 3, Maven | TTS provider (post-Browser Web Speech API abstraction) |
| PostgreSQL, Spring Data JPA, Hibernate, Flyway | Prometheus + Grafana (V2) |
| Spring Security, JWT, BCrypt | |
| REST + WebSockets | |
| AIProvider interface + Google Gemini (default) | |
| Faster-Whisper + Python FastAPI | |
| Docker + Docker Compose | |
