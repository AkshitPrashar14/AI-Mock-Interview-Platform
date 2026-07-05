# Database Design

> **Status:** Placeholder — to be completed during implementation.

## Overview

PostgreSQL is used as the primary datastore. Schema management is handled via Flyway migrations.

## Entity Relationship Diagram

_To be added._

## Tables (Planned)

| Table | Description |
|---|---|
| users | Registered candidates and admins |
| interviews | Interview session records |
| questions | Question bank |
| nswers | Candidate audio answers + transcriptions |
| evaluations | Agent evaluation scores per answer |
| eports | Generated interview reports |
| nalytics | Aggregated performance data |

## Migrations

All migrations will live in ackend/src/main/resources/db/migration/.

_No migrations have been created yet._
