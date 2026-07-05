<div align="center">

# 🎙️ AI Mock Interview & Assessment Platform

**Production-grade, audio-first AI interview platform**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14-000000?style=for-the-badge&logo=next.js&logoColor=white)](https://nextjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)

</div>

---

## 📋 Table of Contents

- [Project Overview](#-project-overview)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Folder Structure](#-folder-structure)
- [Module Descriptions](#-module-descriptions)
- [Current Version Scope (v1)](#-current-version-scope-v1)
- [Future Scope](#-future-scope)
- [How to Run](#-how-to-run)
- [Development Workflow](#-development-workflow)
- [Documentation](#-documentation)

---

## 🎯 Project Overview

The **AI Mock Interview & Assessment Platform** is a production-quality, audio-first platform that conducts adaptive AI-driven mock interviews, evaluates candidate responses using a suite of specialized AI agents, and generates comprehensive performance reports.

### Key Features (v1)

| Feature | Status |
|---|---|
| Audio-based interview sessions | 🚧 In progress |
| Local Speech-to-Text (provider-agnostic) | 🚧 In progress |
| Adaptive interview flow (AI Orchestrator) | 🚧 In progress |
| Multi-agent evaluation (Technical, English, Behavioral) | 🚧 In progress |
| Automated interview report generation | 🚧 In progress |
| Candidate dashboard and analytics | 🚧 In progress |
| JWT Authentication | 🚧 In progress |
| Docker Compose deployment | 🚧 In progress |

> **v1 is AUDIO ONLY. Video analysis is intentionally out of scope.**

---

## 🏛️ Architecture

This project follows a **Modular Monolith** architecture.

- Each feature lives in its own isolated module with well-defined interfaces.
- Business logic is never coupled across module boundaries.
- The architecture is designed so any module can be extracted into a microservice with minimal refactoring.

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (Next.js 14)                    │
│         React · TypeScript · TailwindCSS · shadcn/ui         │
└────────────────────────┬────────────────────────────────────┘
                         │  REST API / WebSocket
┌────────────────────────▼────────────────────────────────────┐
│                   Backend (Spring Boot 3)                     │
│                                                               │
│  ┌─────────┐ ┌─────────┐ ┌───────────────────────────────┐  │
│  │  Auth   │ │  User   │ │     Orchestrator (State FSM)   │  │
│  └─────────┘ └─────────┘ └───────────────┬───────────────┘  │
│                                           │                   │
│  ┌────────────────────────────────────────▼────────────────┐ │
│  │               Interview Module                           │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                                               │
│  ┌──────────────────┐    ┌─────────────────────────────────┐ │
│  │  Speech Module   │    │       AI Agent Layer            │ │
│  │  (STT Abstraction│    │  Technical · English · Behavior  │ │
│  │   — Local First) │    │  Interview Conductor · Reporter  │ │
│  └──────────────────┘    └─────────────────────────────────┘ │
│                                                               │
│  ┌──────────────────┐    ┌─────────────────────────────────┐ │
│  │   AI Module      │    │  Report · Analytics · Dashboard  │ │
│  │ (LLM Abstraction)│    │  Modules                         │ │
│  └──────────────────┘    └─────────────────────────────────┘ │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              PostgreSQL · Spring Data JPA                │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

See [`docs/architecture.md`](docs/architecture.md) for a detailed breakdown.

---

## 🛠️ Tech Stack

### Backend

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language (LTS) |
| Spring Boot | 3.3.x | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.x | Database access layer |
| PostgreSQL | 16 | Primary datastore |
| Maven | 3.9.x | Build tool |
| MapStruct | 1.5.x | DTO ↔ Entity mapping |
| Lombok | 1.18.x | Boilerplate reduction |
| SpringDoc OpenAPI | 2.x | API documentation |

### Frontend

| Technology | Version | Purpose |
|---|---|---|
| Next.js | 14.x | React framework (App Router) |
| React | 18.x | UI library |
| TypeScript | 5.x | Type safety |
| TailwindCSS | 3.x | Utility-first CSS |
| shadcn/ui | latest | Component library |

### Infrastructure

| Technology | Purpose |
|---|---|
| Docker | Containerization |
| Docker Compose | Local orchestration |
| GitHub Actions | CI/CD (planned) |

---

## 📁 Folder Structure

```
ai-mock-interview-platform/
│
├── backend/                          # Spring Boot Modular Monolith
│   └── src/main/java/com/interviewplatform/
│       ├── InterviewPlatformApplication.java
│       ├── config/                   # Cross-cutting Spring @Configuration
│       ├── security/                 # Spring Security & JWT
│       ├── auth/                     # Authentication module
│       ├── common/                   # Shared utilities, base classes
│       ├── exception/                # Global error handling
│       ├── user/                     # User management module
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── entity/
│       │   ├── dto/
│       │   ├── mapper/
│       │   └── validation/
│       ├── interview/                # Interview session module
│       │   └── (same layer structure)
│       ├── orchestrator/             # Interview flow state machine
│       ├── speech/                   # STT abstraction layer
│       │   ├── provider/
│       │   ├── service/
│       │   ├── model/
│       │   └── config/
│       ├── ai/                       # LLM abstraction layer
│       │   ├── provider/
│       │   ├── prompt/
│       │   ├── memory/
│       │   ├── model/
│       │   └── factory/
│       ├── agents/                   # AI evaluator agents
│       │   ├── interview/
│       │   ├── technical/
│       │   ├── english/
│       │   ├── behavior/
│       │   └── report/
│       ├── report/                   # Report generation module
│       ├── analytics/                # Analytics module
│       └── dashboard/                # Dashboard aggregation
│
├── frontend/                         # Next.js 14 Application
│   ├── app/                          # Next.js App Router
│   │   ├── login/
│   │   ├── signup/
│   │   ├── dashboard/
│   │   ├── interview/
│   │   ├── report/
│   │   └── settings/
│   ├── components/
│   │   ├── layout/                   # Navbar, Sidebar, Footer
│   │   ├── dashboard/
│   │   ├── interview/                # Audio recorder, timer, Q&A UI
│   │   ├── report/                   # Score cards, feedback
│   │   └── ui/                       # shadcn/ui re-exports
│   ├── hooks/
│   ├── lib/
│   ├── services/                     # API client layer
│   ├── store/                        # Global state (Zustand)
│   ├── types/
│   ├── styles/
│   ├── public/
│   └── utils/
│
├── docs/                             # Project documentation
│   ├── architecture.md
│   ├── api-spec.md
│   ├── database-design.md
│   ├── agent-design.md
│   ├── speech-module.md
│   ├── ai-provider.md
│   ├── deployment.md
│   ├── backend-structure.md
│   ├── frontend-structure.md
│   ├── coding-guidelines.md
│   └── future-roadmap.md
│
├── docker/
│   ├── Dockerfile.backend
│   └── Dockerfile.frontend
│
├── scripts/
│   └── setup.sh
│
├── .github/
│   └── workflows/                    # GitHub Actions (planned)
│
├── .gitignore
├── docker-compose.yml
└── README.md
```

---

## 🧩 Module Descriptions

| Module | Package | Responsibility |
|---|---|---|
| **Config** | `config` | Cross-cutting Spring `@Configuration` beans: CORS, Jackson, OpenAPI |
| **Security** | `security` | Spring Security filter chain, JWT token utilities |
| **Auth** | `auth` | Login, token refresh, credential management |
| **Common** | `common` | Shared base classes, constants, utilities |
| **Exception** | `exception` | `@ControllerAdvice`, custom exceptions, error response DTOs |
| **User** | `user` | User profiles, roles, preferences |
| **Interview** | `interview` | Interview session lifecycle, question/answer recording |
| **Orchestrator** | `orchestrator` | Interview state machine, agent coordination, session flow |
| **Speech** | `speech` | Provider-agnostic STT abstraction layer — local-first |
| **AI** | `ai` | Provider-agnostic LLM abstraction layer with factory |
| **Agents** | `agents` | Five specialized AI evaluators (see below) |
| **Report** | `report` | Report generation, storage, retrieval, export |
| **Analytics** | `analytics` | Performance trends and platform metrics |
| **Dashboard** | `dashboard` | Aggregated dashboard data for candidates |

### AI Agent Catalogue

| Agent | Package | Evaluates |
|---|---|---|
| Interview Conductor | `agents.interview` | Adaptive question generation and flow control |
| Technical Evaluator | `agents.technical` | Correctness, depth, problem-solving ability |
| English Proficiency | `agents.english` | Grammar, vocabulary, fluency |
| Behavioral Evaluator | `agents.behavior` | Communication, STAR method, soft skills |
| Report Generator | `agents.report` | Aggregates all agent outputs into final report |

---

## 🎯 Current Version Scope (v1)

| In Scope | Out of Scope |
|---|---|
| ✅ Audio-only interviews | ❌ Video analysis |
| ✅ Local STT engine (abstracted) | ❌ Real-time streaming STT |
| ✅ JWT authentication | ❌ OAuth / SSO |
| ✅ Multi-agent evaluation | ❌ Fine-tuned models |
| ✅ Report generation | ❌ PDF export (v2) |
| ✅ Candidate dashboard | ❌ Admin panel |
| ✅ Docker Compose deployment | ❌ Kubernetes |
| ✅ PostgreSQL | ❌ Redis / caching |

---

## 🔮 Future Scope

- **v2**: Video analysis, streaming STT, multi-language support, admin panel, email notifications
- **v3**: Microservices extraction, Kubernetes, ATS integrations, mobile app, enterprise white-labelling

See [`docs/future-roadmap.md`](docs/future-roadmap.md) for the full roadmap.

---

## 🚀 How to Run

### Prerequisites

| Tool | Version |
|---|---|
| Java | 21+ |
| Maven | 3.9+ |
| Node.js | 20+ |
| Docker | 24+ |
| Docker Compose | v2+ |

### Option A — Docker Compose (Recommended)

```bash
# Clone the repository
git clone <repo-url>
cd ai-mock-interview-platform

# Copy environment template (when created)
cp .env.example .env

# Start all services
docker-compose up --build

# Access the application
# Frontend: http://localhost:3000
# Backend:  http://localhost:8080/api/v1
# Swagger:  http://localhost:8080/api/v1/swagger-ui.html
```

### Option B — Local Development

**Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

**Database:**
```bash
# Run PostgreSQL only
docker-compose up postgres
```

---

## 💻 Development Workflow

### Branch Strategy

| Branch Pattern | Purpose |
|---|---|
| `main` | Production-ready code |
| `develop` | Integration branch |
| `feature/xyz` | New features |
| `bugfix/xyz` | Bug fixes |
| `hotfix/xyz` | Urgent production fixes |
| `chore/xyz` | Tooling, dependencies, docs |

### Commit Convention

This project uses [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add audio recorder component
fix: resolve JWT token expiry issue
chore: upgrade Spring Boot to 3.3.1
docs: update architecture diagram
refactor: extract STT provider interface
test: add unit tests for InterviewService
```

### Code Quality

- All PRs must pass CI before merge
- No direct commits to `main` or `develop`
- Backend: minimum 80% test coverage (target)
- Frontend: ESLint + TypeScript strict mode

---

## 📖 Documentation

| Document | Description |
|---|---|
| [`docs/architecture.md`](docs/architecture.md) | System architecture and design decisions |
| [`docs/api-spec.md`](docs/api-spec.md) | REST API endpoint catalogue |
| [`docs/database-design.md`](docs/database-design.md) | Entity relationships and schema |
| [`docs/agent-design.md`](docs/agent-design.md) | AI agent architecture and evaluation flow |
| [`docs/speech-module.md`](docs/speech-module.md) | STT abstraction layer design |
| [`docs/ai-provider.md`](docs/ai-provider.md) | LLM abstraction layer design |
| [`docs/deployment.md`](docs/deployment.md) | Deployment guide |
| [`docs/backend-structure.md`](docs/backend-structure.md) | Backend module and layer guide |
| [`docs/frontend-structure.md`](docs/frontend-structure.md) | Frontend directory and component guide |
| [`docs/coding-guidelines.md`](docs/coding-guidelines.md) | Code style and conventions |
| [`docs/future-roadmap.md`](docs/future-roadmap.md) | v2 and v3 feature roadmap |

---

<div align="center">

Built with ❤️ · v1.0.0-SNAPSHOT · Audio-first AI Interview Platform

</div>
