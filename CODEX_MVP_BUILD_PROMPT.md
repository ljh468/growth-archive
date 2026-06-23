# CODEX_MVP_BUILD_PROMPT.md

Project: **부자습관 만들기 - Growth Archive**  
Purpose: Final build prompt for Codex  
Status: FINAL  
Last Updated: 2026-06-23

---

## 1. How to Use This File

Use this file when asking Codex to implement the Growth Archive MVP.

Recommended usage:

1. Put this file in the repository root.
2. Ensure all docs exist in the paths described below.
3. Start Codex from the repository root.
4. Ask Codex to read this file, `AGENTS.md`, and `docs/IMPLEMENTATION_PLAN.md` first.
5. Run implementation phase by phase.

Do not ask Codex to blindly implement everything without checkpoints. The MVP is large enough that phase-based execution is safer.

---

## 2. Codex Goal

Copy this into Codex as the main goal.

```text
You are implementing the MVP for "부자습관 만들기 - Growth Archive".

Growth Archive is a premium growth archive for members who read, execute, reflect, meet, and leave records of their growth.

Before coding, read the project documents in this order:

1. AGENTS.md
2. docs/prd/PRD_001_Growth_Archive.md
3. docs/prd/PRD_002_Users_Auth_Permissions_Onboarding.md
4. docs/prd/PRD_003_IA_User_Flows_Screen_Requirements.md
5. docs/prd/PRD_004_Core_Feature_Requirements.md
6. docs/design/DESIGN_001_Design_System.md
7. docs/tech/TSD_001_Architecture_DB_ERD.md
8. docs/tech/TSD_002_API_Specification.md
9. docs/ops/OPS_001_Data_Migration_Admin_Release.md
10. docs/IMPLEMENTATION_PLAN.md
11. MVP_CHECKLIST.md

Implement the MVP phase by phase according to docs/IMPLEMENTATION_PLAN.md.

Do not add features outside the MVP scope.
Do not add comments, likes, points, ranking, chat, notifications, or payment automation.
Do not rewrite member-owned content as Admin.
Do not use an account_status enum for members.
Use role + timestamp fields for member access calculation.

After each phase:
- run available tests/checks
- update MVP_CHECKLIST.md
- summarize completed work
- list skipped or blocked items
- list open questions instead of guessing

The MVP is complete only when MVP_CHECKLIST.md is substantially complete and all core flows work locally.
```

---

## 3. One-Time Repository Setup Prompt

Use this when the repository only contains documentation and no app code yet.

```text
Read AGENTS.md, docs/IMPLEMENTATION_PLAN.md, and MVP_CHECKLIST.md.

Then perform Phase 0 only:

- create backend/ Spring Boot project skeleton using Java 25 and Spring Boot 4.1.0
- create frontend/ Next.js TypeScript project skeleton
- create root docker-compose.yml
- create backend Dockerfile
- create frontend Dockerfile
- create .env.example
- add health check endpoint placeholder for backend
- update README.md with local development instructions
- do not implement domain features yet
- do not add MVP-excluded features
- update MVP_CHECKLIST.md Phase 0 items

After implementation, run whatever build/check commands are available and report results.
```

---

## 4. Recommended Phase Execution Prompts

### Phase 1. Database Foundation & Migrations

```text
Implement Phase 1 from docs/IMPLEMENTATION_PLAN.md.

Focus only on database foundation and Flyway migrations.

Use TSD_001_Architecture_DB_ERD.md as the source of truth.

Important rules:
- do not create account_status enum for members
- use role + timestamp fields
- use Long Auto Increment IDs
- add soft delete fields where required
- prepare tables for reading records, books, action plans, reflections, meetings, reviews, recommended books, activity events, participation notes, admin audit logs

After implementation:
- run backend tests or migration validation if available
- update MVP_CHECKLIST.md
- report any schema ambiguity as TODO
```

### Phase 2. Auth, Member, Onboarding

```text
Implement Phase 2 from docs/IMPLEMENTATION_PLAN.md.

Focus only on:
- Kakao OAuth flow or safe local stub with TODO for real credentials
- JWT + HttpOnly Cookie auth
- invite code verification
- terms/privacy agreement
- onboarding
- member profile basics
- role/member access calculation
- deactivation/reactivation basics

Use PRD-002 and TSD-002 as source of truth.

Do not implement reading library or meetings in this phase.

After implementation:
- add tests for access calculation where practical
- update MVP_CHECKLIST.md
```

### Phase 3. Frontend App Shell & Design System Base

```text
Implement Phase 3 from docs/IMPLEMENTATION_PLAN.md.

Focus only on:
- app shell
- routes
- PC header navigation
- mobile bottom navigation
- mobile hamburger menu
- auth/onboarding page shells
- design tokens from DESIGN-001
- shared components for loading/empty/error states

Do not implement full domain features yet.

The UI must feel premium, editorial, real-photo-first, clean, focused, archive-like, and calm but serious.

Update MVP_CHECKLIST.md after implementation.
```

### Phase 4. Book Search & Reading Library

```text
Implement Phase 4 from docs/IMPLEMENTATION_PLAN.md.

Focus only on:
- Kakao Book Search integration
- BookSearchProvider abstraction
- manual UNVERIFIED book registration
- reading record CRUD
- rating optional 1~5 integer
- blog URL public visibility
- reading library pages
- book detail page
- popular books TOP5
- recommended books display

Important rules:
- book search failure must not block record creation
- author can edit own reading record
- Admin can hide/delete but cannot edit content
- deleted reading records do not count toward participation

Update MVP_CHECKLIST.md after implementation.
```

### Phase 5. Growth People, Profile, My Page

```text
Implement Phase 5 from docs/IMPLEMENTATION_PLAN.md.

Focus only on:
- growth people page
- member profile detail /people/{memberId}
- public vs member-only profile fields
- 50살의 나 hero area
- growth statistics
- my page dashboard
- recent records limited to 3 where specified

Do not implement ranking, points, competitive badges, likes, or comments.

Update MVP_CHECKLIST.md after implementation.
```

### Phase 6. Monthly Action Plan, Reflection, Participation

```text
Implement Phase 6 from docs/IMPLEMENTATION_PLAN.md.

Focus only on:
- monthly action plan
- monthly reflection
- monthly participation calculation
- coffee support target
- admin participation view basics if required by dependencies

Participation rule:
- reading record >= 1 OR monthly action plan >= 1
- reflection does not count
- deleted records do not count
- coffee support unit is 투썸 아메리카노 1잔

Update MVP_CHECKLIST.md after implementation.
```

### Phase 7. Meetings & Small Meetings

```text
Implement Phase 7 from docs/IMPLEMENTATION_PLAN.md.

Focus only on:
- regular meeting scheduler
- monthly reading/action meeting auto creation
- meeting list/detail
- small meeting create/edit/delete rules
- attendance/cancel attendance
- capacity enforcement
- Guest vs Member location/attendee visibility

Rules:
- scheduler runs monthly 1st 00:10 KST or equivalent idempotent job
- creator can edit own small meeting
- Admin can hide/delete small meeting but cannot edit content
- Guest sees tiny avatars only, no names/profile links

Update MVP_CHECKLIST.md after implementation.
```

### Phase 8. Meeting Reviews & Images

```text
Implement Phase 8 from docs/IMPLEMENTATION_PLAN.md.

Focus only on:
- image upload
- Supabase Storage integration or safe local abstraction with TODO if credentials missing
- WebP conversion/resizing where practical
- meeting review create/list/detail
- max 10 images per review
- photo public visibility warning
- Admin hide/delete review

Rules:
- Active Member can write review without attendance restriction
- Admin cannot rewrite review content

Update MVP_CHECKLIST.md after implementation.
```

### Phase 9. Admin Console

```text
Implement Phase 9 from docs/IMPLEMENTATION_PLAN.md.

Focus only on:
- Admin dashboard
- invite code management
- interest tag management
- recommended book management
- regular meeting management
- participation status and non-participant CSV
- member deactivate/reactivate
- content hide/delete operations
- audit logging where practical

Do not add payment settlement, chat, comments, likes, notifications, ranking, or point systems.

Update MVP_CHECKLIST.md after implementation.
```

### Phase 10. Data Migration Tools

```text
Implement Phase 10 from docs/IMPLEMENTATION_PLAN.md.

Focus only on:
- CSV import structure or scripts for migration
- reading record import support
- signup greeting import support as internal/member-admin data
- meeting review import support with max 10 selected photos
- import status logging

Rules:
- existing Somoim/Notion original links are not preserved
- blog URLs are preserved
- mapped reading records can become ACTIVE
- unmapped records remain HIDDEN

Update MVP_CHECKLIST.md after implementation.
```

### Phase 11. QA, Security, Release Readiness

```text
Implement Phase 11 from docs/IMPLEMENTATION_PLAN.md.

Focus only on:
- tests
- lint/type/build checks
- Docker Compose verification
- security review
- permission review
- README update
- MVP_CHECKLIST.md completion
- known limitations

Do not add new features unless they fix a bug or complete an existing MVP requirement.

Report:
- tests run
- tests passed/failed
- known issues
- open questions
- remaining MVP checklist items
```

---

## 5. Final Full-MVP Prompt

Use this only after the repository is stable and you want Codex to continue from the current state.

```text
Read AGENTS.md, docs/IMPLEMENTATION_PLAN.md, MVP_CHECKLIST.md, and all documents under docs/.

Inspect the current repository state.

Then continue implementing the Growth Archive MVP according to the next incomplete phase in docs/IMPLEMENTATION_PLAN.md.

Do not restart completed work.
Do not rewrite stable code unnecessarily.
Do not introduce features outside MVP.
Update MVP_CHECKLIST.md after completing work.
Run available tests/checks and report results.
```

---

## 6. Review Prompt for Codex

Use this after Codex finishes a phase.

```text
Review the current repository against:

- AGENTS.md
- docs/IMPLEMENTATION_PLAN.md
- MVP_CHECKLIST.md
- relevant PRD/TSD/OPS docs

Check for:
- MVP scope violations
- missing permission checks
- member-owned content being editable by Admin
- account_status enum accidentally introduced
- image limit violations
- public/private visibility mistakes
- participation calculation mistakes
- hard-coded secrets
- missing tests
- broken mobile navigation

Then provide:
1. Critical issues
2. Important issues
3. Nice-to-have improvements
4. Checklist items to update
5. Suggested next phase
```

---

## 7. Non-Negotiable Rules

Codex must not violate these rules.

```text
1. Do not create account_status enum for members.
2. Do not store JWT in localStorage.
3. Do not allow Guest to see exact meeting location.
4. Do not allow Guest to see member-only profile fields such as job, join reason, current concern, three-year goal.
5. Do not let Admin edit member-owned record content.
6. Do not count deleted reading records/action plans toward participation.
7. Do not let book search failure block reading record creation.
8. Do not upload more than 10 images for a meeting review.
9. Do not add likes/comments/ranking/points/chat/notifications in MVP.
10. Do not commit real secrets.
```

---

## 8. Final Completion Message Template

When Codex believes a phase is complete, it should report using this format.

```text
Phase Completed:

Implemented:
- ...

Files changed:
- ...

Tests/checks run:
- ...

MVP_CHECKLIST updates:
- ...

Skipped or blocked:
- ...

Open questions:
- ...

Recommended next phase:
- ...
```

---

## 9. Final Reminder

The goal is not to build a big platform.

The goal is to build the smallest serious MVP where members can:

```text
log in
join with invite code
complete onboarding
record reading
write monthly action plan
see monthly participation
view growth profiles
join meetings
write meeting reviews
operate admin basics
```

The product should feel like:

```text
진짜 진지하고 멋있는 사람들이
자신의 성장 기록을 품격 있게 남기는 공간
```
