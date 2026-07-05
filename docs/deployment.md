# Deployment Guide

> **Status:** Placeholder — to be completed during DevOps phase.

## Infrastructure

| Component | Technology |
|---|---|
| Backend container | Docker |
| Frontend container | Docker |
| Database | PostgreSQL (Docker / managed) |
| Container orchestration | Docker Compose (v1) |
| CI/CD | GitHub Actions (future) |

## Quick Start (Local)

```bash
docker-compose up --build
```

_Detailed deployment steps TBD._

## Environments

| Environment | Description |
|---|---|
| local | Developer machine via Docker Compose |
| staging | Cloud VM with Docker Compose |
| production | TBD (Kubernetes / ECS future scope) |

## GitHub Actions Pipelines (Future)

- CI: build, test, lint on every PR
- CD: deploy to staging on merge to main

_Workflow files will be added to .github/workflows/._
