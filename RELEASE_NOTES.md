# Growth Archive MVP Release Notes

## MVP Coverage

- Guest can browse public library, books, people, meetings, reviews, about, terms, and privacy pages.
- Member can log in through Kakao mock/local flow, verify invite code, agree to terms, complete onboarding, and use member features.
- Member can create reading records, manual UNVERIFIED books, monthly action plans, monthly reflections, small meetings, attendance, and meeting reviews.
- Participation status counts reading records or monthly action plans and excludes deleted content.
- Admin can manage members, invite code, interest tags, recommended books, regular meetings, participation, and content visibility without rewriting member-owned content.
- Import tooling supports OPS-001 CSV templates, dry-run reports, join intro commit mode, reading record/review/meeting validation, and hidden handling for unmapped authors.

## Verification

- Backend: `./gradlew test`, `./gradlew bootJar`
- Frontend: `npm run typecheck`, `npm run lint`, `npm run build`
- Docker Compose: `docker compose up --build -d`
- Smoke checks: health endpoint, auth cookies, admin APIs, participation CSV, upload/review flow, import dry-run and join-intro commit mode.

## Known Limitations

- Real Kakao OAuth and Kakao Book Search require production API keys.
- Supabase Storage requires `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`, and bucket configuration; local development stores image metadata without persistent image bytes when not configured.
- Reading-record representative image upload and profile-image upload are not yet wired in the MVP UI; meeting-review image upload is implemented.
- Import tooling is intentionally semi-automated. Operators must prepare CSVs, map members, and select review photos manually.
- Soft-delete participation exclusion is implemented through `status = 'ACTIVE'` queries, but still needs a dedicated repository/integration test with PostgreSQL/Testcontainers.
- Lint has existing warnings for `<img>` usage in meeting/review image views.
- Final production infrastructure, Kubernetes manifests, and Helm charts are outside MVP implementation scope.
