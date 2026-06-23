# MVP_CHECKLIST.md

Project: **부자습관 만들기 - Growth Archive**  
Purpose: MVP completion checklist for Noah, Codex, and future developers  
Status: LIVING  
Last Updated: 2026-06-23

---

## 0. How to Use This Checklist

Codex must update this checklist after each implementation phase.

Use these marks:

```text
[ ] Not started
[-] In progress
[x] Done
[!] Blocked / needs decision
```

Do not mark an item as done unless it is implemented and verified or explicitly accepted by Noah.

---

## 1. Documentation Package

- [x] `AGENTS.md` exists in repository root
- [x] `docs/IMPLEMENTATION_PLAN.md` exists
- [x] `MVP_CHECKLIST.md` exists in repository root
- [x] `CODEX_MVP_BUILD_PROMPT.md` exists in repository root
- [x] `CODEX_PHASE_GOALS.md` exists when phase prompts are used
- [x] `docs/prd/PRD_001_Growth_Archive.md` exists
- [x] `docs/prd/PRD_002_Users_Auth_Permissions_Onboarding.md` exists
- [x] `docs/prd/PRD_003_IA_User_Flows_Screen_Requirements.md` exists
- [x] `docs/prd/PRD_004_Core_Feature_Requirements.md` exists
- [x] `docs/design/DESIGN_001_Design_System.md` exists
- [x] `docs/tech/TSD_001_Architecture_DB_ERD.md` exists
- [x] `docs/tech/TSD_002_API_Specification.md` exists
- [x] `docs/ops/OPS_001_Data_Migration_Admin_Release.md` exists
- [x] `README.md` explains local development
- [x] `.env.example` exists and contains no real secrets

---

## 2. Repository Bootstrap

- [x] Backend project is created under `backend/`
- [x] Frontend project is created under `frontend/`
- [x] Root `docker-compose.yml` exists
- [x] Backend Dockerfile exists
- [x] Frontend Dockerfile exists
- [x] Backend has health check endpoint
- [x] Frontend can be containerized
- [x] Local development can run with Docker Compose
- [x] Project structure follows `AGENTS.md`
- [x] No real secrets are committed

---

## 3. Backend Foundation

- [x] Java 25 is configured
- [x] Spring Boot 4.1.0 is configured
- [x] Spring Security is configured
- [x] Spring Data JPA is configured
- [x] Querydsl is configured
- [x] Flyway is configured
- [x] PostgreSQL connection is configured
- [x] Hibernate `ddl-auto` is not used for schema creation in production
- [x] Common API envelope is implemented
- [x] Common error response is implemented
- [ ] Global exception handler is implemented
- [x] Bean Validation is enabled
- [ ] Pagination convention is implemented

---

## 4. Database / ERD / Migration

- [x] `members` table exists
- [x] No `account_status` enum is used for members
- [x] `role` supports `MEMBER` and `ADMIN`
- [x] `invite_verified_at` exists
- [x] `terms_agreed_at` exists
- [x] `privacy_agreed_at` exists
- [x] `onboarding_completed_at` exists
- [x] `deactivated_at` exists
- [x] `oauth_accounts` table exists
- [x] `interest_tags` table exists
- [x] `member_interest_tags` table exists
- [x] `member_join_intro_sources` or equivalent internal migration table exists for signup greetings
- [x] `books` table exists
- [x] Manual books can be marked `UNVERIFIED`
- [x] `reading_records` table exists
- [x] `reading_records.recorded_at` exists and is used for monthly participation
- [x] `monthly_action_plans` table exists
- [x] `monthly_reflections` table exists
- [x] `meetings` table exists
- [x] `meeting_attendances` table exists
- [x] `meeting_reviews` table exists
- [x] `meeting_review_images` table exists or equivalent image relation exists
- [x] `recommended_books` table exists
- [x] `activity_events` table exists
- [x] `participation_admin_notes` table exists
- [x] `admin_audit_logs` table exists
- [x] Soft delete fields are applied where required
- [x] Required indexes are added for major list/search queries

---

## 5. Authentication / Onboarding

- [ ] Kakao OAuth login endpoint exists
- [ ] Kakao OAuth callback endpoint exists
- [ ] JWT + HttpOnly Cookie auth is implemented
- [ ] Refresh token handling is implemented or safely documented as TODO
- [ ] Logout clears auth cookies
- [ ] `/api/v1/auth/me` returns current user state
- [ ] Invite code verification endpoint exists
- [ ] Only one active invite code is supported
- [ ] Admin can change active invite code
- [ ] Terms agreement endpoint exists
- [ ] Privacy agreement timestamp is stored
- [ ] Onboarding endpoint exists
- [ ] Nickname duplication is blocked
- [ ] Onboarding required fields are validated
- [ ] Member access is computed from timestamp fields
- [ ] Deactivated members cannot use member-only APIs
- [ ] Admin access requires active member + `role = ADMIN`

---

## 6. Frontend App Shell / Navigation

- [ ] Next.js app is created
- [ ] TypeScript is configured
- [ ] Tailwind CSS is configured
- [ ] shadcn/ui or component base is configured
- [ ] Design tokens from DESIGN-001 are applied
- [ ] PC header navigation exists
- [ ] Mobile bottom navigation exists
- [ ] Mobile hamburger menu exists
- [ ] Guest routes render correctly
- [ ] Member-only route guard exists
- [ ] Admin route guard exists
- [ ] Mobile WebView safe-area is considered
- [ ] Loading state components exist
- [ ] Empty state components exist
- [ ] Error state components exist

---

## 7. Home / Public Pages

- [ ] Home Hero renders brand message
- [ ] Real-photo-first visual direction is reflected
- [ ] This month recommended books section renders
- [ ] Recent growth records section renders
- [ ] Popular books TOP5 section renders
- [ ] Recent meetings section renders
- [ ] Recent meeting reviews section renders
- [ ] Growth people preview section renders
- [ ] Guest can browse public content without login
- [ ] Login CTA is visible where required

---

## 8. Reading Library

- [ ] `/library` page exists
- [ ] Kakao Book Search integration works
- [ ] Book search result list renders
- [ ] Book detail page exists
- [ ] Book detail shows average rating, record count, readers
- [ ] “이 책을 읽은 사람” section exists
- [ ] Reading records are displayed by author accordion/toggle
- [ ] Recent reading records list exists
- [ ] Popular books TOP5 calculation works
- [ ] Recommended books 3~5 can be displayed
- [ ] Book search no-result state exists
- [ ] Member can manually register an `UNVERIFIED` book

---

## 9. Reading Records

- [ ] Member can create reading record
- [ ] Book selection is required
- [ ] Rating is optional
- [ ] Rating accepts integer 1~5 only when provided
- [ ] One-line review validation is implemented
- [ ] Blog URL validation is implemented
- [ ] Blog URL is visible to Guest
- [ ] Representative image can be uploaded
- [ ] Author can edit own reading record
- [ ] Author can delete own reading record
- [ ] Admin cannot edit reading record content
- [ ] Admin can hide reading record
- [ ] Admin can delete/soft-delete reading record
- [ ] Deleted reading records do not count toward participation

---

## 10. Growth People / Profiles

- [ ] `/people` page exists
- [ ] Growth people page is card/showcase style, not plain table
- [ ] Profile URL uses `/people/{memberId}`
- [ ] Guest sees public profile fields only
- [ ] Member sees deeper profile fields
- [ ] Job is not visible to Guest
- [ ] Age is not collected
- [ ] 50살의 나 is prominently displayed
- [ ] Growth statistics are displayed
- [ ] Recent reading records are limited to 3 items
- [ ] Recent action plans are limited to 3 items for Member view
- [ ] Profile image fallback works: upload → Kakao → default

---

## 11. My Page / Dashboard

- [ ] `/mypage` exists
- [ ] My page requires Member access
- [ ] `GET /me/dashboard` or equivalent data loading exists
- [ ] Monthly participation status is shown at top
- [ ] Quick CTA for reading record exists
- [ ] Quick CTA for action plan exists
- [ ] Quick CTA for reflection exists
- [ ] Quick CTA for small meeting exists
- [ ] Recent personal records are shown
- [ ] Profile edit page exists

---

## 12. Monthly Action Plan

- [ ] Member can create monthly action plan
- [ ] One monthly action plan per member per month is enforced
- [ ] Action plan is free-form text
- [ ] Author can edit own action plan
- [ ] Author can delete own action plan
- [ ] Admin cannot edit action plan content
- [ ] Action plan is Member-only
- [ ] Action plan counts toward monthly participation
- [ ] Deleted action plan does not count toward participation

---

## 13. Monthly Reflection

- [ ] Monthly reflection page exists
- [ ] Reflection is optional
- [ ] Reflection does not count toward participation
- [ ] Reflection slot/entry is available by month
- [ ] Actual activity event is created only after content is saved
- [ ] Author can edit own reflection
- [ ] Reflection is Member-only
- [ ] Admin cannot rewrite reflection content

---

## 14. Monthly Participation

- [ ] Participation rule is implemented: reading record >= 1 OR action plan >= 1
- [ ] Participation is calculated by month
- [ ] New member participation starts from defined start month
- [ ] Deleted records are excluded
- [ ] Member sees completed / needs participation state
- [ ] Coffee support target is shown when incomplete
- [ ] Coffee support unit is 투썸 아메리카노 1잔
- [ ] Admin can view monthly completion summary
- [ ] Admin can view non-participant list
- [ ] Admin can download non-participant CSV
- [ ] Admin can write operation memo for participation notes
- [ ] Admin can adjust member participation start month

---

## 15. Meetings / Small Meetings

- [ ] `/meetings` page exists
- [ ] Regular monthly reading meeting is auto-created
- [ ] Regular monthly action meeting is auto-created
- [ ] Scheduler runs at monthly 1st 00:10 KST or equivalent job exists
- [ ] Scheduler is idempotent
- [ ] Admin can edit regular meeting operation fields
- [ ] Member can create small meeting
- [ ] Small meeting creator can edit own small meeting
- [ ] Admin cannot edit small meeting content
- [ ] Admin can hide/delete small meeting
- [ ] Member can attend meeting
- [ ] Member can cancel attendance
- [ ] Meeting capacity is enforced if capacity > 0
- [ ] capacity null/0 means unlimited
- [ ] Guest sees region-level location only
- [ ] Member sees exact location
- [ ] Guest sees tiny attendee avatars only, no names/profile links
- [ ] Member sees attendee names/profile links

---

## 16. Meeting Reviews / Images

- [ ] `/reviews` page exists
- [ ] Review detail page exists
- [ ] Active Member can write review without attendance restriction
- [ ] Review is linked to a meeting when possible
- [ ] Review title validation is implemented
- [ ] Review content validation is implemented
- [ ] Review image upload supports max 10 photos
- [ ] Single image max size is 10MB
- [ ] Uploaded images are converted/resized to WebP or equivalent optimized format
- [ ] Original image is not permanently stored unless explicitly required
- [ ] Photo public visibility warning is shown
- [ ] Admin can hide review
- [ ] Admin can delete/soft-delete review
- [ ] Admin cannot rewrite review content

---

## 17. Admin Console

- [ ] Admin dashboard exists
- [ ] Admin member list exists
- [ ] Admin can deactivate member
- [ ] Admin can reactivate member
- [ ] Admin can change active invite code
- [ ] Admin can manage interest tags
- [ ] Admin can manage recommended books
- [ ] Admin can manage regular meetings
- [ ] Admin can hide/delete member content where allowed
- [ ] Admin can view participation status
- [ ] Admin can export non-participants CSV
- [ ] Admin actions are recorded in audit log where practical

---

## 18. Data Migration / Operations

- [ ] CSV/Google Sheet import template is documented
- [ ] Reading record import supports full target import
- [ ] Existing mapped reading records can be ACTIVE
- [ ] Unmapped records remain HIDDEN
- [ ] Existing meeting review photos are manually selected max 10
- [ ] Existing signup greetings are imported as internal/member-admin data
- [ ] Existing Somoim/Notion original links are not preserved
- [ ] Blog URLs are preserved
- [ ] Import failures are logged
- [ ] Admin can review imported data or import output is documented

---

## 19. Security / Privacy

- [ ] No real secrets in repository
- [ ] `.env.example` documents required variables
- [ ] JWT cookies are HttpOnly
- [ ] Production cookies are Secure
- [ ] SameSite policy is configured
- [ ] CORS allowed origins are restricted
- [ ] Unsafe methods validate Origin/Referer or equivalent CSRF mitigation
- [ ] Guest cannot access member-only APIs
- [ ] Deactivated users are blocked from member-only APIs
- [ ] Admin APIs require active Admin access
- [ ] Guest cannot see exact meeting location
- [ ] Guest cannot see job field
- [ ] Guest cannot see private profile fields

---

## 20. Design / UX Acceptance

- [ ] Uses Quiet Luxury Archive palette
- [ ] Real-photo-first direction is visible
- [ ] UI does not look like a generic bulletin board
- [ ] UI does not use excessive emoji
- [ ] Member cards feel like premium profile showcase
- [ ] Home page feels premium/editorial/serious
- [ ] Mobile UI is usable with one hand where practical
- [ ] Primary CTA is clear on key pages
- [ ] Empty states guide users gently

---

## 21. QA / Release Readiness

- [ ] Backend tests pass
- [ ] Frontend build passes
- [ ] Lint/type checks pass if configured
- [ ] Docker Compose up works locally
- [ ] Health check works
- [ ] Auth flow is tested
- [ ] Invite code validation is tested
- [ ] Onboarding flow is tested
- [ ] Member access calculation from timestamp fields is tested
- [ ] Reading record flow is tested
- [ ] Manual `UNVERIFIED` book creation is tested
- [ ] Monthly participation calculation is tested
- [ ] New member participation start month is tested
- [ ] Soft delete exclusion from participation calculation is tested
- [ ] Regular meeting scheduler idempotency is tested
- [ ] Meeting attendance flow is tested
- [ ] Meeting capacity enforcement is tested
- [ ] Review image limit is tested
- [ ] Admin access control is tested
- [ ] Admin cannot rewrite member-owned content is tested
- [ ] Mobile responsive behavior is checked
- [ ] Release notes are prepared
- [ ] Known limitations are documented

---

## 22. MVP Final Acceptance

- [ ] Noah can log in with Kakao
- [ ] Noah can verify invite code
- [ ] Noah can complete onboarding
- [ ] Noah can create a reading record
- [ ] Noah can create a monthly action plan
- [ ] My page shows monthly participation status correctly
- [ ] Guest can browse reading library
- [ ] Guest can view public profile card/detail within allowed scope
- [ ] Member can view member-only profile details
- [ ] Member can attend a meeting
- [ ] Member can create a small meeting
- [ ] Member can write a meeting review with up to 10 images
- [ ] Admin can manage invite code
- [ ] Admin can manage recommended books
- [ ] Admin can view non-participants
- [ ] Admin can hide/delete inappropriate content
- [ ] The service feels like a serious premium growth archive

---

## 23. Notes for Codex

After each phase, update relevant sections of this checklist.

When a requirement is not implemented because it is blocked or ambiguous, mark it as `[!]` and add a short reason.

Do not mark checklist items as done only because code was generated. Mark done after build/test/manual verification or clear implementation proof.
