$ErrorActionPreference = "Stop"

# Configure Git for the user
git config user.email "akshitprashar14@gmail.com"
git config user.name "AkshitPrashar14"

Write-Host "Resetting to root commit..."
# The root commit hash we discovered earlier
git reset 49e7e4e

Write-Host "Amending root commit author..."
git commit --amend --author="AkshitPrashar14 <akshitprashar14@gmail.com>" --no-edit

$commits = @(
    @{ msg = "build(backend): project scaffolding, POM, and docker configuration"; paths = @("backend/pom.xml", "backend/.gitignore", "backend/.dockerignore", "docker-compose.yml", "docker", "backend/src/main/resources/application.yml", "backend/src/main/java/com/interviewplatform/AiMockInterviewPlatformApplication.java", "backend/src/main/java/com/interviewplatform/exception") },
    @{ msg = "feat(db): implement flyway migrations and database schema"; paths = @("backend/src/main/resources/db") },
    @{ msg = "feat(user): implement User entity and JPA repositories"; paths = @("backend/src/main/java/com/interviewplatform/user", "backend/src/main/java/com/interviewplatform/auth/repository") },
    @{ msg = "feat(auth): implement JWT security and authentication services"; paths = @("backend/src/main/java/com/interviewplatform/security", "backend/src/main/java/com/interviewplatform/auth/service") },
    @{ msg = "feat(auth): implement REST controllers for user authentication"; paths = @("backend/src/main/java/com/interviewplatform/auth/controller", "backend/src/main/java/com/interviewplatform/auth/dto") },
    @{ msg = "feat(interview): define core interview entities and repositories"; paths = @("backend/src/main/java/com/interviewplatform/interview/entity", "backend/src/main/java/com/interviewplatform/interview/repository") },
    @{ msg = "feat(interview): implement interview session management services"; paths = @("backend/src/main/java/com/interviewplatform/interview/service", "backend/src/main/java/com/interviewplatform/interview/dto", "backend/src/main/java/com/interviewplatform/interview/mapper") },
    @{ msg = "feat(interview): expose interview REST endpoints and WebSocket events"; paths = @("backend/src/main/java/com/interviewplatform/interview/controller", "backend/src/main/java/com/interviewplatform/websocket") },
    @{ msg = "feat(orchestrator): implement state machine and interview orchestrator"; paths = @("backend/src/main/java/com/interviewplatform/orchestrator") },
    @{ msg = "feat(ai): integrate provider abstraction layer for LLMs"; paths = @("backend/src/main/java/com/interviewplatform/ai") },
    @{ msg = "feat(agents): implement common agent context and prompt builders"; paths = @("backend/src/main/java/com/interviewplatform/agents/common", "backend/src/main/resources/prompts") },
    @{ msg = "feat(agents): implement dynamic question generation and technical evaluation"; paths = @("backend/src/main/java/com/interviewplatform/agents/interview", "backend/src/main/java/com/interviewplatform/agents/technical") },
    @{ msg = "feat(agents): implement english, behavioral evaluation, and parallel execution"; paths = @("backend/src/main/java/com/interviewplatform/agents/english", "backend/src/main/java/com/interviewplatform/agents/behavioral", "backend/src/main/java/com/interviewplatform/agents/orchestrator") },
    @{ msg = "feat(report): implement report aggregation and narrative compiler agent"; paths = @("backend/src/main/java/com/interviewplatform/agents/report", "backend/src/main/java/com/interviewplatform/agents/aggregator", "backend/src/main/java/com/interviewplatform/report") },
    @{ msg = "feat(speech): implement FastAPI Whisper speech-to-text microservice"; paths = @("speech-service", "backend/src/main/java/com/interviewplatform/speech") },
    @{ msg = "feat(feedback): implement feedback APIs and application configuration"; paths = @("backend/src/main/java/com/interviewplatform/feedback", "backend/src/main/java/com/interviewplatform/config", ".gitignore") },
    @{ msg = "feat(frontend): initialize Next.js app, UI components, and styling"; paths = @("frontend/package.json", "frontend/package-lock.json", "frontend/tsconfig.json", "frontend/tailwind.config.ts", "frontend/postcss.config.mjs", "frontend/components", "frontend/lib", "frontend/public", "frontend/app/layout.tsx", "frontend/app/globals.css", "frontend/next.config.mjs", "frontend/next-env.d.ts", "frontend/.dockerignore", "frontend/.gitignore") },
    @{ msg = "feat(frontend): implement authentication and dashboard views"; paths = @("frontend/app/login", "frontend/app/dashboard", "frontend/services", "frontend/app/page.tsx") },
    @{ msg = "feat(frontend): implement live interview room and detailed report pages"; paths = @("frontend/app/interview", "frontend/app/report") }
)

Write-Host "Creating module-wise commits..."
$count = 1
foreach ($c in $commits) {
    $added = $false
    foreach ($p in $c.paths) {
        if (Test-Path $p) {
            git add $p
            $added = $true
        }
    }
    
    if ($added) {
        Write-Host "Committing module $count/19: $($c.msg)"
        git commit -m $c.msg
        $count++
    }
}

Write-Host "Catching any remaining files..."
git add .
$status = git status --porcelain
if ($status) {
    git commit -m "chore: final code polish and integration fixes"
    Write-Host "Created final catch-all commit."
} else {
    Write-Host "No remaining files to commit."
}

Write-Host "Force pushing to GitHub..."
git push origin main --force

Write-Host "Done!"
