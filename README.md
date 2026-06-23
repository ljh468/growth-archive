# Growth Archive

부자습관 만들기 Growth Archive MVP.

읽고, 실행하고, 성장한 기록을 남기는 사람들을 위한 프리미엄 성장 아카이브입니다.

## Stack

- Backend: Java 25, Spring Boot 4.1.0, Spring Security, Spring Data JPA, Flyway
- Frontend: Next.js, TypeScript, Tailwind CSS
- Local runtime: Docker Compose with PostgreSQL

## Prerequisites

- Java 25
- Node.js 22+
- npm 11+
- Docker 27+

## Local Environment

Copy the example files before running locally:

```bash
cp .env.example .env
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
```

The example files contain placeholders only. Do not commit real secrets.

## Run With Docker Compose

```bash
docker compose up --build
```

Services:

- Frontend: `http://localhost:3000`
- Backend health: `http://localhost:8080/api/v1/health`
- Actuator health: `http://localhost:8080/actuator/health`
- PostgreSQL: `localhost:5432`

## Backend

```bash
cd backend
./gradlew test
./gradlew bootRun
```

The backend uses the base package `com.growtharchive`.

## Frontend

```bash
cd frontend
npm install
npm run typecheck
npm run build
npm run dev
```

## Phase Status

Phase 0 creates only the repository bootstrap:

- backend Spring Boot skeleton
- frontend Next.js skeleton
- Dockerfiles
- Docker Compose
- environment examples
- health check endpoint

Domain features start in later phases.
