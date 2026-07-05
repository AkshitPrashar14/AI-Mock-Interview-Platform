# 14 — AI JSON Contracts

> **Version:** V1
> **Status:** Approved — New Document
> **Related:** [13-prompt-architecture.md](./13-prompt-architecture.md) · [16-report-schema.md](./16-report-schema.md)

---

## 1. Purpose

This document defines the exact JSON request and response schemas for every AI agent. These contracts are the binding interface between the AI Orchestration Layer and the LLM. The Schema Validator enforces every field and constraint defined here at runtime.

**Rules:**
- All agents must return valid JSON — never Markdown, prose, or mixed formats
- All numeric scores are integers in the range 0–100 (inclusive)
- All string fields are non-null; use `""` for empty, never `null`
- All array fields are present even when empty (`[]`, not `null`)
- `status` field is always present: `SUCCESS | PARTIAL | ERROR`

---

## 2. Common Structures

### 2.1 ScoreDetail (reused across agents)

```json
{
  "$schema": "common/score-detail",
  "type": "object",
  "required": ["score", "rationale"],
  "properties": {
    "score": {
      "type": "integer",
      "minimum": 0,
      "maximum": 100,
      "description": "Score for this sub-dimension"
    },
    "rationale": {
      "type": "string",
      "minLength": 10,
      "description": "One to two sentence explanation for this score"
    }
  }
}
```

### 2.2 ImprovementItem (reused in Technical and Report)

```json
{
  "$schema": "common/improvement-item",
  "type": "object",
  "required": ["area", "observation", "recommendation"],
  "properties": {
    "area": { "type": "string" },
    "observation": { "type": "string", "minLength": 20 },
    "recommendation": { "type": "string", "minLength": 20 }
  }
}
```

---

## 3. Interview Agent Contract

### 3.1 Input (passed by AI Orchestrator via Context Injector)

```json
{
  "$schema": "agents/interview-agent/input-v1",
  "description": "Context injected into the Interview Agent prompt",
  "required": [
    "domain", "roleLevel", "currentDifficulty",
    "questionNumber", "totalQuestions",
    "coveredTopics", "weakTopics", "strongTopics",
    "conversationHistory"
  ],
  "properties": {
    "domain": {
      "type": "string",
      "example": "Java Backend"
    },
    "roleLevel": {
      "type": "string",
      "enum": ["JUNIOR", "MID", "SENIOR", "LEAD", "PRINCIPAL"]
    },
    "currentDifficulty": {
      "type": "string",
      "enum": ["EASY", "MEDIUM", "HARD", "EXPERT"]
    },
    "questionNumber": {
      "type": "integer",
      "minimum": 1
    },
    "totalQuestions": {
      "type": "integer",
      "minimum": 1
    },
    "coveredTopics": {
      "type": "array",
      "items": { "type": "string" },
      "description": "Topics already asked about in this session"
    },
    "weakTopics": {
      "type": "array",
      "items": { "type": "string" },
      "description": "Topics where candidate scored below threshold"
    },
    "strongTopics": {
      "type": "array",
      "items": { "type": "string" },
      "description": "Topics where candidate performed well"
    },
    "conversationHistory": {
      "type": "array",
      "maxItems": 6,
      "items": {
        "type": "object",
        "required": ["questionText", "transcript", "compositeScore"],
        "properties": {
          "questionText": { "type": "string" },
          "transcript":   { "type": "string" },
          "compositeScore": { "type": "integer", "minimum": 0, "maximum": 100 }
        }
      }
    }
  }
}
```

### 3.2 Output (returned by LLM — validated by Schema Validator)

```json
{
  "$schema": "agents/interview-agent/output-v1",
  "required": [
    "questionText", "questionType", "difficultyLevel",
    "expectedKeyPoints", "followUpHints", "topicTag", "status"
  ],
  "properties": {
    "questionText": {
      "type": "string",
      "minLength": 20,
      "description": "The full interview question text"
    },
    "questionType": {
      "type": "string",
      "enum": ["TECHNICAL", "BEHAVIORAL", "SITUATIONAL", "FOLLOW_UP"]
    },
    "difficultyLevel": {
      "type": "string",
      "enum": ["EASY", "MEDIUM", "HARD", "EXPERT"]
    },
    "expectedKeyPoints": {
      "type": "array",
      "minItems": 1,
      "maxItems": 8,
      "items": { "type": "string" },
      "description": "Key concepts a strong answer should cover"
    },
    "followUpHints": {
      "type": "array",
      "items": { "type": "string" },
      "description": "Suggested follow-up probes if candidate's answer is shallow"
    },
    "topicTag": {
      "type": "string",
      "description": "Primary topic this question covers, e.g. 'concurrency'"
    },
    "status": {
      "type": "string",
      "enum": ["SUCCESS", "ERROR"],
      "description": "SUCCESS when question was generated; ERROR when agent could not comply"
    }
  }
}
```

**Example Output:**
```json
{
  "questionText": "Explain how Java's CompletableFuture differs from a regular Future, and describe a scenario where you would use it.",
  "questionType": "TECHNICAL",
  "difficultyLevel": "HARD",
  "expectedKeyPoints": [
    "Non-blocking nature of CompletableFuture",
    "Chaining via thenApply / thenCompose",
    "Exception handling with exceptionally()",
    "Comparison to blocking Future.get()"
  ],
  "followUpHints": [
    "Ask about allOf() vs anyOf()",
    "Ask about thread pool used by supplyAsync()"
  ],
  "topicTag": "concurrency",
  "status": "SUCCESS"
}
```

---

## 4. Technical Evaluation Agent Contract

### 4.1 Input

```json
{
  "$schema": "agents/technical-agent/input-v1",
  "required": ["domain", "questionText", "transcript", "expectedKeyPoints", "difficultyLevel"],
  "properties": {
    "domain": { "type": "string" },
    "questionText": { "type": "string" },
    "transcript": {
      "type": "string",
      "minLength": 1,
      "description": "Validated candidate answer transcript"
    },
    "expectedKeyPoints": {
      "type": "array",
      "items": { "type": "string" }
    },
    "difficultyLevel": {
      "type": "string",
      "enum": ["EASY", "MEDIUM", "HARD", "EXPERT"]
    }
  }
}
```

### 4.2 Output

```json
{
  "$schema": "agents/technical-agent/output-v1",
  "required": [
    "overallScore", "subscores", "feedback",
    "conceptsCovered", "conceptsMissed", "improvements", "status"
  ],
  "properties": {
    "overallScore": {
      "type": "integer",
      "minimum": 0,
      "maximum": 100
    },
    "subscores": {
      "type": "object",
      "required": ["correctness", "depth", "problemSolving", "completeness"],
      "properties": {
        "correctness":    { "$ref": "common/score-detail" },
        "depth":          { "$ref": "common/score-detail" },
        "problemSolving": { "$ref": "common/score-detail" },
        "completeness":   { "$ref": "common/score-detail" }
      }
    },
    "feedback": {
      "type": "string",
      "minLength": 30,
      "description": "Overall qualitative feedback paragraph"
    },
    "conceptsCovered": {
      "type": "array",
      "items": { "type": "string" },
      "description": "Key points the candidate correctly addressed"
    },
    "conceptsMissed": {
      "type": "array",
      "items": { "type": "string" },
      "description": "Expected key points that were absent from the answer"
    },
    "improvements": {
      "type": "array",
      "maxItems": 3,
      "items": { "$ref": "common/improvement-item" }
    },
    "status": {
      "type": "string",
      "enum": ["SUCCESS", "PARTIAL", "ERROR"]
    }
  }
}
```

**Example Output:**
```json
{
  "overallScore": 72,
  "subscores": {
    "correctness":    { "score": 80, "rationale": "Core concept of non-blocking was correct..." },
    "depth":          { "score": 65, "rationale": "Chaining was mentioned superficially..." },
    "problemSolving": { "score": 75, "rationale": "Scenario described was relevant..." },
    "completeness":   { "score": 60, "rationale": "Exception handling not discussed..." }
  },
  "feedback": "The candidate demonstrated a solid understanding of CompletableFuture's non-blocking nature but did not cover exception handling or thread pool configuration.",
  "conceptsCovered": ["non-blocking future", "thenApply chaining"],
  "conceptsMissed": ["exceptionally()", "allOf() vs anyOf()", "custom executor"],
  "improvements": [
    {
      "area": "Exception Handling",
      "observation": "Candidate did not mention exceptionally() or handle() methods.",
      "recommendation": "Practice error propagation patterns in CompletableFuture chains."
    }
  ],
  "status": "SUCCESS"
}
```

---

## 5. English Communication Agent Contract

### 5.1 Input

```json
{
  "$schema": "agents/english-agent/input-v1",
  "required": ["transcript"],
  "properties": {
    "transcript": {
      "type": "string",
      "minLength": 1
    },
    "questionContext": {
      "type": "string",
      "description": "Optional — the question asked (for relevance scoring)"
    }
  }
}
```

### 5.2 Output

```json
{
  "$schema": "agents/english-agent/output-v1",
  "required": ["overallScore", "subscores", "feedback", "fillerWords", "status"],
  "properties": {
    "overallScore": {
      "type": "integer",
      "minimum": 0,
      "maximum": 100
    },
    "subscores": {
      "type": "object",
      "required": ["grammar", "vocabulary", "fluency", "professional", "fillerPenalty"],
      "properties": {
        "grammar":      { "$ref": "common/score-detail" },
        "vocabulary":   { "$ref": "common/score-detail" },
        "fluency":      { "$ref": "common/score-detail" },
        "professional": { "$ref": "common/score-detail" },
        "fillerPenalty":{ "$ref": "common/score-detail" }
      }
    },
    "feedback": {
      "type": "string",
      "minLength": 20
    },
    "fillerWords": {
      "type": "object",
      "description": "Map of filler word → count detected in transcript",
      "additionalProperties": { "type": "integer" },
      "example": { "um": 4, "uh": 2, "like": 7 }
    },
    "grammarErrors": {
      "type": "array",
      "items": { "type": "string" },
      "description": "Specific grammar errors detected (max 5)"
    },
    "status": {
      "type": "string",
      "enum": ["SUCCESS", "PARTIAL", "ERROR"]
    }
  }
}
```

---

## 6. Behavioral Evaluation Agent Contract

### 6.1 Input

```json
{
  "$schema": "agents/behavioral-agent/input-v1",
  "required": ["questionText", "transcript", "questionType"],
  "properties": {
    "questionText": { "type": "string" },
    "transcript":   { "type": "string", "minLength": 1 },
    "questionType": {
      "type": "string",
      "enum": ["BEHAVIORAL", "SITUATIONAL", "TECHNICAL", "FOLLOW_UP"]
    }
  }
}
```

### 6.2 Output

```json
{
  "$schema": "agents/behavioral-agent/output-v1",
  "required": ["overallScore", "subscores", "feedback", "starAnalysis", "status"],
  "properties": {
    "overallScore": {
      "type": "integer",
      "minimum": 0,
      "maximum": 100
    },
    "subscores": {
      "type": "object",
      "required": ["confidence", "leadership", "ownership", "decisionMaking", "professionalism"],
      "properties": {
        "confidence":      { "$ref": "common/score-detail" },
        "leadership":      { "$ref": "common/score-detail" },
        "ownership":       { "$ref": "common/score-detail" },
        "decisionMaking":  { "$ref": "common/score-detail" },
        "professionalism": { "$ref": "common/score-detail" }
      }
    },
    "feedback": {
      "type": "string",
      "minLength": 20
    },
    "starAnalysis": {
      "type": "object",
      "description": "STAR method evaluation — only meaningful for behavioral questions",
      "properties": {
        "applicable": {
          "type": "boolean",
          "description": "Was STAR framework relevant for this question?"
        },
        "situationPresent": { "type": "boolean" },
        "taskPresent":      { "type": "boolean" },
        "actionPresent":    { "type": "boolean" },
        "resultPresent":    { "type": "boolean" },
        "starScore": {
          "type": "integer",
          "minimum": 0,
          "maximum": 100,
          "description": "Completeness of STAR framework usage; 0 if not applicable"
        }
      }
    },
    "status": {
      "type": "string",
      "enum": ["SUCCESS", "PARTIAL", "ERROR"]
    }
  }
}
```

---

## 7. Report Compiler Agent Contract

> The Report Compiler Agent receives **only pre-computed scores and metadata**. It never evaluates answers. It never computes scores. Its sole job is to produce a structured narrative.

### 7.1 Input

```json
{
  "$schema": "agents/report-compiler/input-v1",
  "required": [
    "interviewMetadata", "finalScores", "perTurnSummaries"
  ],
  "properties": {
    "interviewMetadata": {
      "type": "object",
      "required": ["domain", "roleLevel", "totalQuestions", "durationMinutes", "completedAt"],
      "properties": {
        "domain":          { "type": "string" },
        "roleLevel":       { "type": "string" },
        "totalQuestions":  { "type": "integer" },
        "durationMinutes": { "type": "integer" },
        "completedAt":     { "type": "string", "format": "date-time" }
      }
    },
    "finalScores": {
      "type": "object",
      "required": ["technical", "english", "behavioral", "composite", "tier"],
      "properties": {
        "technical":  { "type": "number", "minimum": 0, "maximum": 100 },
        "english":    { "type": "number", "minimum": 0, "maximum": 100 },
        "behavioral": { "type": "number", "minimum": 0, "maximum": 100 },
        "composite":  { "type": "number", "minimum": 0, "maximum": 100 },
        "tier": {
          "type": "string",
          "enum": ["NEEDS_WORK", "DEVELOPING", "PROFICIENT", "EXCELLENT"]
        }
      }
    },
    "perTurnSummaries": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["questionNumber", "questionText", "topicTag", "compositeScore", "technicalFeedback", "englishFeedback", "behavioralFeedback"],
        "properties": {
          "questionNumber":    { "type": "integer" },
          "questionText":      { "type": "string" },
          "topicTag":          { "type": "string" },
          "compositeScore":    { "type": "integer" },
          "technicalFeedback": { "type": "string" },
          "englishFeedback":   { "type": "string" },
          "behavioralFeedback":{ "type": "string" }
        }
      }
    }
  }
}
```

### 7.2 Output

The Report Compiler Agent output is the final report narrative JSON. See the complete schema in [16-report-schema.md](./16-report-schema.md).

---

## 8. Validation Rules Summary

| Rule | Applied To | Enforcement |
|---|---|---|
| All scores 0–100 integer | All agents | Schema Validator |
| No null fields | All agents | Schema Validator |
| `status` always present | All agents | Schema Validator |
| `minLength` on all text fields | All agents | Schema Validator |
| Arrays never null (empty allowed) | All agents | Schema Validator |
| `questionType` is a known enum | Interview Agent | Schema Validator |
| `starAnalysis.applicable` drives STAR scoring | Behavioral Agent | Schema Validator |
| Report Compiler returns no score fields | Report Compiler | Schema Validator |
