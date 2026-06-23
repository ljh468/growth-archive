# CODEX PHASE GOALS

Project: 부자습관 만들기 - Growth Archive  
Purpose: Codex가 Growth Archive MVP를 Phase 단위로 멈추지 않고 구현할 수 있도록 사용하는 `/goal` 프롬프트 모음  
Status: Ready for Codex execution  
Last Updated: 2026-06-23

---

## 0. 사용 원칙

이 문서는 Codex에게 한 번에 전체 MVP를 던지기보다, **Phase 단위로 안정적으로 구현**시키기 위한 프롬프트 모음이다.

권장 방식은 다음과 같다.

```text
1. Phase 0 실행
2. 결과 확인
3. git diff 확인
4. build/test 확인
5. 커밋
6. 다음 Phase 실행
```

각 Phase 안에서는 Codex가 멈추지 않고 진행하도록 지시하되, Phase 사이에서는 사람이 검토한다.

---

## 1. 공통 문서 경로

현재 프로젝트 문서 구조는 아래를 기준으로 한다.

```text
growth-archive/
├─ AGENTS.md
├─ MVP_CHECKLIST.md
├─ CODEX_MVP_BUILD_PROMPT.md
├─ README.md
├─ .gitignore
└─ docs/
   ├─ IMPLEMENTATION_PLAN.md
   ├─ design/
   │  └─ DESIGN_001_Design_System.md
   ├─ ops/
   │  └─ OPS_001_Data_Migration_Admin_Release.md
   ├─ prd/
   │  ├─ PRD_001_Growth_Archive.md
   │  ├─ PRD_002_Users_Auth_Permissions_Onboarding.md
   │  ├─ PRD_003_IA_User_Flows_Screen_Requirements.md
   │  └─ PRD_004_Core_Feature_Requirements.md
   └─ tech/
      ├─ TSD_001_Architecture_DB_ERD.md
      └─ TSD_002_API_Specification.md
```

모든 Phase에서 Codex는 최소한 아래 파일을 먼저 읽어야 한다.

```text
AGENTS.md
CODEX_MVP_BUILD_PROMPT.md
MVP_CHECKLIST.md
docs/IMPLEMENTATION_PLAN.md
```

Phase별로 필요한 PRD/TSD/OPS 문서는 각 프롬프트에 별도로 명시한다.

전체 문서 검토나 새 구현 세션을 시작할 때는 `AGENTS.md`의 Required Reading Order가 우선한다.
각 Phase 프롬프트의 목록은 해당 Phase 실행에 필요한 최소 추가 문맥이다.

---

## 2. 모든 Phase에 공통으로 붙일 진행 규칙

아래 문장은 모든 Phase 프롬프트 하단에 포함한다.

```text
Do not stop after planning. After making a plan, immediately start implementing it.
Do not ask for confirmation for routine implementation choices already covered by the documents.
If a detail is missing but a safe default is defined in AGENTS.md, PRD, TSD, OPS, or DESIGN documents, use that default and continue.
If the missing detail is not safety-critical, security-critical, billing-related, or product-critical, add a TODO and continue.
If a command fails, inspect the error and attempt a fix up to 3 times before stopping.
Stop only if secrets, external account credentials, paid service setup, destructive actions, or uncovered product decisions are required.
After finishing this phase, update MVP_CHECKLIST.md and provide a final summary with changed files, commands run, test results, and remaining TODOs.
```

---

## 3. Phase 0 Goal — Repository Bootstrap

```text
/goal

Implement Phase 0 only for the Growth Archive MVP.

Before doing any work, read these files in order:

1. AGENTS.md
2. CODEX_MVP_BUILD_PROMPT.md
3. MVP_CHECKLIST.md
4. docs/IMPLEMENTATION_PLAN.md
5. docs/prd/PRD_001_Growth_Archive.md
6. docs/design/DESIGN_001_Design_System.md
7. docs/tech/TSD_001_Architecture_DB_ERD.md
8. docs/tech/TSD_002_API_Specification.md

Goal:
Create the repository bootstrap for the Growth Archive MVP.

Scope:
- Create backend Spring Boot project skeleton for Java 25 and Spring Boot 4.1.x.
- Create frontend Next.js + TypeScript + Tailwind project skeleton.
- Create backend Dockerfile.
- Create frontend Dockerfile.
- Create docker-compose.yml for local development.
- Create backend .env.example.
- Create frontend .env.example.
- Add backend health check endpoint.
- Add frontend basic app shell placeholder.
- Update README.md with local development instructions.
- Update MVP_CHECKLIST.md for Phase 0 only.

Important constraints:
- Do not implement domain features yet.
- Do not implement Kakao login yet.
- Do not create database domain tables yet, except if required for bootstrap placeholder.
- Do not add features outside the documents.
- Do not use account_status enum.
- Do not store JWT in localStorage.
- Keep the backend stateless and Kubernetes-ready.
- Use environment variables for all secrets and external endpoints.
- Do not add real secrets.

Done when:
- Backend project can build.
- Frontend project can build.
- docker-compose.yml exists and is documented.
- .env.example files exist.
- README.md explains how to run locally.
- MVP_CHECKLIST.md Phase 0 items are updated.

Do not stop after planning. After making a plan, immediately start implementing it.
Do not ask for confirmation for routine implementation choices already covered by the documents.
If a detail is missing but a safe default is defined in AGENTS.md, PRD, TSD, OPS, or DESIGN documents, use that default and continue.
If the missing detail is not safety-critical, security-critical, billing-related, or product-critical, add a TODO and continue.
If a command fails, inspect the error and attempt a fix up to 3 times before stopping.
Stop only if secrets, external account credentials, paid service setup, destructive actions, or uncovered product decisions are required.
After finishing this phase, update MVP_CHECKLIST.md and provide a final summary with changed files, commands run, test results, and remaining TODOs.
```

---

## 4. Phase 1 Goal — Database Foundation & Migrations

```text
/goal

Continue implementing the Growth Archive MVP.

Implement Phase 1 only: Database Foundation & Migrations.

Before doing any work, read these files in order:

1. AGENTS.md
2. CODEX_MVP_BUILD_PROMPT.md
3. MVP_CHECKLIST.md
4. docs/IMPLEMENTATION_PLAN.md
5. docs/tech/TSD_001_Architecture_DB_ERD.md
6. docs/tech/TSD_002_API_Specification.md
7. docs/prd/PRD_002_Users_Auth_Permissions_Onboarding.md
8. docs/prd/PRD_004_Core_Feature_Requirements.md

Goal:
Create the database foundation for the MVP.

Scope:
- Configure PostgreSQL connection for Supabase-compatible local development.
- Add Flyway.
- Set Hibernate ddl-auto to validate.
- Create initial database migrations according to TSD_001.
- Use Long Auto Increment IDs.
- Do not create account_status enum.
- Use role + timestamp fields for member lifecycle:
  - role
  - invite_verified_at
  - terms_agreed_at
  - privacy_agreed_at
  - onboarding_completed_at
  - deactivated_at
- Add created_at and updated_at to core tables.
- Add deleted_at where soft delete is required.
- Add content status fields where required for content entities.
- Add indexes required by TSD_001.
- Add basic entity/repository structure only if needed for migration validation.
- Update MVP_CHECKLIST.md for Phase 1.

Important constraints:
- Do not implement business APIs yet unless needed for health or migration validation.
- Do not invent schema outside TSD_001.
- Do not use account_status enum.
- Soft delete is the default for user-generated content.
- Hard delete is not part of MVP unless explicitly documented.

Done when:
- Flyway migrations run successfully.
- Backend build passes.
- Schema matches TSD_001.
- MVP_CHECKLIST.md Phase 1 items are updated.

Do not stop after planning. After making a plan, immediately start implementing it.
Do not ask for confirmation for routine implementation choices already covered by the documents.
If a detail is missing but a safe default is defined in AGENTS.md, PRD, TSD, OPS, or DESIGN documents, use that default and continue.
If the missing detail is not safety-critical, security-critical, billing-related, or product-critical, add a TODO and continue.
If a command fails, inspect the error and attempt a fix up to 3 times before stopping.
Stop only if secrets, external account credentials, paid service setup, destructive actions, or uncovered product decisions are required.
After finishing this phase, update MVP_CHECKLIST.md and provide a final summary with changed files, commands run, test results, and remaining TODOs.
```

---

## 5. Phase 2 Goal — Auth, Member, Onboarding

```text
/goal

Continue implementing the Growth Archive MVP.

Implement Phase 2 only: Auth, Member, Onboarding.

Before doing any work, read these files in order:

1. AGENTS.md
2. CODEX_MVP_BUILD_PROMPT.md
3. MVP_CHECKLIST.md
4. docs/IMPLEMENTATION_PLAN.md
5. docs/prd/PRD_002_Users_Auth_Permissions_Onboarding.md
6. docs/tech/TSD_001_Architecture_DB_ERD.md
7. docs/tech/TSD_002_API_Specification.md

Goal:
Implement authentication, member lifecycle, invite code verification, terms agreement, and onboarding.

Scope:
- Implement Kakao OAuth backend flow according to TSD_002.
- Implement JWT + HttpOnly Secure Cookie.
- Do not store JWT in localStorage.
- Add auth current user API.
- Add logout API.
- Add invite code verification API.
- Add terms/privacy agreement API.
- Add onboarding API.
- Enforce nickname uniqueness.
- Implement role MEMBER / ADMIN.
- Do not create account_status enum.
- Calculate access level from:
  - role
  - invite_verified_at
  - terms_agreed_at
  - privacy_agreed_at
  - onboarding_completed_at
  - deactivated_at
- Add Admin member deactivation/reactivation API if documented in TSD_002.
- Add frontend login/onboarding screens.
- Add route guards for Guest/Auth/Member/Admin flows.
- Update MVP_CHECKLIST.md for Phase 2.

Important constraints:
- No account_status enum.
- Admin = active member + role ADMIN.
- Deactivated member cannot access Member/Admin features.
- Existing members must still go through Kakao login, invite code, terms, and onboarding.
- Do not implement domain features outside auth/member/onboarding.

Done when:
- Kakao OAuth flow is scaffolded and environment-based.
- JWT cookies are issued and cleared correctly.
- Onboarding completion creates an active Member.
- Nickname duplication is prevented.
- Role/access level logic is implemented.
- Backend build/tests pass.
- Frontend build passes.
- MVP_CHECKLIST.md Phase 2 items are updated.

Do not stop after planning. After making a plan, immediately start implementing it.
Do not ask for confirmation for routine implementation choices already covered by the documents.
If a detail is missing but a safe default is defined in AGENTS.md, PRD, TSD, OPS, or DESIGN documents, use that default and continue.
If the missing detail is not safety-critical, security-critical, billing-related, or product-critical, add a TODO and continue.
If a command fails, inspect the error and attempt a fix up to 3 times before stopping.
Stop only if secrets, external account credentials, paid service setup, destructive actions, or uncovered product decisions are required.
After finishing this phase, update MVP_CHECKLIST.md and provide a final summary with changed files, commands run, test results, and remaining TODOs.
```

---

## 6. Phase 3 Goal — Frontend App Shell & Design System Base

```text
/goal

Continue implementing the Growth Archive MVP.

Implement Phase 3 only: Frontend App Shell & Design System Base.

Before doing any work, read these files in order:

1. AGENTS.md
2. CODEX_MVP_BUILD_PROMPT.md
3. MVP_CHECKLIST.md
4. docs/IMPLEMENTATION_PLAN.md
5. docs/prd/PRD_003_IA_User_Flows_Screen_Requirements.md
6. docs/design/DESIGN_001_Design_System.md
7. docs/tech/TSD_002_API_Specification.md

Goal:
Implement the frontend shell, routing structure, navigation, and base design system.

Scope:
- Implement root layout.
- Implement PC top navigation:
  - 독서기록 라이브러리
  - 모임
  - 성장하는 사람들
  - 모임 후기
  - 소개
- Implement mobile bottom navigation:
  - 홈
  - 라이브러리
  - 모임
  - 사람들
  - 마이
- Implement mobile top hamburger menu:
  - 모임 후기
  - 소개
  - 이용약관
  - 개인정보처리방침
- Implement design tokens according to DESIGN_001.
- Use Quiet Luxury Archive palette.
- Implement reusable UI primitives:
  - Button
  - Card
  - Section
  - PageHeader
  - EmptyState
  - LoadingState
  - Avatar
  - Badge/Tag
- Implement placeholder pages for all routes from PRD_003.
- Implement frontend API client using common Envelope response.
- Update MVP_CHECKLIST.md for Phase 3.

Important constraints:
- Real-photo-first design direction.
- Premium, Editorial, Clean, Focused, Calm but serious.
- Do not make it look like a generic bulletin board.
- Do not implement unrelated UI effects.
- Do not add likes, comments, ranking, points, chat, or notifications.

Done when:
- Frontend routes exist.
- Desktop/mobile navigation works.
- Design tokens exist.
- Placeholder pages render.
- Frontend build passes.
- MVP_CHECKLIST.md Phase 3 items are updated.

Do not stop after planning. After making a plan, immediately start implementing it.
Do not ask for confirmation for routine implementation choices already covered by the documents.
If a detail is missing but a safe default is defined in AGENTS.md, PRD, TSD, OPS, or DESIGN documents, use that default and continue.
If the missing detail is not safety-critical, security-critical, billing-related, or product-critical, add a TODO and continue.
If a command fails, inspect the error and attempt a fix up to 3 times before stopping.
Stop only if secrets, external account credentials, paid service setup, destructive actions, or uncovered product decisions are required.
After finishing this phase, update MVP_CHECKLIST.md and provide a final summary with changed files, commands run, test results, and remaining TODOs.
```

---

## 7. Phase 4 Goal — Book Search & Reading Library

```text
/goal

Continue implementing the Growth Archive MVP.

Implement Phase 4 only: Book Search & Reading Library.

Before doing any work, read these files in order:

1. AGENTS.md
2. CODEX_MVP_BUILD_PROMPT.md
3. MVP_CHECKLIST.md
4. docs/IMPLEMENTATION_PLAN.md
5. docs/prd/PRD_004_Core_Feature_Requirements.md
6. docs/prd/PRD_003_IA_User_Flows_Screen_Requirements.md
7. docs/tech/TSD_001_Architecture_DB_ERD.md
8. docs/tech/TSD_002_API_Specification.md
9. docs/design/DESIGN_001_Design_System.md

Goal:
Implement Kakao book search, Book entity, Reading Record features, reading library pages, and related admin controls.

Scope:
- Implement BookSearchProvider interface.
- Implement KakaoBookSearchProvider.
- Implement book search API.
- Implement book selection/import into internal books table.
- If external search returns no result, allow Member to create UNVERIFIED book manually.
- Implement reading record CRUD.
- Rating is optional.
- Rating unit is 1~5 integer stars.
- Representative image can be book cover or real reading proof photo.
- Blog URL is visible to Guest.
- Author can edit own reading record.
- Admin cannot edit reading record content.
- Admin can hide/delete/restore reading records according to TSD_002.
- Implement library main page:
  - 이달의 추천책
  - 인기 도서 TOP5
  - 최근 독서기록
  - 책 검색 entry
- Implement books list/search page.
- Implement book detail page:
  - book info
  - average rating
  - reading record count
  - people who read this book
  - author-based accordion reading records
- Implement recommended books Admin API/UI if within Phase 4 scope.
- Update MVP_CHECKLIST.md for Phase 4.

Important constraints:
- Kakao Book Search API is MVP default.
- Do not block writing if book search fails.
- Directly registered books must be UNVERIFIED.
- Guest can view public reading records and blog URLs.
- Admin cannot directly modify member-created reading record content.
- Soft delete/hide should be respected in public queries.

Done when:
- Member can create a reading record.
- Guest can browse reading library and book details.
- Book search works or is safely stubbed behind provider interface if API key is missing.
- Direct UNVERIFIED book creation works.
- Popular books TOP5 calculation works.
- Recommended books display works.
- Backend build/tests pass.
- Frontend build passes.
- MVP_CHECKLIST.md Phase 4 items are updated.

Do not stop after planning. After making a plan, immediately start implementing it.
Do not ask for confirmation for routine implementation choices already covered by the documents.
If a detail is missing but a safe default is defined in AGENTS.md, PRD, TSD, OPS, or DESIGN documents, use that default and continue.
If the missing detail is not safety-critical, security-critical, billing-related, or product-critical, add a TODO and continue.
If a command fails, inspect the error and attempt a fix up to 3 times before stopping.
Stop only if secrets, external account credentials, paid service setup, destructive actions, or uncovered product decisions are required.
After finishing this phase, update MVP_CHECKLIST.md and provide a final summary with changed files, commands run, test results, and remaining TODOs.
```

---

## 8. Phase 5 Goal — Growth People, Profile, My Page

```text
/goal

Continue implementing the Growth Archive MVP.

Implement Phase 5 only: Growth People, Profile, My Page.

Before doing any work, read these files in order:

1. AGENTS.md
2. CODEX_MVP_BUILD_PROMPT.md
3. MVP_CHECKLIST.md
4. docs/IMPLEMENTATION_PLAN.md
5. docs/prd/PRD_002_Users_Auth_Permissions_Onboarding.md
6. docs/prd/PRD_003_IA_User_Flows_Screen_Requirements.md
7. docs/prd/PRD_004_Core_Feature_Requirements.md
8. docs/design/DESIGN_001_Design_System.md
9. docs/tech/TSD_002_API_Specification.md

Goal:
Implement Growth People showcase, public/member profile pages, and My Page dashboard.

Scope:
- Implement /people page as growth profile showcase, not a generic member list.
- Implement member cards using available fields:
  - profile image
  - display name
  - one-line intro
  - interest tags
  - future me at 50 summary
  - growth stats summary
  - recent public activity
- Implement /people/{memberId} profile detail.
- URL must use memberId.
- Guest can see public profile subset.
- Member can see deeper profile fields according to PRD_002/PRD_003.
- Do not collect or display age in MVP.
- Job is Member-only.
- Join reason, current concern, three-year goal are Member-only.
- Implement profile edit page for current member.
- Implement profile image fallback:
  1. uploaded image
  2. Kakao profile image
  3. default profile image
- Implement /mypage dashboard.
- My Page top section must show monthly participation status.
- Add quick actions:
  - 독서기록 작성
  - 실행계획 작성
  - 월간회고 작성
  - 소소모임 만들기
- Implement GET /me/dashboard if documented in TSD_002.
- Update MVP_CHECKLIST.md for Phase 5.

Important constraints:
- 성장하는 사람들 is a showcase of serious, premium growth-oriented people.
- No ranking or competitive leaderboard.
- UI can be improved later, but required data fields and permission rules must be implemented now.
- Guest must not see Member-only personal details.

Done when:
- Guest can view Growth People cards and public profile details.
- Member can view additional profile information.
- Current member can edit own profile.
- My Page dashboard renders monthly participation summary and quick actions.
- Backend build/tests pass.
- Frontend build passes.
- MVP_CHECKLIST.md Phase 5 items are updated.

Do not stop after planning. After making a plan, immediately start implementing it.
Do not ask for confirmation for routine implementation choices already covered by the documents.
If a detail is missing but a safe default is defined in AGENTS.md, PRD, TSD, OPS, or DESIGN documents, use that default and continue.
If the missing detail is not safety-critical, security-critical, billing-related, or product-critical, add a TODO and continue.
If a command fails, inspect the error and attempt a fix up to 3 times before stopping.
Stop only if secrets, external account credentials, paid service setup, destructive actions, or uncovered product decisions are required.
After finishing this phase, update MVP_CHECKLIST.md and provide a final summary with changed files, commands run, test results, and remaining TODOs.
```

---

## 9. Phase 6 Goal — Monthly Action Plan, Reflection, Participation

```text
/goal

Continue implementing the Growth Archive MVP.

Implement Phase 6 only: Monthly Action Plan, Monthly Reflection, Participation Status.

Before doing any work, read these files in order:

1. AGENTS.md
2. CODEX_MVP_BUILD_PROMPT.md
3. MVP_CHECKLIST.md
4. docs/IMPLEMENTATION_PLAN.md
5. docs/prd/PRD_004_Core_Feature_Requirements.md
6. docs/prd/PRD_003_IA_User_Flows_Screen_Requirements.md
7. docs/tech/TSD_001_Architecture_DB_ERD.md
8. docs/tech/TSD_002_API_Specification.md

Goal:
Implement monthly action plans, monthly reflections, and monthly participation calculation.

Scope:
- Implement monthly action plan CRUD.
- Monthly action plan is free-form.
- One member can have one action plan per month.
- Action plan is Member-only.
- Action plan counts toward monthly participation.
- Implement monthly reflection API/UI.
- Monthly reflection slot is available every month.
- Reflection is optional.
- Reflection does not count toward participation.
- Reflection write event is created only when user actually saves content.
- Implement participation calculation:
  - Completed if reading record count >= 1 for month using `reading_records.recorded_at`
  - OR action plan count >= 1 for month
  - Otherwise participation needed
- New members are counted starting from the month after official onboarding/participation start.
- Implement coffee support target flag:
  - If not completed after calculation period, target for 투썸 아메리카노 1잔.
- Implement Admin monthly participation list and CSV export if documented in TSD_002.
- Implement Admin participation memo if documented.
- Update My Page participation card.
- Update MVP_CHECKLIST.md for Phase 6.

Important constraints:
- Participation is not based on meeting attendance.
- Participation is based only on reading record or monthly action plan.
- Do not add payment or settlement features.
- Coffee support is a management label only.
- No ranking or points.

Done when:
- Member can create/edit/delete monthly action plan.
- Member can create/edit monthly reflection.
- My Page correctly shows participation status.
- Admin can see completed/needed/coffee support target status by month.
- CSV export works if included in API spec.
- Backend build/tests pass.
- Frontend build passes.
- MVP_CHECKLIST.md Phase 6 items are updated.

Do not stop after planning. After making a plan, immediately start implementing it.
Do not ask for confirmation for routine implementation choices already covered by the documents.
If a detail is missing but a safe default is defined in AGENTS.md, PRD, TSD, OPS, or DESIGN documents, use that default and continue.
If the missing detail is not safety-critical, security-critical, billing-related, or product-critical, add a TODO and continue.
If a command fails, inspect the error and attempt a fix up to 3 times before stopping.
Stop only if secrets, external account credentials, paid service setup, destructive actions, or uncovered product decisions are required.
After finishing this phase, update MVP_CHECKLIST.md and provide a final summary with changed files, commands run, test results, and remaining TODOs.
```

---

## 10. Phase 7 Goal — Meetings & Small Meetings

```text
/goal

Continue implementing the Growth Archive MVP.

Implement Phase 7 only: Meetings & Small Meetings.

Before doing any work, read these files in order:

1. AGENTS.md
2. CODEX_MVP_BUILD_PROMPT.md
3. MVP_CHECKLIST.md
4. docs/IMPLEMENTATION_PLAN.md
5. docs/prd/PRD_003_IA_User_Flows_Screen_Requirements.md
6. docs/prd/PRD_004_Core_Feature_Requirements.md
7. docs/tech/TSD_001_Architecture_DB_ERD.md
8. docs/tech/TSD_002_API_Specification.md
9. docs/design/DESIGN_001_Design_System.md

Goal:
Implement regular meetings, small meetings, attendance, and meeting scheduler.

Scope:
- Implement meetings list page.
- Implement meeting detail page.
- Implement Member attendance join/cancel.
- Guest can see:
  - meeting title
  - description
  - datetime
  - region-level location
  - attendee count
  - tiny profile image stack preview
- Guest cannot see:
  - exact location
  - attendee names
  - clickable attendee profile links
- Member can see:
  - exact location
  - attendee profile images
  - attendee display names
  - profile links
  - attend/cancel button
- Implement regular meeting auto creation scheduler:
  - Runs every month on day 1 at 00:10 KST
  - Creates monthly reading record meeting on second Sunday at 10:00 KST
  - Creates monthly action plan meeting on fourth Sunday at 10:00 KST
  - Does not duplicate if already created
- Admin can modify regular meeting operation fields.
- Implement small meeting creation by Member.
- Small meeting creator can edit own small meeting.
- Admin cannot directly edit small meeting content.
- Admin can hide/delete small meetings.
- Implement capacity policy:
  - null or 0 = unlimited
  - capacity > 0 = cannot exceed capacity
  - no waitlist in MVP
- Implement meeting status:
  - SCHEDULED
  - HELD
  - CANCELED
  - HIDDEN
  - DELETED
- Update MVP_CHECKLIST.md for Phase 7.

Important constraints:
- Guest exact location must be hidden.
- Guest attendee preview must be tiny and non-clickable.
- Small meeting creator is the only editor.
- Admin handles hide/delete, not content rewriting.
- Meeting attendance does not count toward monthly participation.

Done when:
- Guest can view safe meeting information.
- Member can view exact meeting information and attend.
- Small meeting create/edit/delete rules work.
- Regular meeting scheduler creates monthly defaults without duplication.
- Capacity enforcement works.
- Backend build/tests pass.
- Frontend build passes.
- MVP_CHECKLIST.md Phase 7 items are updated.

Do not stop after planning. After making a plan, immediately start implementing it.
Do not ask for confirmation for routine implementation choices already covered by the documents.
If a detail is missing but a safe default is defined in AGENTS.md, PRD, TSD, OPS, or DESIGN documents, use that default and continue.
If the missing detail is not safety-critical, security-critical, billing-related, or product-critical, add a TODO and continue.
If a command fails, inspect the error and attempt a fix up to 3 times before stopping.
Stop only if secrets, external account credentials, paid service setup, destructive actions, or uncovered product decisions are required.
After finishing this phase, update MVP_CHECKLIST.md and provide a final summary with changed files, commands run, test results, and remaining TODOs.
```

---

## 11. Phase 8 Goal — Meeting Reviews & Images

```text
/goal

Continue implementing the Growth Archive MVP.

Implement Phase 8 only: Meeting Reviews & Images.

Before doing any work, read these files in order:

1. AGENTS.md
2. CODEX_MVP_BUILD_PROMPT.md
3. MVP_CHECKLIST.md
4. docs/IMPLEMENTATION_PLAN.md
5. docs/prd/PRD_003_IA_User_Flows_Screen_Requirements.md
6. docs/prd/PRD_004_Core_Feature_Requirements.md
7. docs/design/DESIGN_001_Design_System.md
8. docs/tech/TSD_001_Architecture_DB_ERD.md
9. docs/tech/TSD_002_API_Specification.md

Goal:
Implement meeting reviews, image upload, review gallery, and review management.

Scope:
- Implement image upload API using storage abstraction.
- MVP image storage provider: Supabase Storage.
- Keep storage interface replaceable for future mounted disk/self-hosted storage.
- Single image max size: 10MB.
- Meeting review images max count: 10.
- Server should support WebP conversion/resizing according to TSD/DESIGN.
- Original image storage policy should follow TSD.
- Implement meeting review create/edit/delete.
- Active Member can write a review even if they did not click attendance in the system.
- Review can be associated with a meeting.
- Review photos are public when review is public.
- Show upload warning:
  - Uploaded photos may be visible in public meeting reviews.
  - Please confirm public sharing with people in the photos.
- Implement meeting review list page.
- Implement meeting review detail page.
- Implement review photo gallery for mobile.
- Admin cannot rewrite review content.
- Admin can hide/delete/restore reviews and images if documented.
- Update recent growth record when review is actually created.
- Update MVP_CHECKLIST.md for Phase 8.

Important constraints:
- Max review images = 10.
- Do not allow comments/likes.
- Do not create chat or notification features.
- Use real-photo-first design.
- Admin does not directly edit member review content.

Done when:
- Member can write meeting review with up to 10 images.
- Guest can view public meeting reviews and gallery.
- Member can edit/delete own review.
- Admin can hide/delete according to management policy.
- Image upload is safe and documented.
- Backend build/tests pass.
- Frontend build passes.
- MVP_CHECKLIST.md Phase 8 items are updated.

Do not stop after planning. After making a plan, immediately start implementing it.
Do not ask for confirmation for routine implementation choices already covered by the documents.
If a detail is missing but a safe default is defined in AGENTS.md, PRD, TSD, OPS, or DESIGN documents, use that default and continue.
If the missing detail is not safety-critical, security-critical, billing-related, or product-critical, add a TODO and continue.
If a command fails, inspect the error and attempt a fix up to 3 times before stopping.
Stop only if secrets, external account credentials, paid service setup, destructive actions, or uncovered product decisions are required.
After finishing this phase, update MVP_CHECKLIST.md and provide a final summary with changed files, commands run, test results, and remaining TODOs.
```

---

## 12. Phase 9 Goal — Admin Console

```text
/goal

Continue implementing the Growth Archive MVP.

Implement Phase 9 only: Admin Console.

Before doing any work, read these files in order:

1. AGENTS.md
2. CODEX_MVP_BUILD_PROMPT.md
3. MVP_CHECKLIST.md
4. docs/IMPLEMENTATION_PLAN.md
5. docs/prd/PRD_002_Users_Auth_Permissions_Onboarding.md
6. docs/prd/PRD_004_Core_Feature_Requirements.md
7. docs/tech/TSD_001_Architecture_DB_ERD.md
8. docs/tech/TSD_002_API_Specification.md
9. docs/ops/OPS_001_Data_Migration_Admin_Release.md

Goal:
Implement the MVP Admin Console.

Scope:
- Implement Admin route protection.
- Implement Admin dashboard.
- Implement member management:
  - list members
  - view member summary
  - deactivate member
  - reactivate member
  - adjust participation start month
  - role management only if documented and safe
- Implement invite code management:
  - only one active invite code
  - changing code invalidates previous active code
- Implement interest tag management:
  - Admin-managed tags
  - Member selects from existing tags only
- Implement recommended books management:
  - 3~5 recommended books target
  - recommendation reason
  - display order
  - active period if documented
- Implement regular meeting management:
  - Admin can modify regular meeting operational fields
  - Admin cannot directly edit small meeting content
  - Admin can hide/delete meetings according to policy
- Implement participation management:
  - monthly completed/needed list
  - coffee support target for 투썸 아메리카노 1잔
  - CSV export
  - admin memo if documented
- Implement content moderation:
  - reading records hide/delete/restore
  - reviews hide/delete/restore
  - books verification if documented
- Implement admin audit logs if documented.
- Update MVP_CHECKLIST.md for Phase 9.

Important constraints:
- All Admins have same authority in MVP.
- Admin is role ADMIN + active member.
- Admin does not directly edit member-created content body.
- Admin can manage visibility, deletion, operational settings, and moderation.
- No payment/settlement management in MVP.

Done when:
- Admin can perform core operational tasks.
- Non-admin cannot access Admin pages or APIs.
- Invite code replacement works with one active code.
- Participation CSV export works.
- Admin moderation actions work.
- Backend build/tests pass.
- Frontend build passes.
- MVP_CHECKLIST.md Phase 9 items are updated.

Do not stop after planning. After making a plan, immediately start implementing it.
Do not ask for confirmation for routine implementation choices already covered by the documents.
If a detail is missing but a safe default is defined in AGENTS.md, PRD, TSD, OPS, or DESIGN documents, use that default and continue.
If the missing detail is not safety-critical, security-critical, billing-related, or product-critical, add a TODO and continue.
If a command fails, inspect the error and attempt a fix up to 3 times before stopping.
Stop only if secrets, external account credentials, paid service setup, destructive actions, or uncovered product decisions are required.
After finishing this phase, update MVP_CHECKLIST.md and provide a final summary with changed files, commands run, test results, and remaining TODOs.
```

---

## 13. Phase 10 Goal — Data Migration Tools

```text
/goal

Continue implementing the Growth Archive MVP.

Implement Phase 10 only: Data Migration Tools.

Before doing any work, read these files in order:

1. AGENTS.md
2. CODEX_MVP_BUILD_PROMPT.md
3. MVP_CHECKLIST.md
4. docs/IMPLEMENTATION_PLAN.md
5. docs/ops/OPS_001_Data_Migration_Admin_Release.md
6. docs/prd/PRD_004_Core_Feature_Requirements.md
7. docs/tech/TSD_001_Architecture_DB_ERD.md
8. docs/tech/TSD_002_API_Specification.md

Goal:
Implement data migration support tools for existing Somoim/Notion/blog records.

Scope:
- Create CSV import templates and documentation.
- Implement migration scripts or Admin import endpoints according to OPS_001.
- Support importing existing reading records.
- Existing reading records are ACTIVE when author is mapped/onboarded.
- Existing records with unmapped author remain HIDDEN.
- Preserve personal blog URL for reading records.
- Do not preserve old Somoim/Notion original links.
- Support importing meeting reviews.
- Existing meeting review photos must be selected by Admin, max 10 per review.
- Support importing signup introductions.
- Signup introductions are not Guest-public.
- Signup introduction data can be used as internal/member profile source according to OPS.
- Implement import status separation if documented:
  - import_status
  - content_status
- Add import logs or reports.
- Update MVP_CHECKLIST.md for Phase 10.

Important constraints:
- No old Somoim/Notion original link preservation.
- Blog URLs in reading records must be preserved.
- Do not publish unmapped author records.
- Do not auto-publish all meeting photos.
- Do not import comments or KakaoTalk chat logs in MVP unless explicitly documented.

Done when:
- CSV templates exist.
- Import tool/script exists.
- Import dry-run or validation mode exists if feasible.
- Existing records can be mapped to members.
- Unmapped records stay hidden.
- Backend build/tests pass.
- MVP_CHECKLIST.md Phase 10 items are updated.

Do not stop after planning. After making a plan, immediately start implementing it.
Do not ask for confirmation for routine implementation choices already covered by the documents.
If a detail is missing but a safe default is defined in AGENTS.md, PRD, TSD, OPS, or DESIGN documents, use that default and continue.
If the missing detail is not safety-critical, security-critical, billing-related, or product-critical, add a TODO and continue.
If a command fails, inspect the error and attempt a fix up to 3 times before stopping.
Stop only if secrets, external account credentials, paid service setup, destructive actions, or uncovered product decisions are required.
After finishing this phase, update MVP_CHECKLIST.md and provide a final summary with changed files, commands run, test results, and remaining TODOs.
```

---

## 14. Phase 11 Goal — QA, Security, Release Readiness

```text
/goal

Continue implementing the Growth Archive MVP.

Implement Phase 11 only: QA, Security, Release Readiness.

Before doing any work, read these files in order:

1. AGENTS.md
2. CODEX_MVP_BUILD_PROMPT.md
3. MVP_CHECKLIST.md
4. docs/IMPLEMENTATION_PLAN.md
5. docs/ops/OPS_001_Data_Migration_Admin_Release.md
6. docs/tech/TSD_001_Architecture_DB_ERD.md
7. docs/tech/TSD_002_API_Specification.md
8. all documents under docs/prd/
9. docs/design/DESIGN_001_Design_System.md

Goal:
Finalize MVP readiness through tests, security checks, documentation, and release checklist.

Scope:
- Review all MVP features against MVP_CHECKLIST.md.
- Add missing tests for critical flows:
  - auth/access control
  - invite code
  - onboarding
  - member access calculation from timestamp fields
  - reading record creation
  - UNVERIFIED book creation
  - monthly participation calculation
  - new member participation start month
  - regular meeting scheduler idempotency
  - soft delete exclusion from participation calculation
  - meeting attendance capacity
  - Guest safe meeting visibility
  - review image limit 10
  - Admin cannot rewrite member-owned content
- Run backend tests.
- Run frontend tests/build/lint if configured.
- Run type checks.
- Validate Docker Compose local setup.
- Validate health checks.
- Validate API Envelope consistency.
- Check security basics:
  - JWT not in localStorage
  - HttpOnly cookie
  - SameSite policy
  - Origin/Referer validation for unsafe methods if implemented
  - no secrets committed
- Update README.md.
- Update .env.example if needed.
- Update MVP_CHECKLIST.md.
- Create RELEASE_NOTES.md if appropriate.

Important constraints:
- Do not add new product features during QA.
- Fix bugs and missing MVP requirements only.
- Do not broaden scope into Phase 2 features.
- Do not add likes/comments/ranking/points/chat/notifications/payment.

Done when:
- MVP_CHECKLIST.md is complete or explicitly marked with remaining TODOs.
- Backend tests/build pass.
- Frontend tests/build pass.
- Docker Compose setup is documented and tested as far as possible.
- README.md has final local run instructions.
- Final summary lists:
  - completed features
  - incomplete items
  - commands run
  - test results
  - release risks
  - next recommended actions

Do not stop after planning. After making a plan, immediately start implementing it.
Do not ask for confirmation for routine implementation choices already covered by the documents.
If a detail is missing but a safe default is defined in AGENTS.md, PRD, TSD, OPS, or DESIGN documents, use that default and continue.
If the missing detail is not safety-critical, security-critical, billing-related, or product-critical, add a TODO and continue.
If a command fails, inspect the error and attempt a fix up to 3 times before stopping.
Stop only if secrets, external account credentials, paid service setup, destructive actions, or uncovered product decisions are required.
After finishing this phase, update MVP_CHECKLIST.md and provide a final summary with changed files, commands run, test results, and remaining TODOs.
```

---

## 15. 전체 MVP Goal — 권장하지 않지만 필요할 때 사용

아래 프롬프트는 전체 MVP를 한 번에 진행시키고 싶을 때 사용한다.  
다만 권장 방식은 Phase별 실행이다.

```text
/goal

Implement the Growth Archive MVP phase by phase.

Before coding, read:

1. AGENTS.md
2. CODEX_MVP_BUILD_PROMPT.md
3. MVP_CHECKLIST.md
4. docs/IMPLEMENTATION_PLAN.md
5. all documents under docs/prd/
6. docs/design/DESIGN_001_Design_System.md
7. all documents under docs/tech/
8. docs/ops/OPS_001_Data_Migration_Admin_Release.md

Primary goal:
Implement the MVP according to the documentation.

Execution order:
Follow docs/IMPLEMENTATION_PLAN.md from Phase 0 through Phase 11.
Do not skip phases.

After each phase:
1. Run relevant build/test/lint commands.
2. Fix failures up to 3 times.
3. Update MVP_CHECKLIST.md.
4. Add progress notes.
5. Continue to the next phase unless a true blocker is reached.

Non-negotiable rules:
- Do not add features outside MVP scope.
- Do not create account_status enum.
- Use role + timestamp fields for member lifecycle.
- Use JWT + HttpOnly Secure Cookie.
- Never store JWT in localStorage.
- Use Kakao Book Search API.
- If book search returns no result, allow Member to create UNVERIFIED book.
- Meeting review images are limited to 10.
- Guest must not see exact meeting location.
- Guest may see tiny non-clickable attendee profile image previews.
- Admin must not directly edit member-created content body.
- No likes, comments, ranking, points, chat, notifications, or payment features.
- Backend must be stateless and Kubernetes-ready.
- Local development must work with Docker Compose.

Stop only when:
- The full MVP checklist is complete, or
- A secret/credential is required, or
- A product decision not covered by docs is required, or
- A destructive action is needed, or
- A command fails after 3 serious fix attempts.

Done when:
- Backend builds and tests pass.
- Frontend builds and tests pass.
- Docker Compose local setup is documented.
- MVP_CHECKLIST.md is updated.
- README.md is updated.
- Final summary includes:
  - completed phases
  - changed files
  - commands run
  - test results
  - known TODOs
  - blocked items

Do not stop after planning. After making a plan, immediately start implementing it.
Do not ask for confirmation for routine implementation choices already covered by the documents.
If a detail is missing but a safe default is defined in AGENTS.md, PRD, TSD, OPS, or DESIGN documents, use that default and continue.
If the missing detail is not safety-critical, security-critical, billing-related, or product-critical, add a TODO and continue.
```

---

## 16. Phase 실행 후 검토용 프롬프트

각 Phase 완료 후, 별도로 Codex에게 아래 검토를 시킬 수 있다.

```text
Review the last implementation phase against:

1. AGENTS.md
2. MVP_CHECKLIST.md
3. docs/IMPLEMENTATION_PLAN.md
4. related PRD/TSD/DESIGN/OPS documents for this phase

Check:
- Did the implementation follow the documented MVP scope?
- Did it add any forbidden features?
- Are there security issues?
- Are access controls correct?
- Are API responses using the common Envelope format?
- Are tests missing for critical flows?
- Is MVP_CHECKLIST.md accurately updated?
- Are there TODOs that should block the next phase?

Provide:
1. Pass/fail summary
2. Critical issues
3. Recommended fixes
4. Whether it is safe to proceed to the next phase
```

---

## 17. 추천 운영 방식

```text
Phase 실행
↓
검토 프롬프트 실행
↓
수정 필요하면 같은 Phase에서 수정
↓
테스트 통과
↓
git commit
↓
다음 Phase
```

가장 중요한 원칙은 하나다.

```text
Phase 안에서는 멈추지 않게 하고,
Phase 사이에서는 사람이 확인한다.
```

Growth Archive는 한 번에 세워지는 건물이 아니라,  
기록을 쌓듯 한 층씩 올라가는 서비스다.
