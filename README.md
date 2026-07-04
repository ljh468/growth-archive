# Growth Archive

부자습관 만들기 Growth Archive MVP.

읽고, 실행하고, 성장한 기록을 남기는 사람들을 위한 프리미엄 성장 아카이브입니다.

## Stack

- Backend: Java 25, Spring Boot 4.1.0, Spring Security, JDBC/JPA, Flyway
- Frontend: Next.js, TypeScript, Tailwind CSS
- Database: PostgreSQL
- Storage: Supabase Storage abstraction with local development metadata fallback
- Local runtime: Docker Compose

## Prerequisites

- Java 25
- Node.js 22+
- npm
- Docker 27+
- Optional for import scripts: `python3`, `psql`

## Local Run

```bash
cp .env.example .env
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
docker compose up --build
```

Services:

- Frontend: `http://localhost:3000`
- Backend health: `http://localhost:8080/api/v1/health`
- Actuator health: `http://localhost:8080/actuator/health`
- PostgreSQL: `localhost:5432`

Local Kakao OAuth mock is enabled by default. `INITIAL_INVITE_CODE=local-dev-invite` is used for local onboarding.

## Backend Checks

```bash
cd backend
./gradlew test
./gradlew bootJar
```

## Frontend Checks

```bash
cd frontend
npm install
npm run typecheck
npm run lint
npm run build
```

`npm run lint` currently passes with warnings for existing `<img>` usage on meeting/review image views.

## Data Import

CSV templates and dry-run/commit tooling live under `scripts/import/`.

```bash
python3 scripts/import/import_growth_archive.py --type reading-records --csv scripts/import/templates/reading_record.csv --dry-run
```

Commit mode requires a database URL or a `psql` command. See `scripts/import/README.md`.

## Security Notes

- JWT is stored in HttpOnly cookies, not browser storage.
- SameSite is `Lax`.
- `JWT_SECURE_COOKIE=true` must be used in production HTTPS.
- Unsafe methods require an allowed `Origin` or `Referer`.
- CORS origins are controlled by `CORS_ALLOWED_ORIGINS`.
- Do not commit real secrets. Example files contain placeholders only.

## MVP Scope

Included:

- Kakao login and invite-code onboarding
- Member profiles and growth profile showcase
- Reading library, Kakao book search, manual UNVERIFIED books
- Monthly action plans, reflections, and participation status
- Regular meetings, small meetings, attendance
- Meeting reviews with up to 10 images
- Admin dashboard, member management, invite code, tags, recommended books, participation, moderation
- CSV-based migration support

Excluded from MVP:

- Payment, chat, push notifications, native app
- Likes, comments, rankings, points, complex badges
- Public social feed beyond recent growth activity
- Hard-delete UI
