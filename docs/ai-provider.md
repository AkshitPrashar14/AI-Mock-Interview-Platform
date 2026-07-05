# AI Provider Module

> **Status:** Placeholder — to be completed during implementation.

## Overview

The AI module decouples all business logic from specific LLM vendors. A factory pattern selects the configured provider at startup.

## Package Structure

```
ai/
├── provider/    — LLM provider implementations
├── prompt/      — Prompt templates and builders
├── memory/      — Conversation memory strategies
├── model/       — Request/response model classes
└── factory/     — Dynamic provider factory
```

## Planned Providers

| Provider | Type | Notes |
|---|---|---|
| OpenAI GPT-4 | Cloud | Default v1 target |
| Google Gemini | Cloud | Alternative |
| Ollama | Local | Privacy-first option |

## Configuration

Provider selection via pplication.yml:

```yaml
ai:
  provider: openai
  api-key: \
```

_Detailed design TBD._
