# Speech Module

> **Status:** Placeholder — to be completed during implementation.

## Overview

The Speech module provides a provider-agnostic abstraction layer for Speech-to-Text (STT) functionality. Version 1 uses a **local STT engine** for privacy and zero-latency dependency on external APIs.

## Package Structure

```
speech/
├── provider/    — STT provider implementations
├── service/     — Audio processing and transcription service
├── model/       — Transcription result and audio segment models
└── config/      — Provider selection and initialization config
```

## Planned Providers

| Provider | Type | Notes |
|---|---|---|
| Vosk | Local | Open-source, offline, good accuracy |
| OpenAI Whisper | Local / API | High accuracy, can run locally |
| Google STT | Cloud | Future option |
| Azure STT | Cloud | Future option |

## Abstraction Contract (Planned)

The SpeechToTextProvider interface will be the single contract that all provider implementations satisfy. The service layer will only depend on this interface.

_Interface design TBD._

## Supported Audio Formats (v1)

- WAV (primary)
- WebM (from browser MediaRecorder API)

_Conversion pipeline TBD._
