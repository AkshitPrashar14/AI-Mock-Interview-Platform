# Backend Structure

> **Status:** Scaffold only — no implementation.

## Overview

The backend is a Spring Boot 3 Modular Monolith built with Java 21 and Maven.

## Module Map

| Module | Package | Responsibility |
|---|---|---|
| Application Entry | com.interviewplatform | Spring Boot bootstrap |
| Config | config | Cross-cutting Spring @Configuration |
| Security | security | Spring Security + JWT |
| Auth | uth | Authentication flows |
| Common | common | Shared utilities and base classes |
| Exception | exception | Global error handling |
| User | user | User management |
| Interview | interview | Interview session lifecycle |
| Orchestrator | orchestrator | Interview flow state machine |
| Speech | speech | STT abstraction layer |
| AI | i | LLM abstraction layer |
| Agents | gents | Specialized evaluator agents |
| Report | eport | Report generation and storage |
| Analytics | nalytics | Performance analytics |
| Dashboard | dashboard | Aggregated dashboard views |

## Layer Convention (per module)

```
module/
├── controller/    — REST API controllers (@RestController)
├── service/       — Business logic interfaces + implementations
├── repository/    — Spring Data JPA repositories
├── entity/        — JPA entities
├── dto/           — Request/Response DTOs
├── mapper/        — MapStruct mappers
└── validation/    — Custom constraint validators
```
