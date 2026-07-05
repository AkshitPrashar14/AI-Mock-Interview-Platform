# Frontend Structure

> **Status:** Scaffold only — no implementation.

## Overview

The frontend is a Next.js 14 application with TypeScript, TailwindCSS, and shadcn/ui.

## Directory Map

| Directory | Responsibility |
|---|---|
| pp/ | Next.js App Router pages |
| components/ | Reusable React components |
| hooks/ | Custom React hooks |
| lib/ | Third-party library configurations |
| services/ | API client functions |
| store/ | Global state management |
| 	ypes/ | TypeScript type definitions |
| styles/ | Global CSS and Tailwind overrides |
| public/ | Static assets |
| utils/ | Pure utility functions |

## Pages

| Route | Directory | Description |
|---|---|---|
| /login | pp/login | Candidate login |
| /signup | pp/signup | Candidate registration |
| /dashboard | pp/dashboard | Candidate dashboard |
| /interview | pp/interview | Live interview session |
| /report | pp/report | Interview report view |
| /settings | pp/settings | User settings |

## Component Groups

| Group | Directory | Contents |
|---|---|---|
| Layout | components/layout | Navbar, Sidebar, Footer |
| Dashboard | components/dashboard | Stats cards, session history |
| Interview | components/interview | Audio recorder, question display, timer |
| Report | components/report | Score cards, feedback sections |
| UI | components/ui | shadcn/ui re-exports and custom primitives |
