# AI Mock Interview Platform - Startup Guide

This document outlines the standard process for building, testing, and running the AI Mock Interview Platform locally without running into environment conflicts.

## Prerequisites
- Docker & Docker Compose
- Java 21
- Maven (optional if you use the dockerized maven command below)

## 1. Running Infrastructure
Start the required databases (PostgreSQL) using Docker Compose:
```powershell
docker-compose up -d postgres
```

## 2. Building and Testing the Backend
To ensure a clean build and run tests without requiring local environment variables (we use the `test` profile with an H2 in-memory DB):

```powershell
# Clean the target directory to avoid stale classes locking issues (Windows)
Remove-Item -Recurse -Force "backend\target" -ErrorAction SilentlyContinue

# Run Maven test via Docker
docker run --rm -v "${PWD}/backend:/app" -v "$env:USERPROFILE\.m2:/root/.m2" -w /app maven:3.9.9-eclipse-temurin-21 mvn clean test --no-transfer-progress
```

## 3. Running the Backend Server
The server is configured to run on port **8081** locally (to avoid conflicts with Jenkins/other default apps on 8080).

Start the Spring Boot application:
```powershell
docker run --rm -v "${PWD}/backend:/app" -v "$env:USERPROFILE\.m2:/root/.m2" -p 8081:8081 -w /app maven:3.9.9-eclipse-temurin-21 mvn spring-boot:run
```
*(Alternatively, if you have Maven installed locally, you can run `mvn spring-boot:run` inside the `backend/` directory).*

## 4. Accessing the API
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **API Base URL:** http://localhost:8081/api/v1/

## Troubleshooting
- **Port Conflicts:** If `8081` is taken, modify the `server.port` in `backend/src/main/resources/application.yml`.
- **Test Failures (ApplicationContext):** If tests fail to load context, ensure you are running a `clean test` to purge stale `.class` files.
