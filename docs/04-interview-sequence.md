# 04 — Interview Sequence Diagram

> **Version:** V1 (Audio First)
> **Status:** Approved — Design Phase

---

## 1. Purpose

This document captures the complete sequence of interactions between all system participants during an interview session. It covers: session initialization, question delivery, audio recording, transcription, parallel evaluation, and report generation.

---

## 2. Participants

| Participant | Abbreviation | Role |
|---|---|---|
| Candidate (Browser) | `CAND` | Records audio, receives questions |
| Next.js Frontend | `FE` | Manages UI state, audio streaming |
| Nginx | `NGINX` | API Gateway |
| Interview Controller | `CTRL` | HTTP entry point |
| Interview Orchestrator | `ORCH` | Central coordinator |
| Speech Module | `SPEECH` | Transcription + validation |
| Technical Agent | `TECH` | Technical evaluation |
| English Agent | `ENG` | Language evaluation |
| Behavioral Agent | `BEH` | Behavioral evaluation |
| Evaluation Aggregator | `AGG` | Score computation |
| Difficulty Manager | `DIFF` | Difficulty adjustment |
| Interview Agent | `INTV_AGENT` | Question generation |
| Report Compiler Agent | `RPT_AGENT` | Report narrative |
| Database | `DB` | Persistence |

---

## 3. Full Interview Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    actor CAND as Candidate
    participant FE as Next.js Frontend
    participant NGINX as Nginx Gateway
    participant CTRL as Interview Controller
    participant ORCH as Interview Orchestrator ★
    participant DB as PostgreSQL

    %% ═══════════════════════════════════════
    %% PHASE 1: Session Initialization
    %% ═══════════════════════════════════════
    Note over CAND, DB: ── PHASE 1: Session Initialization ──

    CAND->>FE: Click "Start Interview"
    FE->>NGINX: POST /api/v1/interviews {config}
    NGINX->>CTRL: Forward Request
    CTRL->>ORCH: initializeInterview(config)
    ORCH->>DB: INSERT interview (state=CREATED)
    DB-->>ORCH: interviewId
    ORCH->>DB: UPDATE state=CONFIGURED
    ORCH-->>CTRL: InterviewSession {id, config}
    CTRL-->>FE: 201 Created {interviewId}
    FE->>NGINX: POST /api/v1/interviews/{id}/start
    NGINX->>CTRL: Forward Request
    CTRL->>ORCH: startInterview(interviewId)
    ORCH->>DB: UPDATE state=STARTED
    ORCH-->>CTRL: First Question
    CTRL-->>FE: 200 OK {question, state=QUESTION_DELIVERED}
    FE-->>CAND: Display Question #1

    %% ═══════════════════════════════════════
    %% PHASE 2: Answer Recording
    %% ═══════════════════════════════════════
    Note over CAND, DB: ── PHASE 2: Answer Recording ──

    CAND->>FE: Press Record
    FE->>DB: [State = LISTENING]
    FE-->>CAND: Recording indicator shown
    CAND->>FE: Speaks answer (audio)
    CAND->>FE: Press Stop
    FE->>NGINX: POST /api/v1/interviews/{id}/answers {audioBlob}
    NGINX->>CTRL: Forward Request
    CTRL->>ORCH: processAnswer(interviewId, audioBlob)
    ORCH->>DB: UPDATE state=TRANSCRIBING
    ORCH->>DB: INSERT answer record (raw audio stored)
```

---

## 4. Speech Processing Sequence

```mermaid
sequenceDiagram
    autonumber
    participant ORCH as Interview Orchestrator ★
    participant SPEECH as Speech Module
    participant STT_PROV as STT Provider (Vosk/Whisper)
    participant TRANS_VAL as Transcript Validator
    participant DB as PostgreSQL

    %% ═══════════════════════════════════════
    %% PHASE 3: Transcription
    %% ═══════════════════════════════════════
    Note over ORCH, DB: ── PHASE 3: Speech Processing ──

    ORCH->>SPEECH: transcribeAudio(audioBlob)
    SPEECH->>SPEECH: Convert audio format (WebM → WAV if needed)
    SPEECH->>STT_PROV: transcribe(audioWav)
    STT_PROV->>STT_PROV: Run local STT inference
    STT_PROV-->>SPEECH: RawTranscript {text, confidence, duration}

    SPEECH->>TRANS_VAL: validate(rawTranscript)

    alt Transcript Invalid (too short / low confidence)
        TRANS_VAL-->>SPEECH: ValidationResult {INVALID, reason}
        SPEECH-->>ORCH: TranscriptionResult {INVALID}
        ORCH-->>ORCH: Request re-answer (increment retry count)
        Note over ORCH: If max retries exceeded → skip question
    else Transcript Valid
        TRANS_VAL-->>SPEECH: ValidationResult {VALID}
        SPEECH-->>ORCH: TranscriptionResult {VALID, text}
        ORCH->>DB: UPDATE answer.transcript = text
        ORCH->>DB: UPDATE state = EVALUATING
    end
```

---

## 5. Parallel Evaluation Sequence

```mermaid
sequenceDiagram
    autonumber
    participant ORCH as Interview Orchestrator ★
    participant TECH as Technical Agent
    participant ENG as English Agent
    participant BEH as Behavioral Agent
    participant AGG as Evaluation Aggregator
    participant DB as PostgreSQL

    %% ═══════════════════════════════════════
    %% PHASE 4: Parallel Evaluation
    %% ═══════════════════════════════════════
    Note over ORCH, DB: ── PHASE 4: Parallel AI Evaluation ──

    par Parallel Execution via CompletableFuture.allOf()
        ORCH->>TECH: evaluate(transcript, context)
        TECH->>TECH: Build technical prompt
        TECH->>TECH: Call LLM API
        TECH-->>ORCH: TechnicalResult {score, concepts, gaps, feedback}
    and
        ORCH->>ENG: evaluate(transcript, context)
        ENG->>ENG: Build language prompt
        ENG->>ENG: Call LLM API
        ENG-->>ORCH: EnglishResult {score, grammar, vocabulary, fillers, feedback}
    and
        ORCH->>BEH: evaluate(transcript, context)
        BEH->>BEH: Build behavioral prompt
        BEH->>BEH: Call LLM API
        BEH-->>ORCH: BehavioralResult {score, confidence, leadership, ownership, feedback}
    end

    Note over ORCH: All 3 agents completed — proceed to aggregation

    ORCH->>AGG: aggregate(technicalResult, englishResult, behavioralResult)
    AGG->>AGG: Apply weighted formula (configurable weights)
    AGG->>AGG: Compute composite score
    AGG-->>ORCH: AggregatedEvaluation {compositeScore, dimensionScores}

    ORCH->>DB: INSERT evaluation record (all scores persisted)
    ORCH->>DB: UPDATE interview context (running averages, turn count)
```

---

## 6. Next Question Generation Sequence

```mermaid
sequenceDiagram
    autonumber
    participant ORCH as Interview Orchestrator ★
    participant DIFF as Difficulty Manager
    participant INTV_AGENT as Interview Agent
    participant FE as Next.js Frontend
    participant CAND as Candidate
    participant DB as PostgreSQL

    %% ═══════════════════════════════════════
    %% PHASE 5: Next Question
    %% ═══════════════════════════════════════
    Note over ORCH, DB: ── PHASE 5: Difficulty Adjustment + Next Question ──

    ORCH->>DIFF: recommendDifficulty(aggregatedEvaluation, currentDifficulty)
    DIFF->>DIFF: Apply difficulty algorithm
    Note right of DIFF: Score >= threshold → increase difficulty<br/>Score < lower_threshold → decrease difficulty<br/>Otherwise → maintain
    DIFF-->>ORCH: DifficultyRecommendation {level}

    ORCH->>ORCH: Update InterviewContext {difficulty, history, turnCount}
    ORCH->>DB: UPDATE state = GENERATING_NEXT_QUESTION

    ORCH->>INTV_AGENT: generateNextQuestion(context, difficulty)
    INTV_AGENT->>INTV_AGENT: Build adaptive prompt with conversation history
    INTV_AGENT->>INTV_AGENT: Call LLM API
    INTV_AGENT-->>ORCH: Question {text, type, difficulty, expectedKeyPoints}

    ORCH->>DB: INSERT question record
    ORCH->>DB: UPDATE state = QUESTION_DELIVERED

    ORCH-->>FE: NextQuestionResponse {question, turnNumber, difficulty}
    FE-->>CAND: Display Next Question
    Note over CAND: Loop continues until maxQuestions reached
```

---

## 7. Report Generation Sequence

```mermaid
sequenceDiagram
    autonumber
    participant ORCH as Interview Orchestrator ★
    participant RPT_AGENT as Report Compiler Agent
    participant AGG as Evaluation Aggregator
    participant RPT_SVC as Report Service
    participant FE as Next.js Frontend
    participant CAND as Candidate
    participant DB as PostgreSQL

    %% ═══════════════════════════════════════
    %% PHASE 6: Report Generation
    %% ═══════════════════════════════════════
    Note over ORCH, DB: ── PHASE 6: Interview Completion + Report ──

    ORCH->>ORCH: Detect maxQuestions reached OR candidate ends session
    ORCH->>DB: UPDATE state = COMPLETED

    ORCH->>AGG: computeFinalScores(allEvaluations)
    AGG->>AGG: Aggregate all turn scores into final dimension scores
    AGG->>AGG: Compute final composite score (no LLM involvement)
    AGG-->>ORCH: FinalScores {technical, english, behavioral, composite}

    ORCH->>RPT_AGENT: compileReport(finalScores, interviewMetadata, evaluationHistory)
    Note right of RPT_AGENT: Report Compiler Agent ONLY generates narrative<br/>It does NOT compute scores

    RPT_AGENT->>RPT_AGENT: Build report compilation prompt
    RPT_AGENT->>RPT_AGENT: Call LLM API
    RPT_AGENT-->>ORCH: ReportNarrative {summary, strengths, improvements, recommendations}

    ORCH->>RPT_SVC: saveReport(finalScores, narrative, interviewId)
    RPT_SVC->>DB: INSERT report record
    ORCH->>DB: UPDATE state = REPORT_GENERATED

    ORCH-->>FE: ReportReadyEvent {reportId}
    FE-->>CAND: "Your interview report is ready!"
    CAND->>FE: View Report
    FE->>DB: GET /api/v1/reports/{interviewId}
    DB-->>FE: FullReport {scores, narrative, recommendations}
    FE-->>CAND: Display complete assessment report
```

---

## 8. Key Design Decisions

### 8.1 Stateful Session, Stateless HTTP

The interview session is stateful in the database. However, the HTTP layer remains stateless — each request is independently authenticated via JWT. The Interview Orchestrator reconstructs session context from the database on each request.

### 8.2 WebSocket for Real-time Updates

For latency-sensitive updates (question delivery, state transitions), a WebSocket channel pushes events from the backend to the frontend without polling.

### 8.3 Parallel Agent Execution

The three evaluation agents run concurrently using `CompletableFuture.allOf()`. The orchestrator does not proceed until all three complete, ensuring consistent aggregation.

### 8.4 Retry Mechanism for Invalid Transcripts

If the transcript is rejected, the orchestrator prompts the candidate to re-answer (up to a configured maximum number of retries). After the maximum, the turn is skipped and scored as zero.

### 8.5 Report Compiler Separation

The Report Compiler Agent is invoked only once, at the very end. It is deliberately isolated from the evaluation pipeline. It only sees final scores and metadata — it never evaluates individual answers.
