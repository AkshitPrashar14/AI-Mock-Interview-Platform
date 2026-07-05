# AI Agent Design

> **Status:** Placeholder — to be completed during implementation.

## Overview

The platform uses multiple specialized AI agents, each evaluating a distinct dimension of the candidate's response. Agents are orchestrated after each answer is transcribed.

## Agent Catalogue

| Agent | Package | Responsibility |
|---|---|---|
| Interview Conductor | gents.interview | Drives adaptive question flow |
| Technical Evaluator | gents.technical | Correctness, depth, problem-solving |
| English Proficiency | gents.english | Grammar, vocabulary, fluency |
| Behavioral Evaluator | gents.behavior | STAR method, soft skills |
| Report Generator | gents.report | Aggregates scores into final report |

## Agent Interface (Planned)

Each agent will implement a common interface to be defined in the gents package.

_Interface design TBD._

## Evaluation Flow

1. Candidate answers question (audio)
2. STT engine transcribes audio → text
3. All evaluator agents receive the transcript simultaneously
4. Each agent returns a structured score + feedback object
5. Report agent aggregates all outputs
6. Report is stored and surfaced to the candidate

_Detailed prompt engineering and memory strategy TBD._
