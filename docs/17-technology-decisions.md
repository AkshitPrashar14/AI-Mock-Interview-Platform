# 17 — Technology Decision Record (TDR)

> **Project:** AI Mock Interview & Assessment Platform
> **Version:** V1 — Audio First
> **Status:** FROZEN — Technology Stack Finalized
> **Date:** 2026-07-02

---

## 1. Purpose

This document is the authoritative Technology Decision Record (TDR) for the AI Mock Interview & Assessment Platform. It records every selected technology, the rationale behind each selection, alternatives considered, and the strategy for future evolution. No technology decisions affecting V1 may be made without updating this document.

---

## 2. Technology Freeze Summary

| Category | Technology | Status |
|---|---|---|
| Frontend Framework | Next.js 14 | ✅ Frozen |
| Frontend Library | React 18 | ✅ Frozen |
| Frontend Language | TypeScript 5 | ✅ Frozen |
| Frontend Styling | TailwindCSS | ✅ Frozen |
| Frontend UI Components | shadcn/ui | ✅ Frozen |
| Backend Runtime | Java 21 | ✅ Frozen |
| Backend Framework | Spring Boot 3 | ✅ Frozen |
| Build Tool | Maven | ✅ Frozen |
| Database | PostgreSQL | ✅ Frozen |
| ORM | Spring Data JPA + Hibernate | ✅ Frozen |
| DB Migrations | Flyway | ✅ Frozen |
| Auth Framework | Spring Security | ✅ Frozen |
| Auth Tokens | JWT (Access + Refresh) | ✅ Frozen |
| Password Hashing | BCrypt | ✅ Frozen |
| API Protocol | REST + WebSockets | ✅ Frozen |
| AI Abstraction | AIProvider interface | ✅ Frozen |
| AI Default Provider | Google Gemini | ✅ Frozen |
| STT Engine | Faster-Whisper | ✅ Frozen |
| STT Model Size | Not fixed | ⏳ Deferred |
| Speech Service Runtime | Python FastAPI | ✅ Frozen |
| TTS Provider | Not fixed | ⏳ Deferred |
| TTS V1 Default | Browser Web Speech API | ✅ Frozen |
| Monitoring V1 | Spring Boot Actuator | ✅ Frozen |
| Monitoring Future | Prometheus + Grafana | ⏳ Deferred |
| Containerization | Docker + Docker Compose | ✅ Frozen |

---

## 3. Technology Freeze Register

### 3.1 Frozen Technologies

#### Frontend Framework
**Status:** ✅ Frozen
**Reason:** Selected for V1.

#### Faster-Whisper Model Size
**Status:** ⏳ Deferred
**Reason:** Model size depends on hardware benchmarking. Options: `tiny`, `base`, `small`, `medium`, `large-v3`. Final selection made after evaluating the development and production machine hardware.

#### TTS Provider
**Status:** ⏳ Deferred
**Reason:** Will begin with Browser Web Speech API abstraction. Concrete provider selected post-V1 based on quality and cost evaluation.

#### Monitoring Stack (Prometheus + Grafana)
**Status:** ⏳ Deferred
**Reason:** Spring Boot Actuator covers V1 observability needs. Full Prometheus + Grafana stack deferred to V2.

---

## 4. Frontend

### 4.1 Next.js 14

| Field | Detail |
|---|---|
| **Purpose** | Full-stack React framework for the candidate-facing UI |
| **Responsibilities** | Page routing, server-side rendering, API proxying, static asset serving |
| **Advantages** | App Router, file-based routing, SSR/SSG/ISR, built-in image optimization, streaming |
| **Alternatives Considered** | Vite + React SPA, Remix, Angular |
| **Why Selected** | App Router provides the best balance of server rendering and client interactivity; strong ecosystem; production-proven |
| **Future Replacement** | No replacement planned; upgrade to Next.js 15+ when stable |
| **Integration** | Communicates with Spring Boot via REST and WebSocket (SockJS + STOMP) |

### 4.2 React 18

| Field | Detail |
|---|---|
| **Purpose** | UI component library underpinning Next.js |
| **Responsibilities** | Component rendering, state management, hooks, concurrent rendering |
| **Advantages** | Concurrent mode, Server Components, Suspense, massive ecosystem |
| **Alternatives Considered** | Vue.js, Svelte, SolidJS |
| **Why Selected** | Industry standard; required by Next.js; team familiarity; largest component ecosystem |
| **Future Replacement** | No replacement planned |
| **Integration** | Renders shadcn/ui components; consumes Zustand state; calls REST endpoints via React Query |

### 4.3 TypeScript 5

| Field | Detail |
|---|---|
| **Purpose** | Statically typed superset of JavaScript |
| **Responsibilities** | Compile-time type checking, IDE autocompletion, API contract enforcement |
| **Advantages** | Catches bugs at compile time, self-documenting code, essential for large codebases |
| **Alternatives Considered** | Plain JavaScript, Flow |
| **Why Selected** | Industry standard for production Next.js applications; required by shadcn/ui |
| **Future Replacement** | No replacement planned |
| **Integration** | Applied across all frontend code; types shared for API request/response contracts |

### 4.4 TailwindCSS

| Field | Detail |
|---|---|
| **Purpose** | Utility-first CSS framework for rapid, consistent styling |
| **Responsibilities** | All component styling, responsive layouts, dark mode, design tokens |
| **Advantages** | No CSS naming conflicts, tree-shaken output, rapid iteration, consistent design system |
| **Alternatives Considered** | CSS Modules, styled-components, Vanilla CSS, MUI |
| **Why Selected** | Required by shadcn/ui; fastest for building consistent UIs without design overhead |
| **Future Replacement** | No replacement planned |
| **Integration** | Applied in all Next.js components; extended via `tailwind.config.ts` for brand tokens |

### 4.5 shadcn/ui

| Field | Detail |
|---|---|
| **Purpose** | Accessible, unstyled UI component library built on Radix UI primitives |
| **Responsibilities** | Buttons, dialogs, forms, tabs, cards, and all reusable UI primitives |
| **Advantages** | Components are copied into the codebase (full ownership), Radix accessibility, Tailwind-native |
| **Alternatives Considered** | MUI, Ant Design, Chakra UI, Headless UI |
| **Why Selected** | Full code ownership (no runtime dependency), accessible by default, best Tailwind integration |
| **Future Replacement** | No replacement needed; components owned by the project |
| **Integration** | Used across all frontend pages; styled via Tailwind; no backend dependency |

---

## 5. Backend

### 5.1 Java 21

| Field | Detail |
|---|---|
| **Purpose** | Primary runtime for the Spring Boot backend |
| **Responsibilities** | Business logic execution, concurrent agent evaluation via Virtual Threads, JVM memory management |
| **Advantages** | Virtual Threads (Project Loom) for high-concurrency without reactive complexity; LTS release; strong tooling |
| **Alternatives Considered** | Java 17, Kotlin, Go, Node.js |
| **Why Selected** | Virtual Threads are a core requirement for the parallel AI agent execution model; Java 21 is an LTS release |
| **Future Replacement** | Upgrade to Java 25 (next LTS) when available and stable |
| **Integration** | Hosts Spring Boot; Virtual Thread executor runs parallel CompletableFuture agent tasks |

### 5.2 Spring Boot 3

| Field | Detail |
|---|---|
| **Purpose** | Application framework for the backend modular monolith |
| **Responsibilities** | Dependency injection, REST API hosting, WebSocket support, security, data access, actuator |
| **Advantages** | Mature ecosystem, auto-configuration, production-ready observability, native GraalVM path |
| **Alternatives Considered** | Quarkus, Micronaut, Vert.x |
| **Why Selected** | Most complete Spring ecosystem (Security, Data JPA, WebSocket, Actuator); team familiarity; largest support community |
| **Future Replacement** | Upgrade to Spring Boot 4 when released; no framework replacement planned |
| **Integration** | Central hub connecting PostgreSQL, Flyway, Spring Security, JWT, WebSocket, and the Python STT service |

### 5.3 Maven

| Field | Detail |
|---|---|
| **Purpose** | Build automation and dependency management for the Java backend |
| **Responsibilities** | Dependency resolution, compilation, packaging, test execution, Docker image build trigger |
| **Advantages** | Standard in Spring Boot ecosystem; robust dependency conflict resolution; stable plugin ecosystem |
| **Alternatives Considered** | Gradle |
| **Why Selected** | Default for Spring Initializr; XML is verbose but predictable; no build scripting complexity |
| **Future Replacement** | Migration to Gradle possible in V3 if build complexity grows |
| **Integration** | Used in Dockerfile multi-stage build (`mvn package -DskipTests`); GitHub Actions CI |

---

## 6. Database

### 6.1 PostgreSQL

| Field | Detail |
|---|---|
| **Purpose** | Primary relational database for all persistent application data |
| **Responsibilities** | Stores users, interviews, questions, evaluations, reports, sessions; JSONB for flexible AI response storage |
| **Advantages** | ACID compliance, JSONB support, full-text search, row-level security, strong ORM support |
| **Alternatives Considered** | MySQL, MongoDB, H2 (dev only) |
| **Why Selected** | Best-in-class relational database with JSONB flexibility needed for AI evaluation storage; proven at scale |
| **Future Replacement** | Migrate to Amazon RDS PostgreSQL (managed) in V3; add read replica in V2 |
| **Integration** | Connected via Spring Data JPA + Hibernate; schema managed by Flyway |

### 6.2 Spring Data JPA + Hibernate

| Field | Detail |
|---|---|
| **Purpose** | ORM layer for database access |
| **Responsibilities** | Entity mapping, CRUD repositories, JPQL queries, lazy loading, transaction management |
| **Advantages** | Eliminates boilerplate SQL; deep Spring integration; second-level cache support |
| **Alternatives Considered** | JOOQ, MyBatis, plain JDBC |
| **Why Selected** | Fastest development velocity; native Spring Boot integration; sufficient for V1 query complexity |
| **Future Replacement** | Add JOOQ for complex analytical queries if needed in V3 |
| **Integration** | All modules access the database through JPA repositories; transaction boundaries enforced via `@Transactional` |

### 6.3 Flyway

| Field | Detail |
|---|---|
| **Purpose** | Database schema migration management |
| **Responsibilities** | Versioned SQL migrations, schema evolution, rollback tracking, environment consistency |
| **Advantages** | Migrations are version-controlled SQL; atomic application; no schema drift across environments |
| **Alternatives Considered** | Liquibase |
| **Why Selected** | Simpler than Liquibase for pure SQL migrations; native Spring Boot auto-run on startup |
| **Future Replacement** | No replacement planned |
| **Integration** | Runs automatically on Spring Boot startup; migrations in `src/main/resources/db/migration/` |

---

## 7. Authentication & Security

### 7.1 Spring Security

| Field | Detail |
|---|---|
| **Purpose** | Authentication and authorization framework |
| **Responsibilities** | Filter chain, JWT validation, role-based access control, CORS, CSRF protection |
| **Advantages** | Deep Spring integration, highly configurable, OWASP-aligned |
| **Alternatives Considered** | Custom filter chain, Apache Shiro |
| **Why Selected** | De facto standard for Spring Boot security; integrates natively with JWT, BCrypt, and method security |
| **Future Replacement** | Add OAuth2/OIDC in V3 for enterprise SSO |
| **Integration** | Applied at the API gateway layer; validates JWT on every protected request |

### 7.2 JWT (Access + Refresh Tokens)

| Field | Detail |
|---|---|
| **Purpose** | Stateless authentication tokens |
| **Responsibilities** | Access token (short-lived, 15 min), Refresh token (long-lived, 7 days, rotated) |
| **Advantages** | Stateless; no server-side session storage; scalable horizontally |
| **Alternatives Considered** | Session cookies, OAuth2 opaque tokens |
| **Why Selected** | Stateless scalability; standard for REST APIs; pairs with Spring Security cleanly |
| **Future Replacement** | Supplement with OAuth2/OIDC in V3 |
| **Integration** | Issued by Auth module; validated by Spring Security filter; stored in HTTP-only cookies on frontend |

### 7.3 BCrypt Password Encoder

| Field | Detail |
|---|---|
| **Purpose** | Secure password hashing |
| **Responsibilities** | Hash passwords at registration; verify at login |
| **Advantages** | Adaptive cost factor, built-in salting, resistant to rainbow table and brute force attacks |
| **Alternatives Considered** | Argon2, PBKDF2 |
| **Why Selected** | Built into Spring Security; industry standard; sufficient for V1 threat model |
| **Future Replacement** | Migrate to Argon2 in V3 if security requirements escalate |
| **Integration** | Used exclusively in Auth module; passwords never stored in plaintext |

---

## 8. Communication

### 8.1 REST APIs

| Field | Detail |
|---|---|
| **Purpose** | Primary client-server communication protocol |
| **Responsibilities** | All CRUD operations, interview lifecycle management, report retrieval, auth endpoints |
| **Advantages** | Stateless, cacheable, universally understood, simple to test |
| **Alternatives Considered** | GraphQL, gRPC |
| **Why Selected** | Simplest fit for request/response patterns; GraphQL overhead not justified for V1 |
| **Future Replacement** | Add GraphQL for recruiter portal in V3 |
| **Integration** | Next.js calls Spring Boot REST endpoints; documented via SpringDoc OpenAPI (Swagger) |

### 8.2 WebSockets

| Field | Detail |
|---|---|
| **Purpose** | Real-time bidirectional communication |
| **Responsibilities** | Interview state push notifications, real-time question delivery, live transcription feedback |
| **Advantages** | Low latency, server-initiated pushes, persistent connection |
| **Alternatives Considered** | Server-Sent Events, long polling |
| **Why Selected** | Required for real-time interview state machine updates; bidirectional for future live coaching |
| **Future Replacement** | No replacement planned; scale via sticky sessions or Redis pub/sub in V3 |
| **Integration** | Spring Boot STOMP WebSocket broker; Next.js SockJS + STOMP client |

---

## 9. AI Provider Abstraction

### 9.1 AIProvider Interface

| Field | Detail |
|---|---|
| **Purpose** | Decouple business logic from any specific LLM vendor |
| **Responsibilities** | Define a single contract (`generateResponse(prompt, context) → AIResponse`) implemented by all providers |
| **Advantages** | Zero code change to swap providers; supports A/B testing across providers; no vendor lock-in |
| **Alternatives Considered** | Direct OpenAI SDK, LangChain4j (considered but abstraction is internal) |
| **Why Selected** | Business logic must never depend on a specific provider — a core architecture constraint |
| **Future Replacement** | Interface is permanent; implementations are swappable |
| **Integration** | All 5 AI agents call `AIProvider`; provider resolved via factory based on configuration |

### 9.2 Google Gemini (Default V1 Provider)

| Field | Detail |
|---|---|
| **Purpose** | Default LLM for all AI agent calls in V1 |
| **Responsibilities** | Question generation, technical evaluation, English evaluation, behavioral evaluation, report narrative |
| **Advantages** | Generous free tier, long context window, strong reasoning, multimodal readiness for V2 |
| **Alternatives Considered** | OpenAI GPT-4o, Anthropic Claude, local Ollama |
| **Why Selected** | Best cost/performance for V1; long context supports full interview history injection |
| **Future Replacement** | Swappable via `AIProvider` factory; OpenRouter, OpenAI, Anthropic, and local LLM are planned implementations |
| **Integration** | Implemented as `GeminiAIProvider implements AIProvider`; API key in environment variable |

### 9.3 Future AI Providers (Planned)

| Provider | Status | Notes |
|---|---|---|
| OpenRouter | Planned V2 | Multi-model routing, cost optimization |
| OpenAI | Planned V2 | GPT-4o fallback or primary |
| Anthropic | Planned V2 | Claude for behavioral evaluation |
| Local LLM | Planned V3 | Privacy-first enterprise deployment |

---

## 10. Speech-to-Text

### 10.1 Faster-Whisper

| Field | Detail |
|---|---|
| **Purpose** | Offline speech-to-text transcription engine |
| **Responsibilities** | Convert candidate audio (WAV/WebM) to text transcripts with confidence scores |
| **Advantages** | 4x faster than original Whisper via CTranslate2; CPU + GPU support; fully offline; no API cost |
| **Alternatives Considered** | OpenAI Whisper (original), Vosk, Google Cloud STT, AWS Transcribe |
| **Why Selected** | Best performance/accuracy ratio for local inference; privacy-first (audio never leaves server) |
| **Future Replacement** | Model size upgradeable without code change; cloud STT optional in V4 |
| **Integration** | Runs inside Python FastAPI service; called by Spring Boot via HTTP REST |

### 10.2 Faster-Whisper Model Size

| Model | Parameters | Speed | Accuracy | Use Case |
|---|---|---|---|---|
| `tiny` | 39M | Fastest | Lowest | Low-spec hardware |
| `base` | 74M | Fast | Low-Medium | Development |
| `small` | 244M | Medium | Medium | Balanced dev |
| `medium` | 769M | Moderate | Good | Staging |
| `large-v3` | 1.5B | Slowest | Best | Production |

> **Status: ⏳ Deferred** — Final model selected after hardware benchmarking on the target machine.

---

## 11. Speech Service

### 11.1 Python FastAPI

| Field | Detail |
|---|---|
| **Purpose** | Independent microservice wrapping the Faster-Whisper STT engine |
| **Responsibilities** | Accept audio bytes, run Faster-Whisper inference, return transcript JSON |
| **Advantages** | Python is native for ML inference; FastAPI is async, high-performance, auto-generates OpenAPI docs |
| **Alternatives Considered** | Flask, Django REST Framework, gRPC server |
| **Why Selected** | Fastest Python web framework; async-native; auto-validation via Pydantic; cleanest STT wrapper |
| **Future Replacement** | No replacement planned; scale horizontally in V3 via Kubernetes worker pool |
| **Integration** | Runs as Docker container `stt-service:8001` on internal network; Spring Boot calls `http://stt-service:8001/transcribe` via `SpeechServiceClient` interface |

---

## 12. Text-to-Speech Abstraction

> **Status: ⏳ Deferred** — Provider not frozen. V1 uses Browser Web Speech API.

### 12.1 TTS Abstraction Interface

| Field | Detail |
|---|---|
| **Purpose** | Decouple business logic from any TTS vendor, mirroring the AIProvider pattern |
| **Responsibilities** | Define a contract for converting text to spoken audio |
| **V1 Default** | Browser Web Speech API (zero server cost, no infrastructure) |
| **Future Providers** | Piper (local, open-source), Kokoro (high quality, local), ElevenLabs (commercial), Azure TTS (enterprise) |

### 12.2 TTS Provider Comparison

| Provider | Cost | Quality | Latency | Privacy | Status |
|---|---|---|---|---|---|
| Browser Web Speech API | Free | Medium | Zero (client) | High | ✅ V1 Default |
| Piper | Free (self-hosted) | Good | Low | High | Planned V2 |
| Kokoro | Free (self-hosted) | Very Good | Low | High | Planned V2 |
| ElevenLabs | Paid | Excellent | Medium | Medium | Planned V3 |
| Azure TTS | Paid | Excellent | Low | Medium | Planned V3 |

---

## 13. Monitoring & Observability

### 13.1 Spring Boot Actuator (V1)

| Field | Detail |
|---|---|
| **Purpose** | Built-in health and metrics exposure for V1 |
| **Responsibilities** | `/actuator/health`, `/actuator/prometheus`, liveness/readiness probes |
| **Status** | ✅ Frozen for V1 |
| **Future** | Prometheus + Grafana stack added in V2 |

### 13.2 Prometheus + Grafana (Future)

| Field | Detail |
|---|---|
| **Purpose** | Full observability stack for metrics visualization and alerting |
| **Status** | ⏳ Deferred to V2 |
| **Integration** | Scrapes `/actuator/prometheus`; Grafana dashboards for interview metrics, AI agent latency, STT performance |

---

## 14. Deployment

### 14.1 Docker

| Field | Detail |
|---|---|
| **Purpose** | Containerization of all services |
| **Responsibilities** | Package backend, frontend, STT service, and database into isolated containers |
| **Advantages** | Environment parity, reproducible builds, isolation, portability |
| **Alternatives Considered** | Podman, bare metal |
| **Why Selected** | Industry standard; required by Docker Compose; CI/CD pipeline compatibility |
| **Future Replacement** | Containers remain; orchestration moves to Kubernetes in V3 |
| **Integration** | Multi-stage Dockerfiles for backend (Java), frontend (Node), STT service (Python) |

### 14.2 Docker Compose

| Field | Detail |
|---|---|
| **Purpose** | Multi-container orchestration for local development and V1 production |
| **Responsibilities** | Define and run 6 services: nginx, frontend, backend, stt-service, postgres, redis |
| **Advantages** | Simple single-file configuration, internal networking, volume management |
| **Alternatives Considered** | Kubernetes (too complex for V1), Nomad |
| **Why Selected** | Right-sized for V1 single-server deployment; zero operational overhead |
| **Future Replacement** | Migrate to Kubernetes in V3 using Strangler Fig pattern |
| **Integration** | `docker-compose.yml` at repo root; all services on `interview-net` bridge network |

---

## 15. Technology Compatibility Matrix

| Technology | Version | Compatible With | Notes |
|---|---|---|---|
| **Spring Boot** | 3.3.x | Java 21, Hibernate 6.x, Flyway 9+, Spring Security 6.x | Requires Java 17+ minimum |
| **Java** | 21 (LTS) | Spring Boot 3.x, Maven 3.9+, eclipse-temurin Docker image | Virtual Threads GA |
| **PostgreSQL** | 15–16 | Hibernate 6.x, Flyway 9+, Spring Data JPA | JSONB fully supported |
| **Hibernate** | 6.x (via Spring Boot 3) | PostgreSQL 14+, Java 21, Spring Data JPA | Jakarta EE 10 namespace |
| **Flyway** | 9.x | PostgreSQL 14+, Spring Boot 3.x | Auto-run via Spring Boot |
| **FastAPI** | 0.111+ | Python 3.11, Faster-Whisper 1.x, Pydantic v2 | Async-native |
| **Faster-Whisper** | 1.x | Python 3.11, CUDA 11.8+ (optional), CPU | CTranslate2 backend |
| **Google Gemini** | API v1beta | Spring Boot (via HTTP client), AIProvider interface | REST API; no Java SDK locked |
| **Docker** | 24+ | All services (Java 21, Node 20, Python 3.11, PostgreSQL 15) | Compose v2 syntax |
| **Next.js** | 14.x | Node.js 20+, React 18, TypeScript 5, TailwindCSS 3.x | App Router |
| **TailwindCSS** | 3.x | Next.js 14, shadcn/ui, PostCSS | v4 migration planned post-V1 |
| **shadcn/ui** | Latest | React 18, TailwindCSS 3.x, Radix UI | Components copied into codebase |

---

## 16. Integration Map

```
Next.js (Frontend)
  ├── REST calls → Spring Boot :8080
  ├── WebSocket (SockJS+STOMP) → Spring Boot :8080
  └── Renders shadcn/ui components (TailwindCSS)

Spring Boot (Backend)
  ├── Spring Data JPA → PostgreSQL :5432
  ├── Flyway → PostgreSQL :5432 (migrations on startup)
  ├── Spring Security → JWT filter (every request)
  ├── AIProvider factory → Google Gemini API (HTTPS)
  ├── SpeechServiceClient → Python FastAPI :8001 (HTTP internal)
  └── Spring Actuator → exposes /actuator/health + /actuator/prometheus

Python FastAPI (STT Service)
  ├── Receives audio POST from Spring Boot
  ├── Runs Faster-Whisper inference
  └── Returns JSON transcript

Nginx (Reverse Proxy)
  ├── /* → Next.js :3000
  └── /api/* → Spring Boot :8080

Docker Compose
  └── Runs all 6 services on interview-net bridge network
```

---

## 17. Decision Authority

All changes to the frozen technology stack must:

1. Be documented in this file with a dated entry
2. Include the reason for change
3. Include impact on the compatibility matrix
4. Be approved before implementation begins

---

*Document Owner: Engineering Team*
*Next Review: V2 Planning Phase*
