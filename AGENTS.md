# AGENTS.md

Project: **부자습관 만들기 - Growth Archive**  
Purpose: Guide Codex and future coding agents when implementing the MVP.  
Status: FINAL_REVIEWED  
Last Updated: 2026-06-24

---

## 1. Product Identity

Growth Archive is not a generic community board, SNS, or reading app.

It is a premium growth archive for members of **부자습관 만들기** who read, execute, reflect, meet, and leave records of their growth.

### Brand Message

> 부자습관 만들기  
> Growth Archive  
> 읽고, 실행하고, 성장한 기록을 남기는 사람들

### Product Principles

- Make recording easy and lightweight.
- Preserve real growth records.
- Prefer real photos over stock-like visuals.
- Avoid ranking, points, competitive badges, likes, comments, and social-noise features in MVP.
- Keep the product serious, clean, premium, and calm.
- Admins manage visibility and operations; they should not rewrite member-owned records.

---

## 2. Required Reading Order

Before implementing, read the project documents in this order. Use the exact repository paths and file names below. Do not look for `_FINAL` or alternate versions unless Noah explicitly renames files.

1. `docs/prd/PRD_001_Growth_Archive.md`
2. `docs/prd/PRD_002_Users_Auth_Permissions_Onboarding.md`
3. `docs/prd/PRD_003_IA_User_Flows_Screen_Requirements.md`
4. `docs/prd/PRD_004_Core_Feature_Requirements.md`
5. `docs/design/DESIGN_001_Design_System.md`
6. `docs/tech/TSD_001_Architecture_DB_ERD.md`
7. `docs/tech/TSD_002_API_Specification.md`
8. `docs/ops/OPS_001_Data_Migration_Admin_Release.md`
9. `docs/IMPLEMENTATION_PLAN.md`
10. `MVP_CHECKLIST.md`

If a file is missing, do not invent requirements. Create or update a TODO/Open Questions section and continue only with non-blocking work.

---

## 2.1 Karpathy Guidelines for Coding Agents

Use these behavioral guidelines when writing, reviewing, or refactoring code in this repository. They are included directly here so future agents do not need to rely on external skill loading.

### Think Before Coding

Do not assume or hide confusion.

- State assumptions explicitly when requirements are ambiguous.
- If multiple interpretations exist, surface them instead of silently choosing one.
- If a simpler approach exists, mention it.
- If a decision is product-critical, security-critical, billing-related, destructive, or impossible to infer from local context, stop and ask Noah.

### Simplicity First

Write the minimum code that solves the documented requirement.

- Do not add features beyond what was requested or documented.
- Do not add abstractions for single-use code.
- Do not add speculative configurability or future-proofing.
- Do not add complex error handling for impossible scenarios.
- If a solution is becoming large, check whether a smaller implementation would satisfy the same acceptance criteria.

### Surgical Changes

Touch only what is needed for the task.

- Do not refactor unrelated code.
- Do not reformat unrelated files.
- Match the existing project style even if another style is personally preferred.
- Remove unused imports, variables, and helpers introduced by your own changes.
- If unrelated dead code or design issues are noticed, mention them instead of changing them.

Every changed line should trace back to the current request, project documents, or a failing verification step.

### Goal-Driven Execution

Define success criteria and verify them.

- For validation work, add or run checks that prove invalid inputs are rejected.
- For bug fixes, reproduce the bug when practical, then verify the fix.
- For refactors, run tests before and after when practical.
- For multi-step implementation, keep a short plan and update it as work completes.
- Do not mark checklist items complete unless implemented and verified or explicitly accepted by Noah.

### Priority

If guidance conflicts, follow this order:

```text
Latest Noah decision
> AGENTS.md project/domain/security rules
> PRD/TSD/DESIGN/OPS documents
> Karpathy Guidelines in this section
> general implementation preferences
```

If this section conflicts with a product requirement, security requirement, or explicit Noah decision, follow the project/user requirement and mention the tradeoff.

---

## 3. MVP Scope Control

### MVP Includes

- Kakao OAuth login
- Invite code verification
- Onboarding and member profile
- Reading library
- Kakao Book Search integration
- Manual temporary book registration when search fails
- Monthly action plan
- Monthly reflection slot
- Monthly participation status
- Meetings and small meetings
- Meeting reviews with image upload
- Growth profile showcase
- Admin management screens/APIs
- Data migration support

### MVP Excludes

- Payment
- Chat
- Push notification
- Native mobile app
- Comment system
- Likes/reactions
- Ranking/leaderboard
- Point system
- Complex badge system
- Public social feed beyond recent growth activity
- Hard delete UI
- Suspended account state

Do not add excluded features unless explicitly requested by the product owner.

---

## 4. Tech Stack

### Backend

- Java 25
- Spring Boot 4.1.0
- Spring Security
- Spring JDBC with explicit SQL for MVP
- Spring Data JPA / Querydsl may be reconsidered after MVP if repository complexity justifies it
- Flyway
- PostgreSQL via Supabase
- JWT + HttpOnly Secure Cookie
- Dockerfile required
- Stateless backend required
- Health check endpoint required

### Frontend

- Next.js
- TypeScript
- Tailwind CSS
- shadcn/ui where appropriate
- Mobile-first responsive design
- Dockerfile required
- Must be deployable to Kubernetes later

### Storage

- MVP: Supabase Storage
- Dev: Supabase Storage
- Future: personal server disk, mounted storage, or Kubernetes PersistentVolume
- Do not store persistent images inside ephemeral container filesystem.
- Use a storage abstraction so Supabase Storage can be replaced later.

### Deployment

- Local MVP development must run with Docker Compose.
- Local PostgreSQL runs in Docker Compose and is exposed on host port 5432.
- Production direction is Kubernetes-ready.
- Dev server direction is AWS Free Tier EC2 with separate frontend/backend Docker containers.
- Dev DB is Supabase PostgreSQL.
- Dev image/upload storage is Supabase Storage.
- Paid domain is optional for dev; EC2 public host/IP may be used first.
- Final direction may move to personal server Kubernetes/k3s or a similar self-hosted environment.
- Future self-hosted Kubernetes may use local disks through PersistentVolume/local-path-provisioner/Longhorn/NFS.
- Helm chart and Kubernetes manifests are not required in the first implementation unless explicitly requested.

---

## 5. Repository Structure

Preferred monorepo structure:

```text
growth-archive/
├─ backend/
├─ frontend/
├─ docs/
│  ├─ prd/
│  │  ├─ PRD_001_Growth_Archive.md
│  │  ├─ PRD_002_Users_Auth_Permissions_Onboarding.md
│  │  ├─ PRD_003_IA_User_Flows_Screen_Requirements.md
│  │  └─ PRD_004_Core_Feature_Requirements.md
│  ├─ design/
│  │  └─ DESIGN_001_Design_System.md
│  ├─ tech/
│  │  ├─ TSD_001_Architecture_DB_ERD.md
│  │  └─ TSD_002_API_Specification.md
│  ├─ ops/
│  │  └─ OPS_001_Data_Migration_Admin_Release.md
│  └─ IMPLEMENTATION_PLAN.md
├─ AGENTS.md
├─ MVP_CHECKLIST.md
└─ CODEX_MVP_BUILD_PROMPT.md
```

Use the actual repository structure above. In this project, `docs/IMPLEMENTATION_PLAN.md` lives under `docs/`, while `AGENTS.md`, `MVP_CHECKLIST.md`, and `CODEX_MVP_BUILD_PROMPT.md` live in the repository root.

---

## 6. Backend Implementation Rules

### Package

Default base package:

```text
com.growtharchive
```

If an existing package name exists, follow the existing package name.

### Layering

Use a clear layered structure:

```text
controller
application/service
domain
repository
dto
config
security
scheduler
storage
external
```

### Entity Rules

- Use `Long` auto-increment IDs for primary entities.
- Use `created_at`, `updated_at` timestamps where appropriate.
- Use `deleted_at` for soft delete where deletion is required.
- Do not use an `account_status` enum for members.
- Member access is calculated from timestamp fields.

### Member State Model

Use these fields:

```text
role: MEMBER | ADMIN
invite_verified_at
terms_agreed_at
privacy_agreed_at
onboarding_completed_at
deactivated_at
```

Interpretation:

```text
invite_verified_at IS NOT NULL
= invite code verified

terms_agreed_at IS NOT NULL AND privacy_agreed_at IS NOT NULL
= terms accepted

onboarding_completed_at IS NOT NULL AND deactivated_at IS NULL
= active member

role = ADMIN AND active member
= admin

deactivated_at IS NOT NULL
= deactivated member
```

### Content Status

Content records may use status fields where needed:

```text
ACTIVE
HIDDEN
DELETED
```

Meetings may use:

```text
SCHEDULED
HELD
CANCELED
HIDDEN
DELETED
```

### Admin Editing Rule

Admins may hide/delete/manage records, but must not directly rewrite member-owned content such as:

- reading record content
- meeting review content
- small meeting content created by a member

Exceptions:

- Admin may edit system/operation-managed resources such as regular monthly meetings, invite code, interest tags, and recommended books.

---

## 7. API Rules

### Base Path

Use:

```text
/api/v1
```

### Response Envelope

All API responses must use a common envelope.

Success:

```json
{
  "success": true,
  "data": {},
  "message": null
}
```

Error:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "사용자에게 보여줄 수 있는 메시지"
  }
}
```

### Access Level Terms

Use `Required Access Level` in API docs and comments.

Allowed values:

```text
PUBLIC
AUTHENTICATED
INVITE_VERIFIED
MEMBER
ADMIN
```

Do not mix account state and role terminology.

### Security

- JWT must be stored in HttpOnly cookies.
- Frontend must not store access tokens in localStorage.
- Use Secure cookie in production.
- Use SameSite=Lax by default.
- Validate Origin/Referer for unsafe methods where applicable.
- Use CORS allowlist only.
- Never commit secrets.

---

## 8. Core Domain Rules

### Invite Code

- Only one active invite code exists at a time.
- Admin can change the active invite code.
- When a new invite code is activated, the previous one becomes invalid immediately.

### Onboarding

Existing and new members both follow:

```text
Kakao login
→ invite code
→ terms/privacy agreement
→ onboarding profile
→ member access
```

Onboarding should be lightweight.

Required fields should remain minimal according to PRD-002.

### Reading Record

- Member creates reading records.
- Book search uses Kakao Book Search API.
- If search result does not exist, member can temporarily register a book.
- Manual books are stored as `UNVERIFIED`.
- Book search failure must not block writing.
- Rating is optional.
- Blog URL is public and visible to Guest.
- Reading record image may be a book cover photo or real reading proof photo.
- Author can edit own reading record.
- Admin can hide/delete, not rewrite content.

### Monthly Action Plan

- Free text.
- One action plan per member per month.
- Member-only visibility.
- Counts toward monthly participation.
- This is a monthly declaration, not a todo/task management system.

### Monthly Reflection

- A monthly reflection slot is available.
- Writing is optional.
- Not counted toward monthly participation.
- Event is created only when the member actually saves content.

### Monthly Participation

A member completes monthly participation if at least one of these is true:

```text
1. At least one reading record in the month
OR
2. One monthly action plan in the month
```

If not completed, the member is a coffee support target.

Coffee support rule:

```text
투썸 아메리카노 1잔
```

New members are counted starting from the month after onboarding completion. Existing migrated members are counted starting from the month after the official service launch month.

### Regular Meetings

Every month, two default regular meetings are auto-created:

1. Monthly Reading Record Meeting
   - 2nd Sunday of the month
   - default time: 10:00 KST

2. Monthly Action Plan Meeting
   - 4th Sunday of the month
   - default time: 10:00 KST

Scheduler:

```text
Every month on day 1 at 00:10 KST
```

Do not create duplicates if meetings already exist.

### Small Meetings

- Members can create small meetings.
- Only creator can edit.
- Admin can hide/delete.
- Admin must not rewrite member-created small meeting content.

### Meeting Reviews

- Active members can write reviews even if they did not click attendance in the system.
- Meeting review photos are public when review is public.
- Show a photo privacy notice before upload.
- Maximum review photos: 10.
- Admin can hide/delete.
- Admin must not rewrite review content.

---

## 9. Image Rules

- Single image max size: 10MB.
- Meeting review image count max: 10.
- Recommended total review upload size: 50MB or less.
- Convert uploaded images to WebP where possible.
- Resize images for web display.
- Do not store original images unless explicitly required.
- Real photos are preferred.
- Avoid stock-like, fake, luxury-flex, or AI-generated people images.

---

## 10. Design Rules

Follow `docs/design/DESIGN_001_Design_System.md`.

### Visual Direction

```text
Premium
Editorial
Real-photo-first
Clean
Focused
Archive
Calm but serious
```

### Color Direction

Quiet Luxury Archive:

```text
Ivory       #F6F1E8
Warm White  #FFFDF8
Ink         #171717
Charcoal    #2A2926
Bronze      #A97845
Deep Green  #1F4D3A
Line        #E5DCCF
```

### Avoid

- Generic bulletin-board UI
- Cheap finance/investment feeling
- Excessive gold
- Money piles, supercars, luxury watch imagery
- Ranking-first UI
- Emoji-heavy UI
- Loud gamification

### Mobile

- Mobile-first.
- Bottom navigation:
  - Home
  - Library
  - Meetings
  - People
  - My
- Mobile hamburger menu includes:
  - Meeting Reviews
  - About
  - Terms
  - Privacy Policy

---

## 11. Data Migration Rules

- Existing reading records are migrated in full where possible.
- Existing reading records mapped to onboarded members are published.
- If the original author is not yet mapped/onboarded, keep the record hidden until mapping is complete.
- Existing meeting review photos must be manually selected by admins, max 10 per review.
- Existing join introductions are migrated, but not publicly shown to Guests.
- Original Somoim/Notion source links are not preserved.
- Personal blog URLs in reading records are preserved.
- Migration working data may be managed through Google Sheet or CSV.

---

## 12. Scheduler Jobs

Implement scheduled jobs carefully and idempotently.

Required jobs:

1. Regular monthly meeting creation
2. Monthly reflection slot availability if implemented as materialized records
3. Participation status calculation or refresh if using stored snapshots
4. Optional future job: verify or enrich `UNVERIFIED` manual books

Scheduler jobs must avoid duplicate records.

---

## 13. Testing Rules

At minimum, implement tests for:

- Authentication and onboarding flow
- Invite code validation
- Member access calculation from timestamp fields
- Admin access check
- Reading record creation with Kakao book and manual book
- Monthly participation calculation
- New member participation start month
- Regular meeting scheduler idempotency
- Meeting capacity enforcement
- Soft delete exclusion from participation calculation
- Image count validation for meeting reviews
- Admin cannot rewrite member-owned content

Use project-appropriate test tooling.

Suggested backend:

```text
JUnit 5
Spring Boot Test
Testcontainers or isolated test DB if available
```

Suggested frontend:

```text
TypeScript type check
Component-level tests where practical
E2E tests for critical flows if practical
```

---

## 14. Work Process for Codex

When asked to implement:

1. Read all documents in the required order.
2. Summarize the implementation phase before changing files.
3. Implement only the requested phase.
4. Keep changes small enough to review.
5. Run available tests and build checks.
6. Update `MVP_CHECKLIST.md` after completing each phase.
7. If requirements conflict, follow this priority:
   1. Latest user decision in conversation or updated document
   2. TSD/API documents for technical details
   3. PRD documents for product behavior
   4. Design documents for UI/UX
   5. OPS documents for migration/release
8. If ambiguity remains, add it to `OPEN_QUESTIONS.md` or the relevant TODO section instead of guessing.

---

## 15. Prohibited Actions

Do not:

- Commit `.env` or secrets.
- Store JWT in localStorage.
- Add ranking, likes, comments, point system, or payment in MVP.
- Rewrite member-owned content through Admin APIs.
- Store permanent images inside container ephemeral disk.
- Use unsupported scraping of bookstore sites for MVP.
- Preserve original Somoim/Notion source links unless explicitly requested later.
- Implement complex features not present in PRD/TSD/OPS documents.

---

## 16. Final MVP Definition

The MVP is complete when:

- A Guest can browse public reading library, books, people showcase, meetings preview, reviews, and about page.
- A member can log in with Kakao, verify invite code, complete onboarding, and use member features.
- A member can create reading records and monthly action plans.
- Monthly participation status works.
- A member can create/attend meetings and write reviews.
- Admin can manage invite code, tags, recommended books, meetings, visibility, and participation lists.
- The app runs locally with Docker Compose.
- Backend and frontend are containerized and Kubernetes-ready.
- MVP checklist is complete.
