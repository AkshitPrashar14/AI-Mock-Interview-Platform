# Coding Guidelines

> These are the conventions to follow across this project.

## Backend (Java / Spring Boot)

### General
- Java 21 features are encouraged (records, sealed classes, pattern matching)
- All public APIs must have Javadoc
- Use @Slf4j (Lombok) for logging — no System.out.println
- Never log sensitive data (passwords, tokens, PII)

### Layering Rules
- Controllers depend on Services (interface only)
- Services depend on Repositories (interface only)
- No JPA entity should leak into the controller layer — always use DTOs
- Use MapStruct for entity ↔ DTO conversion

### Error Handling
- All custom exceptions extend from a base exception in exception/
- Use @ControllerAdvice global handler — no try/catch in controllers

### Security
- All endpoints must be explicitly configured in SecurityConfig
- JWT secrets must come from environment variables — never hardcoded

### Testing
- Unit tests for every service class
- Integration tests for every controller
- Use @SpringBootTest sparingly — prefer slice tests (@WebMvcTest, @DataJpaTest)

## Frontend (TypeScript / Next.js)

### General
- Strict TypeScript — no ny
- Use React Server Components where possible
- Client components must be explicitly marked 'use client'
- All API calls go through the services/ layer — never fetch directly in components

### State Management
- Prefer React Context / hooks for local state
- Use Zustand (or equivalent) for global state — add to store/

### Styling
- TailwindCSS utility classes only
- Custom tokens in 	ailwind.config.ts
- No inline styles

### Naming Conventions
- Components: PascalCase
- Hooks: useXxx
- Services: xxxService.ts
- Types: XxxType.ts or interfaces.ts

## Git Workflow

- Branch naming: eature/, ugfix/, hotfix/, chore/
- Commit style: Conventional Commits (eat:, ix:, chore:, docs:)
- PRs must pass CI before merge
- No direct commits to main
"@

    "future-roadmap.md" = @"
# Future Roadmap

## v1 — Current Scope

- [x] Repository scaffold and project structure
- [ ] Authentication (JWT)
- [ ] User management
- [ ] Audio-based interview sessions
- [ ] Local STT integration (Vosk / Whisper)
- [ ] AI agent integration (Technical, English, Behavioral)
- [ ] Report generation
- [ ] Candidate dashboard
- [ ] Docker Compose deployment

## v2 — Planned

- [ ] Video analysis support (facial expressions, eye contact)
- [ ] Real-time transcription (streaming STT)
- [ ] Cloud STT provider options (Google, Azure)
- [ ] Multi-language interview support
- [ ] Admin panel and analytics dashboard
- [ ] Email notifications

## v3 — Future

- [ ] Microservices migration (extract agents and speech into services)
- [ ] Kubernetes deployment
- [ ] Candidate performance benchmarking across cohorts
- [ ] Integration with ATS (Applicant Tracking Systems)
- [ ] Mobile app (React Native)
- [ ] Enterprise white-label offering
