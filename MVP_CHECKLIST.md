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
- [x] Global exception handler is implemented
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

- [x] Kakao OAuth login endpoint exists
- [x] Kakao OAuth callback endpoint exists
- [x] JWT + HttpOnly Cookie auth is implemented
- [x] Refresh token handling is implemented or safely documented as TODO
- [x] Logout clears auth cookies
- [x] `/api/v1/auth/me` returns current user state
- [x] Invite code verification endpoint exists
- [x] Only one active invite code is supported
- [x] Admin can change active invite code
- [x] Terms agreement endpoint exists
- [x] Privacy agreement timestamp is stored
- [x] Onboarding endpoint exists
- [x] Nickname duplication is blocked
- [x] Onboarding required fields are validated
- [x] Member access is computed from timestamp fields
- [x] Deactivated members cannot use member-only APIs
- [x] Admin access requires active member + `role = ADMIN`

---

## 6. Frontend App Shell / Navigation

- [x] Next.js app is created
- [x] TypeScript is configured
- [x] Tailwind CSS is configured
- [x] shadcn/ui or component base is configured
- [x] Design tokens from DESIGN-001 are applied
- [x] PC header navigation exists
- [x] Mobile bottom navigation exists
- [x] Mobile hamburger menu exists
- [x] Guest routes render correctly
- [x] Member-only route guard exists
- [x] Admin route guard exists
- [x] Mobile WebView safe-area is considered
- [x] Loading state components exist
- [x] Empty state components exist
- [x] Error state components exist

---

## 7. Home / Public Pages

- [x] Home Hero renders brand message
- [ ] Real-photo-first visual direction is reflected
- [ ] This month recommended books section renders
- [ ] Recent growth records section renders
- [ ] Popular books TOP5 section renders
- [ ] Recent meetings section renders
- [ ] Recent meeting reviews section renders
- [ ] Growth people preview section renders
- [x] Guest can browse public content without login
- [x] Login CTA is visible where required

---

## 8. Reading Library

- [x] `/library` page exists
- [x] Kakao Book Search integration works
- [x] Book search result list renders
- [x] Book detail page exists
- [x] Book detail shows average rating, record count, readers
- [ ] “이 책을 읽은 사람” section exists
- [x] Reading records are displayed by author accordion/toggle
- [x] Recent reading records list exists
- [x] Popular books TOP5 calculation works
- [x] Recommended books 3~5 can be displayed
- [x] Book search no-result state exists
- [x] Member can manually register an `UNVERIFIED` book

---

## 9. Reading Records

- [x] Member can create reading record
- [x] Book selection is required
- [x] Rating is optional
- [x] Rating accepts integer 1~5 only when provided
- [x] One-line review validation is implemented
- [x] Blog URL validation is implemented
- [x] Blog URL is visible to Guest
- [ ] Representative image can be uploaded
- [x] Author can edit own reading record
- [x] Author can delete own reading record
- [x] Admin cannot edit reading record content
- [x] Admin can hide reading record
- [x] Admin can delete/soft-delete reading record
- [ ] Deleted reading records do not count toward participation

---

## 10. Growth People / Profiles

- [x] `/people` page exists
- [x] Growth people page is card/showcase style, not plain table
- [x] Profile URL uses `/people/{memberId}`
- [x] Guest sees public profile fields only
- [x] Member sees deeper profile fields
- [x] Job is not visible to Guest
- [x] Age is not collected
- [x] 50살의 나 is prominently displayed
- [x] Growth statistics are displayed
- [x] Recent reading records are limited to 3 items
- [x] Recent action plans are limited to 3 items for Member view
- [ ] Profile image fallback works: upload → Kakao → default

---

## 11. My Page / Dashboard

- [x] `/mypage` exists
- [x] My page requires Member access
- [x] `GET /me/dashboard` or equivalent data loading exists
- [x] Monthly participation status is shown at top
- [x] Quick CTA for reading record exists
- [x] Quick CTA for action plan exists
- [x] Quick CTA for reflection exists
- [x] Quick CTA for small meeting exists
- [x] Recent personal records are shown
- [x] Profile edit page exists

---

## 12. Monthly Action Plan

- [x] Member can create monthly action plan
- [x] One monthly action plan per member per month is enforced
- [x] Action plan is free-form text
- [x] Author can edit own action plan
- [x] Author can delete own action plan
- [x] Admin cannot edit action plan content
- [x] Action plan is Member-only
- [x] Action plan counts toward monthly participation
- [x] Deleted action plan does not count toward participation

---

## 13. Monthly Reflection

- [x] Monthly reflection page exists
- [x] Reflection is optional
- [x] Reflection does not count toward participation
- [x] Reflection slot/entry is available by month
- [ ] Actual activity event is created only after content is saved
- [x] Author can edit own reflection
- [x] Reflection is Member-only
- [x] Admin cannot rewrite reflection content

---

## 14. Monthly Participation

- [x] Participation rule is implemented: reading record >= 1 OR action plan >= 1
- [x] Participation is calculated by month
- [x] New member participation starts from defined start month
- [x] Deleted records are excluded
- [x] Member sees completed / needs participation state
- [x] Coffee support target is shown when incomplete
- [x] Coffee support unit is 투썸 아메리카노 1잔
- [x] Admin can view monthly completion summary
- [x] Admin can view non-participant list
- [x] Admin can download non-participant CSV
- [x] Admin can write operation memo for participation notes
- [ ] Admin can adjust member participation start month

---

## 15. Meetings / Small Meetings

- [x] `/meetings` page exists
- [x] Regular monthly reading meeting is auto-created
- [x] Regular monthly action meeting is auto-created
- [x] Scheduler runs at monthly 1st 00:10 KST or equivalent job exists
- [x] Scheduler is idempotent
- [x] Admin can edit regular meeting operation fields
- [x] Member can create small meeting
- [x] Small meeting creator can edit own small meeting
- [x] Admin cannot edit small meeting content
- [x] Admin can hide/delete small meeting
- [x] Member can attend meeting
- [x] Member can cancel attendance
- [x] Meeting capacity is enforced if capacity > 0
- [x] capacity null/0 means unlimited
- [x] Guest sees region-level location only
- [x] Member sees exact location
- [x] Guest sees tiny attendee avatars only, no names/profile links
- [x] Member sees attendee names/profile links

---

## 16. Meeting Reviews / Images

- [x] `/reviews` page exists
- [x] Review detail page exists
- [x] Active Member can write review without attendance restriction
- [x] Review is linked to a meeting when possible
- [x] Review title validation is implemented
- [x] Review content validation is implemented
- [x] Review image upload supports max 10 photos
- [x] Single image max size is 10MB
- [x] Uploaded images are converted/resized to WebP or equivalent optimized format
- [x] Original image is not permanently stored unless explicitly required
- [x] Photo public visibility warning is shown
- [x] Admin can hide review
- [x] Admin can delete/soft-delete review
- [x] Admin cannot rewrite review content

---

## 17. Admin Console

- [x] Admin dashboard exists
- [ ] Admin member list exists
- [ ] Admin can deactivate member
- [ ] Admin can reactivate member
- [ ] Admin can change active invite code
- [ ] Admin can manage interest tags
- [x] Admin can manage recommended books
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

- [x] Backend tests pass
- [x] Frontend build passes
- [x] Lint/type checks pass if configured
- [x] Docker Compose up works locally
- [x] Health check works
- [ ] Auth flow is tested
- [ ] Invite code validation is tested
- [ ] Onboarding flow is tested
- [ ] Member access calculation from timestamp fields is tested
- [ ] Reading record flow is tested
- [ ] Manual `UNVERIFIED` book creation is tested
- [ ] Monthly participation calculation is tested
- [ ] New member participation start month is tested
- [ ] Soft delete exclusion from participation calculation is tested
- [x] Regular meeting scheduler idempotency is tested
- [x] Meeting attendance flow is tested
- [x] Meeting capacity enforcement is tested
- [ ] Review image limit is tested
- [x] Admin access control is tested
- [x] Admin cannot rewrite member-owned content is tested
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
- [x] Member can write a meeting review with up to 10 images
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
