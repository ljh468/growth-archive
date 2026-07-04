# TSD-002 API Specification

**Project**: 부자습관 만들기 - Growth Archive
**Document Type**: Technical Specification
**Version**: 1.1 FINAL
**Status**: Final
**Last Updated**: 2026-06-29
**Primary Owner**: Noah

**Related Documents**

- `PRD_001_Growth_Archive.md`
- `PRD_002_Users_Auth_Permissions_Onboarding.md`
- `PRD_003_IA_User_Flows_Screen_Requirements.md`
- `PRD_004_Core_Feature_Requirements.md`
- `DESIGN_001_Design_System.md`
- `TSD_001_Architecture_DB_ERD.md`

---

## 0. Purpose

이 문서는 Growth Archive MVP 구현을 위한 백엔드 API 명세서다.

Codex 또는 개발자가 이 문서를 읽고 다음을 구현할 수 있어야 한다.

- Spring Boot REST API Controller
- Request / Response DTO
- Bean Validation
- Authentication / Authorization
- Error Handling
- Pagination
- Image Upload API
- Admin API
- Frontend API 연동 기준

화면 구조는 `PRD-003`, 기능 요구사항은 `PRD-004`, DB 구조는 `TSD-001`을 기준으로 한다.

---

## 1. Final Technical Decisions

### 1.1 Account Management Simplification

MVP에서는 복잡한 `account_status` enum을 두지 않는다.

계정 관리는 아래 필드로 단순하게 관리한다.

```text
role
- MEMBER
- ADMIN

invite_verified_at
- 초대코드 인증 완료 시각

terms_agreed_at
- 이용약관 동의 시각

privacy_agreed_at
- 개인정보처리방침 동의 시각

onboarding_completed_at
- 온보딩 완료 시각

deactivated_at
- null이면 활성 회원
- 값이 있으면 비활성 회원
```

`*_at` suffix는 boolean이 아니라 **TIMESTAMPTZ** 값을 의미한다.

```text
null      = 아직 해당 단계가 완료되지 않음
not null  = 해당 단계가 특정 시각에 완료됨
```

API 권한 판단은 DB enum 상태가 아니라 **계산된 Access Level**로 처리한다.

```text
PUBLIC
- 비로그인 사용자도 접근 가능

AUTHENTICATED
- 카카오 로그인은 완료했으나 온보딩 전일 수 있음

INVITE_VERIFIED
- invite_verified_at is not null
- deactivated_at is null
- 약관 동의와 온보딩은 아직 완료 전일 수 있음

MEMBER
- onboarding_completed_at is not null
- deactivated_at is null

ADMIN
- MEMBER 조건 충족
- role = ADMIN
```

Admin은 계정 상태가 아니라 Role이다.

MVP 이후 `SUSPENDED` 같은 세부 상태가 필요해지면 별도 정책 문서에서 재검토한다.

---

### 1.2 Auth

인증은 다음 방식으로 구현한다.

```text
JWT + HttpOnly Secure Cookie
```

기본 쿠키:

```text
access_token
refresh_token
```

기본 만료 시간:

```text
Access Token: 30분
Refresh Token: 14일
```

프론트엔드는 JWT를 직접 읽거나 localStorage/sessionStorage에 저장하지 않는다.

---

### 1.3 CSRF MVP Policy

JWT를 HttpOnly Cookie로 사용하므로 CSRF 최소 대응을 적용한다.

MVP 정책:

```text
- Cookie SameSite=Lax
- Production Secure Cookie=true
- POST/PUT/PATCH/DELETE 요청에 Origin 또는 Referer 검증
- CORS 허용 Origin 화이트리스트 적용
```

Phase 2에서 CSRF Token 또는 Double Submit Cookie를 검토한다.

---

### 1.4 Image Upload Policy

이미지 정책:

```text
단일 이미지 최대 크기: 10MB
모임 후기 사진 최대 개수: 10장
후기 사진 전체 업로드 권장 총량: 50MB 이하
이미지 저장 형식: 브라우저 우선 WebP 변환/리사이징/압축
원본 이미지: MVP에서는 원본 저장하지 않음
업로드 요청 타임아웃: 프론트 기준 요청당 25초
```

프론트엔드는 여러 장을 한 번에 선택하더라도, 안정성을 위해 단일 이미지 업로드 API를 제한된 병렬 개수로 호출한다.
모임 후기 사진은 2개씩 병렬 업로드하는 것을 기본값으로 한다.
일부 이미지 업로드가 실패해도 성공한 이미지 ID만 후기 생성 요청에 포함하고, 실패 이미지는 수정 화면에서 다시 추가하도록 안내한다.
서버는 클라이언트 최적화를 신뢰하지 않고 10MB, MIME type, purpose 검증과 방어용 최적화를 수행한다.

---

### 1.5 Meeting Capacity Policy

```text
capacity = null 또는 0
- 정원 제한 없음

capacity > 0
- 정원 초과 시 참석 불가
- 대기 기능 MVP 제외
```

---

### 1.6 Delete Policy

MVP 기본 삭제 정책은 Soft Delete다.

```text
사용자 삭제: soft delete
Admin 삭제: soft delete
Hard delete: MVP 제외
```

Soft delete된 데이터는 기본 목록, 통계, 참여 현황 계산에서 제외한다.

예:

```text
삭제된 독서기록은 해당 월 참여 현황 계산에서 제외한다.
삭제된 실행계획은 해당 월 참여 현황 계산에서 제외한다.
```

---

### 1.7 Kakao OAuth Redirect URL

현재 dev 배포는 Vercel 프론트엔드와 Render 백엔드를 사용한다.
Kakao Developers에는 실제 접근 가능한 Redirect URI를 정확히 등록해야 한다.

```text
Local:
http://localhost:8080/api/v1/auth/kakao/callback

Current dev via Vercel proxy:
https://growth-archive.vercel.app/api/v1/auth/kakao/callback

Backend direct callback, only when intentionally using backend domain cookies:
https://growth-archive-api.onrender.com/api/v1/auth/kakao/callback

Production:
https://{FRONTEND_HOST}/api/v1/auth/kakao/callback
```

현재 Vercel 배포는 브라우저 API base를 `/api/v1`로 두고 Next.js rewrite로 Render 백엔드에 전달한다. HttpOnly cookie를 프론트 도메인 기준으로 안정적으로 유지하려면 Kakao Redirect URI도 Vercel proxy URL을 우선 사용한다. 백엔드 직접 콜백을 사용할 경우 쿠키 도메인과 이후 API 호출 경로를 함께 검토해야 한다.

HTTPS와 도메인을 붙이면 `KAKAO_REDIRECT_URI`, `FRONTEND_BASE_URL`, `CORS_ALLOWED_ORIGINS`, Vercel `API_PROXY_TARGET`을 함께 갱신한다.

---

### 1.8 Deployment Assumption for API

MVP 구현 기준:

```text
Docker Compose로 로컬 개발 가능
Local PostgreSQL은 Docker Compose로 실행하고 localhost:5432로 노출
Dev 프론트엔드는 Vercel에서 실행
Dev 백엔드는 Render Free Web Service에서 실행
Dev DB는 Supabase PostgreSQL 사용
Dev 이미지/업로드 저장소는 Supabase Storage 사용
Local 개발은 Docker Compose로 frontend/backend/postgres 실행
```

배포 확장 기준:

```text
Kubernetes-ready
Future self-hosted Kubernetes/k3s에서 disk-backed storage로 전환 가능
```

API는 다음 조건을 만족해야 한다.

```text
- Stateless backend
- env 기반 설정
- health check endpoint 제공
- Dockerfile 제공
- 프론트엔드와 백엔드 모두 컨테이너 배포 가능
```

---

## 2. Common API Rules

### 2.1 Base URL

```text
/api/v1
```

예:

```text
GET /api/v1/books/1
POST /api/v1/reading-records
```

---

### 2.2 Common Response Envelope

모든 API는 공통 응답 구조를 사용한다.

#### Success

```json
{
  "success": true,
  "data": {},
  "message": null
}
```

#### Success with Message

```json
{
  "success": true,
  "data": {},
  "message": "저장되었습니다."
}
```

#### Error

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_INVITE_CODE",
    "message": "초대코드가 올바르지 않습니다.",
    "details": []
  }
}
```

---

### 2.3 List and Pagination Response

현재 구현된 MVP API는 공통 envelope는 동일하게 사용하되, 목록 응답은 두 가지 형태를 허용한다.

1. 단순 목록 API: `data`가 배열이다.
2. 향후 확장 또는 대량 목록 API: `data.items`와 page metadata를 포함한다.

현재 코드의 다수 목록 API는 `page`, `size`, `limit`, `offset`, `month` 같은 query parameter를 받고 `data: []` 배열을 반환한다. 클라이언트는 요청 size보다 응답 개수가 적으면 다음 페이지가 없다고 판단한다.

Page metadata가 필요한 API는 아래 구조를 사용한다.

```json
{
  "success": true,
  "data": {
    "items": [],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "hasNext": true
  },
  "message": null
}
```

기본값:

```text
page = 0
size = 20
max size = 100
```

---

### 2.4 Required Access Level

각 API는 `Required Access Level`을 명시한다.

```text
PUBLIC
AUTHENTICATED
INVITE_VERIFIED
MEMBER
ADMIN
```

Access Level 계산 기준:

```text
PUBLIC
- 인증 불필요

AUTHENTICATED
- 유효한 Kakao OAuth 기반 로그인 세션 또는 JWT 쿠키 존재

INVITE_VERIFIED
- invite_verified_at is not null
- deactivated_at is null
- 약관 동의와 온보딩은 아직 완료 전일 수 있음

MEMBER
- onboarding_completed_at is not null
- deactivated_at is null

ADMIN
- MEMBER 조건 충족
- role = ADMIN
```

---

### 2.5 Common HTTP Status Codes

| HTTP Status | Usage |
|---:|---|
| 200 | 조회/수정 성공 |
| 201 | 생성 성공 |
| 204 | 응답 본문 없는 성공 |
| 400 | Validation 실패 |
| 401 | 인증 필요 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 중복/충돌 |
| 413 | 업로드 용량 초과 |
| 429 | 요청 과다 |
| 500 | 서버 오류 |

---

### 2.6 Common Error Codes

| Code | Meaning |
|---|---|
| `UNAUTHORIZED` | 로그인 필요 |
| `FORBIDDEN` | 권한 없음 |
| `VALIDATION_ERROR` | 입력값 검증 실패 |
| `INVALID_INVITE_CODE` | 초대코드 오류 |
| `NICKNAME_ALREADY_EXISTS` | 닉네임 중복 |
| `ALREADY_ONBOARDED` | 이미 온보딩 완료 |
| `MEMBER_DEACTIVATED` | 비활성 회원 |
| `BOOK_NOT_FOUND` | 책 없음 |
| `READING_RECORD_NOT_FOUND` | 독서기록 없음 |
| `ACTION_PLAN_ALREADY_EXISTS` | 해당 월 실행계획 이미 존재 |
| `REFLECTION_NOT_FOUND` | 회고 없음 |
| `MEETING_NOT_FOUND` | 모임 없음 |
| `MEETING_CAPACITY_EXCEEDED` | 정원 초과 |
| `ALREADY_JOINED_MEETING` | 이미 참석 처리됨 |
| `REVIEW_IMAGE_LIMIT_EXCEEDED` | 후기 사진 개수 초과 |
| `IMAGE_TOO_LARGE` | 이미지 용량 초과 |
| `UNSUPPORTED_IMAGE_TYPE` | 지원하지 않는 이미지 형식 |

---

## 3. Validation Defaults

MVP 기본 길이 제한은 아래와 같다.

| Field | Rule |
|---|---|
| nickname | 2~20자, 중복 불가 |
| realName | 2~50자, 필수 |
| oneLineIntro | 최대 80자 |
| futureMeAt50 | 최대 1000자 |
| joinReason | 최대 1000자, 선택 |
| currentConcern | 최대 1000자, 선택 |
| threeYearGoal | 최대 1000자, 선택 |
| bookTitle | 최대 200자 |
| author | 최대 100자 |
| oneLineReview | 최대 200자 |
| blogUrl | 최대 500자, URL 형식 |
| actionPlanTitle | 최대 100자, 선택 |
| actionPlanContent | 최대 5000자 |
| reflection field | 각 최대 2000자 |
| meetingTitle | 최대 100자 |
| meetingDescription | 최대 2000자 |
| exactLocation | 최대 300자 |
| reviewTitle | 최대 100자 |
| reviewContent | 최대 5000자 |

---

## 4. Auth API

### 4.1 Start Kakao Login

```http
GET /api/v1/auth/kakao/login
```

Required Access Level: `PUBLIC`

Purpose:

카카오 OAuth 인증 페이지로 리다이렉트한다.

Response:

```text
302 Redirect to Kakao OAuth URL
```

---

### 4.2 Kakao OAuth Callback

```http
GET /api/v1/auth/kakao/callback?code={code}&state={state}
```

Required Access Level: `PUBLIC`

Purpose:

카카오 인증 코드를 받아 기존 회원은 서버 세션/JWT 쿠키를 발급하고, 신규 사용자는 가입 완료 전 `signup_token`을 발급한다.

System Behavior:

```text
1. Kakao token exchange
2. Kakao profile 조회
3. oauth_accounts에서 provider_user_id로 기존 member_id 조회
4. 기존 회원이면 oauth_accounts 갱신 후 JWT access_token / refresh_token HttpOnly Cookie 발급
5. 신규 사용자이면 members/oauth_accounts row를 만들지 않고 HttpOnly signup_token 발급
6. 상태에 따라 프론트 리다이렉트
```

Redirect target:

```text
온보딩 완료 회원: /
신규 또는 온보딩 미완료 사용자: /onboarding
```

---

### 4.3 Get Current User

```http
GET /api/v1/auth/me
```

Required Access Level: `PUBLIC`

Response:

```json
{
  "success": true,
  "data": {
    "authenticated": true,
    "accessLevel": "MEMBER",
    "memberId": 42,
    "role": "MEMBER",
    "displayName": "노아",
    "profileImageUrl": "https://...",
    "inviteVerified": true,
    "termsAgreed": true,
    "privacyAgreed": true,
    "onboardingCompleted": true,
    "deactivated": false
  },
  "message": null
}
```

비로그인 응답:

```json
{
  "success": true,
  "data": {
    "authenticated": false,
    "accessLevel": "PUBLIC"
  },
  "message": null
}
```

---

### 4.4 Refresh Token

```http
POST /api/v1/auth/refresh
```

Required Access Level: `AUTHENTICATED`

Purpose:

refresh_token cookie가 유효할 때 access_token을 재발급한다.

---

### 4.5 Logout

```http
POST /api/v1/auth/logout
```

Required Access Level: `AUTHENTICATED`

Purpose:

인증 쿠키를 만료시킨다.

---

## 5. Onboarding API

### 5.1 Verify Invite Code

```http
POST /api/v1/onboarding/invite-code
```

Required Access Level: `AUTHENTICATED`

Request:

```json
{
  "code": "test"
}
```

System Behavior:

```text
- 일반 멤버용 활성 코드와 운영진용 활성 코드 2종을 유지한다.
- 같은 종류의 활성 코드는 1개만 유지한다.
- 대소문자 구분 없이 비교한다.
- 성공 시 signup_token의 inviteVerified 값을 true로 갱신하고, 코드 종류에 따른 예정 Role을 담는다.
```

Response:

```json
{
  "success": true,
  "data": {
    "verified": true,
    "role": "MEMBER"
  },
  "message": "초대코드가 확인되었습니다."
}
```

---

### 5.2 Agree Terms

```http
POST /api/v1/onboarding/terms
```

Required Access Level: `INVITE_VERIFIED`

Precondition:

```text
invite_verified_at is not null
```

Request:

```json
{
  "termsAgreed": true,
  "privacyAgreed": true
}
```

Validation:

```text
termsAgreed = true 필수
privacyAgreed = true 필수
```

System Behavior:

```text
terms_agreed_at을 저장한다.
privacy_agreed_at을 저장한다.
```

---

### 5.3 Complete Onboarding Profile

```http
POST /api/v1/onboarding/profile
```

Required Access Level: `INVITE_VERIFIED`

Precondition:

```text
invite_verified_at is not null
terms_agreed_at is not null
privacy_agreed_at is not null
onboarding_completed_at is null
```

Request:

```json
{
  "nickname": "노아",
  "oneLineIntro": "매일 조금씩 성장하는 백엔드 개발자",
  "realName": "이재훈",
  "displayNameType": "REAL_NAME",
  "birthDate": "1990-05-10",
  "profileImageId": 100,
  "interestTagIds": [1, 2, 3],
  "futureMeAt50": "기술과 사업을 연결하는 사람이 된다.",
  "joinReason": "성장하는 사람들과 함께하고 싶어서",
  "currentConcern": "꾸준한 실행",
  "threeYearGoal": "개인 프로젝트를 사업화하기"
}
```

Required fields:

```text
nickname
realName
birthDate
oneLineIntro
interestTagIds 최소 1개
futureMeAt50
displayNameType
```

Optional fields:

```text
profileImageId
joinReason
currentConcern
threeYearGoal
```

Validation:

```text
nickname 중복 불가
nickname 2~20자
realName 2~50자
oneLineIntro 1~80자
futureMeAt50 1~1000자
joinReason/currentConcern/threeYearGoal 입력 시 각 1000자 이하
birthDate는 미래 날짜 불가
profileImageId는 가입 중 업로드한 PROFILE 이미지 또는 null
displayNameType은 REAL_NAME 또는 NICKNAME이며 기본 선택은 REAL_NAME
```

System Behavior:

```text
1. signup_token의 Kakao Provider ID와 fallback 프로필을 검증한다.
2. members row를 생성하고 invite_verified_at, terms_agreed_at, privacy_agreed_at, onboarding_completed_at을 저장한다.
3. oauth_accounts row를 생성하거나 provider_user_id에 member_id를 연결한다.
4. interest tag 매핑과 프로필 이미지 소유자를 저장한다.
5. signup_token을 삭제하고 JWT access_token / refresh_token HttpOnly Cookie를 발급한다.
```

---

## 6. Member / Profile API

### 6.1 Get People List

```http
GET /api/v1/people?page=0&size=20&interestTagId=1
```

Required Access Level: `PUBLIC`

Purpose:

성장하는 사람들 목록을 조회한다.

Guest 공개 정보:

```text
profileImageUrl
displayName
oneLineIntro
interestTags
futureMeAt50 summary
growthStats summary
recentPublicActivity
```

Member 추가 정보는 상세 API에서 제공한다.

---

### 6.2 Get Public/Member Profile Detail

```http
GET /api/v1/people/{memberId}
```

Required Access Level: `PUBLIC`

Response는 요청자의 Access Level에 따라 달라진다.

PUBLIC fields:

```text
memberId
displayName
profileImageUrl
oneLineIntro
interestTags
futureMeAt50
growthStats
recentReadingRecords
recentMeetingReviews
recentPublicActivities
```

MEMBER additional fields:

```text
joinReason
currentConcern
threeYearGoal
recentActionPlans
recentReflections
```

---

### 6.3 Get My Dashboard

```http
GET /api/v1/me/dashboard?month=2026-07
```

Required Access Level: `MEMBER`

Purpose:

마이페이지 첫 화면에 필요한 정보를 한 번에 조회한다.

Response:

```json
{
  "success": true,
  "data": {
    "profile": {
      "memberId": 42,
      "displayName": "노아",
      "profileImageUrl": "https://...",
      "oneLineIntro": "매일 조금씩 성장하는 백엔드 개발자"
    },
    "participation": {
      "month": "2026-07",
      "readingRecordCount": 1,
      "actionPlanCount": 0,
      "completed": true,
      "coffeeSupportTarget": false
    },
    "quickStats": {
      "readingRecordCount": 24,
      "actionPlanCount": 8,
      "monthlyReflectionCount": 5,
      "meetingReviewCount": 3,
      "smallMeetingCreatedCount": 2
    },
    "recentActivities": []
  },
  "message": null
}
```

---

### 6.4 Get My Profile

```http
GET /api/v1/me/profile
```

Required Access Level: `MEMBER`

---

### 6.5 Update My Profile

```http
PUT /api/v1/me/profile
```

Required Access Level: `MEMBER`

Editable fields:

```text
nickname
oneLineIntro
realName
displayNameType
birthDate
profileImageId
interestTagIds
futureMeAt50
joinReason
currentConcern
threeYearGoal
profileImageId
```

Notes:

```text
futureMeAt50은 언제든 수정 가능하다.
nickname은 중복 불가다.
```

---

## 7. Interest Tag API

### 7.1 Get Interest Tags

```http
GET /api/v1/interest-tags
```

Required Access Level: `PUBLIC`

Purpose:

회원 온보딩과 성장하는 사람들 필터에서 사용할 관심 분야 태그를 조회한다.

---

## 8. Book / Book Search API

### 8.1 Search Books

```http
GET /api/v1/books/search?query=원씽&page=0&size=10
```

Required Access Level: `MEMBER`

Provider:

```text
MVP 기본: Kakao Book Search API
```

System Behavior:

```text
1. 내부 books 테이블 우선 검색 가능
2. 필요 시 Kakao Book Search API 호출
3. 검색 결과를 내부 BookSearchResult DTO로 정규화
```

---

### 8.2 Create Manual Book

```http
POST /api/v1/books/manual
```

Required Access Level: `MEMBER`

Purpose:

책 검색 결과가 없을 때 Member가 직접 책을 임시 등록한다.

Request:

```json
{
  "title": "검색되지 않는 책 제목",
  "author": "저자명",
  "publisher": "출판사",
  "publishedDate": "2024-01-01",
  "thumbnailImageId": 100
}
```

Required:

```text
title
author
```

System Behavior:

```text
source = MANUAL
verificationStatus = UNVERIFIED
```

Rule:

```text
책 검색 실패가 독서기록 작성을 막으면 안 된다.
향후 스케줄러 또는 Admin 정리 기능으로 UNVERIFIED 책을 검증/병합할 수 있다.
```

---

### 8.3 Get Book Detail

```http
GET /api/v1/books/{bookId}
```

Required Access Level: `PUBLIC`

Includes:

```text
book info
average rating
reading record count
reader count
readers preview
reading records accordion data
```

---

## 9. Reading Record API

### 9.1 Get Reading Records

```http
GET /api/v1/reading-records?page=0&size=20&bookId=1&memberId=42
```

Required Access Level: `PUBLIC`

Public fields:

```text
book
member display info
rating nullable
oneLineReview
recordImageUrl
blogUrl
recordedAt
createdAt
```

Blog URL is visible to Guest.

---

### 9.2 Create Reading Record

```http
POST /api/v1/reading-records
```

Required Access Level: `MEMBER`

Request:

```json
{
  "bookId": 1,
  "rating": 5,
  "oneLineReview": "중요한 건 결국 하나를 정하고 끝까지 가는 힘이다.",
  "imageId": 100,
  "blogUrl": "https://blog.example.com/post/1"
}
```

Required:

```text
bookId
oneLineReview
blogUrl
```

Optional:

```text
rating
imageId
```

Rating:

```text
1~5 정수
선택 입력
```

RecordedAt Policy:

```text
일반 회원 작성 API는 recordedAt을 서버 저장 시각으로 설정한다.
마이그레이션 도구 또는 Admin import만 원천 recorded_at을 주입할 수 있다.
월별 참여 계산과 월별 독서기록 목록은 recordedAt 기준이다.
```

---

### 9.3 Update Reading Record

```http
PUT /api/v1/reading-records/{readingRecordId}
```

Required Access Level: `MEMBER`

Authorization:

```text
작성자만 수정 가능
Admin도 내용 직접 수정 불가
```

---

### 9.4 Delete Reading Record

```http
DELETE /api/v1/reading-records/{readingRecordId}
```

Required Access Level: `MEMBER`

Authorization:

```text
작성자만 삭제 가능
```

Delete Policy:

```text
soft delete
```

---

## 10. Monthly Action Plan API

### 10.1 Get My Monthly Action Plan

```http
GET /api/v1/me/action-plans/{month}
```

Required Access Level: `MEMBER`

`month` format:

```text
YYYY-MM
```

---

### 10.2 Create Monthly Action Plan

```http
POST /api/v1/me/action-plans
```

Required Access Level: `MEMBER`

Request:

```json
{
  "month": "2026-07",
  "title": "7월 실행계획",
  "content": "- 독서 2권\n- 운동 15회\n- 사이드 프로젝트 MVP 개발"
}
```

Policy:

```text
월 1개
자유 입력
Member 전용 공개
참여 현황 계산에 포함
```

---

### 10.3 Update Monthly Action Plan

```http
PUT /api/v1/me/action-plans/{actionPlanId}
```

Required Access Level: `MEMBER`

Authorization:

```text
작성자만 수정 가능
```

---

### 10.4 Delete Monthly Action Plan

```http
DELETE /api/v1/me/action-plans/{actionPlanId}
```

Required Access Level: `MEMBER`

Delete Policy:

```text
soft delete
```

---

## 11. Monthly Reflection API

### 11.1 Get My Monthly Reflection

```http
GET /api/v1/me/reflections/{month}
```

Required Access Level: `MEMBER`

Purpose:

해당 월 회고 슬롯과 작성 내용을 조회한다.

Policy:

```text
월간 회고 슬롯은 매월 제공된다.
작성하지 않아도 문제 없다.
참여 현황 계산에 포함하지 않는다.
실제 내용을 저장했을 때만 activity event가 생성된다.
```

---

### 11.2 Save Monthly Reflection

```http
PUT /api/v1/me/reflections/{month}
```

Required Access Level: `MEMBER`

Request:

```json
{
  "didWell": "이번 달 잘한 것",
  "couldImprove": "아쉬운 점",
  "nextFocus": "다음 달 집중할 것"
}
```

---

## 12. Participation API

### 12.1 Get My Participation Status

```http
GET /api/v1/me/participation/{month}
```

Required Access Level: `MEMBER`

Rule:

```text
해당 월에 아래 중 하나 이상 충족하면 참여 완료

1. 독서기록 1건 이상
또는
2. 월간 실행계획 1건 이상
```

Response:

```json
{
  "success": true,
  "data": {
    "month": "2026-07",
    "readingRecordCount": 1,
    "actionPlanCount": 0,
    "completed": true,
    "coffeeSupportTarget": false,
    "coffeeSupportItem": "투썸 아메리카노 1잔"
  },
  "message": null
}
```

Policy:

```text
신규 가입자는 가입 다음 달부터 참여 현황 계산 대상이다.
Soft delete된 기록은 계산에서 제외한다.
```

---

### 12.2 Get Admin Participation Status

```http
GET /api/v1/admin/participation?month=2026-07
```

Required Access Level: `ADMIN`

Response includes:

```text
전체 회원 수
참여 완료 수
미참여 수
미참여자 목록
운영 메모
CSV 다운로드 URL 또는 별도 endpoint
```

---

### 12.3 Download Admin Participation CSV

```http
GET /api/v1/admin/participation.csv?month=2026-07
```

Required Access Level: `ADMIN`

---

### 12.4 Save Admin Participation Note

```http
PUT /api/v1/admin/participation/{month}/members/{memberId}/note
```

Required Access Level: `ADMIN`

Request:

```json
{
  "note": "신규 가입자라 이번 달 제외"
}
```

---

## 13. Meeting API

### 13.1 Get Meeting List

```http
GET /api/v1/meetings?page=0&size=20&type=REGULAR_READING
```

Required Access Level: `PUBLIC`

Guest visible fields:

```text
meetingId
title
description summary
meetingAt
locationRegion
attendeeCount
small attendee profile preview images
```

Guest hidden fields:

```text
exactLocation
attendee names
attendee profile links
```

---

### 13.2 Get Meeting Detail

```http
GET /api/v1/meetings/{meetingId}
```

Required Access Level: `PUBLIC`

PUBLIC view:

```text
모임명
설명
일시
지역 수준 장소
참석자 수
아주 작은 원형 프로필 이미지 일부
후기 목록
```

MEMBER additional view:

```text
정확한 장소
참석자 표시명
참석자 프로필 이동
참석하기/취소하기
```

---

### 13.3 Create Small Meeting

```http
POST /api/v1/meetings
```

Required Access Level: `MEMBER`

Request:

```json
{
  "title": "러닝 소소모임",
  "description": "한강에서 같이 뛰어요.",
  "meetingAt": "2026-07-12T10:00:00+09:00",
  "locationRegion": "서울 여의도",
  "exactLocation": "여의나루역 2번 출구",
  "capacity": 10,
  "thumbnailImageId": 100,
  "feeAmount": 0
}
```

Policy:

```text
Member 누구나 소소모임 생성 가능
생성자만 수정 가능
Admin은 숨김/삭제만 가능
```

---

### 13.4 Update Small Meeting

```http
PUT /api/v1/meetings/{meetingId}
```

Required Access Level: `MEMBER`

Authorization:

```text
소소모임 생성자만 수정 가능
Admin도 소소모임 내용 직접 수정 불가
```

---

### 13.5 Join Meeting

```http
POST /api/v1/meetings/{meetingId}/join
```

Required Access Level: `MEMBER`

Capacity Rule:

```text
capacity null 또는 0: 제한 없음
capacity > 0: 현재 참석자 수가 capacity 이상이면 409
```

---

### 13.6 Cancel Meeting Attendance

```http
DELETE /api/v1/meetings/{meetingId}/join
```

Required Access Level: `MEMBER`

---

## 14. Meeting Review API

### 14.1 Get Meeting Review List

```http
GET /api/v1/reviews?page=0&size=20&meetingId=1
```

Required Access Level: `PUBLIC`

---

### 14.2 Get Meeting Review Detail

```http
GET /api/v1/reviews/{reviewId}
```

Required Access Level: `PUBLIC`

Includes:

```text
title
content
writer display info
meeting info
images
createdAt
```

---

### 14.3 Create Meeting Review

```http
POST /api/v1/reviews
```

Required Access Level: `MEMBER`

Policy:

```text
시스템 참석 여부로 제한하지 않는다.
Active Member는 참석 버튼을 누르지 않았어도 후기를 작성할 수 있다.
```

Request:

```json
{
  "meetingId": 1,
  "title": "7월 독서모임 후기",
  "content": "좋은 대화를 나눴습니다.",
  "imageIds": [101, 102, 103]
}
```

Validation:

```text
title 필수
content 필수
imageIds 최대 10개
```

UI 안내 문구:

```text
업로드한 사진은 공개 모임 후기에 노출될 수 있어요.
함께 나온 사람들에게 공개 가능 여부를 확인해주세요.
```

---

### 14.4 Update Meeting Review

```http
PUT /api/v1/reviews/{reviewId}
```

Required Access Level: `MEMBER`

Authorization:

```text
작성자만 수정 가능
Admin도 내용 직접 수정 불가
```

---

### 14.5 Delete Meeting Review

```http
DELETE /api/v1/reviews/{reviewId}
```

Required Access Level: `MEMBER`

Authorization:

```text
작성자만 삭제 가능
```

Delete Policy:

```text
soft delete
```

---

## 15. Home / Activity API

### 15.1 Get Home Data

```http
GET /api/v1/home
```

Required Access Level: `PUBLIC`

Includes:

```text
hero data
recommended books
recent activity events
popular books top 5
recent meetings
recent reviews
growing people preview
```

---

### 15.2 Get Recent Activities

```http
GET /api/v1/activities/recent?page=0&size=20
```

Required Access Level: `PUBLIC`

Included event types:

```text
READING_RECORD_CREATED
ACTION_PLAN_CREATED
REFLECTION_CREATED
MEETING_REVIEW_CREATED
MEETING_CREATED
MEETING_HELD
```

Note:

```text
월간 회고는 사용자가 실제 내용을 저장했을 때만 activity event를 만든다.
```

---

## 16. Upload API

### 16.1 Upload Single Image

```http
POST /api/v1/uploads/images
Content-Type: multipart/form-data
```

Required Access Level: `MEMBER`

Form Data:

```text
file
purpose = PROFILE | READING_RECORD | MEETING | REVIEW | BOOK
```

Validation:

```text
최대 10MB
지원 형식: jpg, jpeg, png, webp
프론트는 jpg, jpeg, png, webp 및 브라우저 지원 시 heic/heif를 WebP로 변환해 전송
서버는 jpg, jpeg, png, webp를 검증하고 방어적으로 WebP 또는 리사이즈된 JPEG로 최적화
원본 저장 안 함
```

Response:

```json
{
  "success": true,
  "data": {
    "imageId": 100,
    "url": "https://...",
    "width": 1200,
    "height": 800
  },
  "message": null
}
```

---

### 16.2 Upload Multiple Review Images

```http
POST /api/v1/uploads/review-images
Content-Type: multipart/form-data
```

Required Access Level: `MEMBER`

Validation:

```text
파일당 최대 10MB
최대 10장
권장 전체 요청 크기 50MB 이하
```

Note:

```text
MVP에서는 단일 업로드 API를 여러 번 호출하는 방식도 허용한다.
프론트는 네트워크 안정성을 위해 단일 업로드 API를 2개씩 제한 병렬 호출한다.
일부 실패 시 성공한 이미지 ID만 후기 생성 요청에 포함한다.
실패한 이미지는 후기 저장 후 수정 화면에서 다시 추가하도록 안내한다.
업로드 요청은 프론트 기준 25초 타임아웃을 둔다.
```

---

### 16.3 Orphan Image Cleanup

이미지 업로드는 후기/프로필/독서기록 저장보다 먼저 완료될 수 있으므로, 사용자가 저장 전에 이탈하면 연결되지 않은 `image_assets`가 남을 수 있다.

Policy:

```text
백엔드 Spring Scheduler가 고아 이미지 정리를 수행한다.
어느 도메인에도 연결되지 않은 image_asset만 정리 대상이다.
업로드 직후 저장 중인 이미지를 지우지 않도록 24시간 유예 후 정리한다.
정리 순서는 Storage object 삭제 후 DB row 삭제다.
Storage 삭제 실패 시 DB row는 유지하고 로그를 남긴다.
```

---

## 17. Admin API

### 17.1 Get Admin Dashboard

```http
GET /api/v1/admin/dashboard
```

Required Access Level: `ADMIN`

Includes:

```text
회원 수
이번 달 참여 완료/미참여 수
최근 독서기록 수
최근 모임 후기 수
UNVERIFIED 책 수
```

---

### 17.2 Member Management

#### Get Members

```http
GET /api/v1/admin/members?page=0&size=20&keyword=노아
```

Required Access Level: `ADMIN`

#### Deactivate Member

```http
POST /api/v1/admin/members/{memberId}/deactivate
```

Required Access Level: `ADMIN`

System Behavior:

```text
deactivated_at과 admin_deactivated_at 저장
운영진 강퇴 회원은 운영진 해제 전 재가입 불가
```

#### Reactivate Member

```http
POST /api/v1/admin/members/{memberId}/reactivate
```

Required Access Level: `ADMIN`

System Behavior:

```text
deactivated_at과 admin_deactivated_at null 처리
```

#### Update Participation Start Month

```http
PUT /api/v1/admin/members/{memberId}/participation-start-month
```

Required Access Level: `ADMIN`

Request:

```json
{
  "participationStartMonth": "2026-08"
}
```

Validation:

```text
YYYY-MM 형식
월 단위 값이며 DB에는 해당 월 1일 DATE로 정규화
```

System Behavior:

```text
members.participation_start_month를 갱신한다.
기존 회원 마이그레이션 또는 운영 보정에 사용한다.
가능하면 Admin audit log를 남긴다.
```

---

### 17.3 Invite Code Management

#### Get Active Invite Code

```http
GET /api/v1/admin/invite-code
```

Required Access Level: `ADMIN`

#### Update Invite Code

```http
PUT /api/v1/admin/invite-code
```

Required Access Level: `ADMIN`

Request:

```json
{
  "code": "test"
}
```

Policy:

```text
일반 멤버용 활성 코드와 운영진용 활성 코드 2종을 유지한다.
같은 종류의 새 코드 저장 시 기존 코드는 즉시 무효화한다.
```

---

### 17.4 Interest Tag Management

```http
GET /api/v1/admin/interest-tags
POST /api/v1/admin/interest-tags
PUT /api/v1/admin/interest-tags/{tagId}
DELETE /api/v1/admin/interest-tags/{tagId}
```

Required Access Level: `ADMIN`

---

### 17.5 Recommended Book Management

```http
GET /api/v1/admin/recommended-books
GET /api/v1/admin/recommended-books?month=2026-07
POST /api/v1/admin/recommended-books
PUT /api/v1/admin/recommended-books/{recommendedBookId}
DELETE /api/v1/admin/recommended-books/{recommendedBookId}
POST /api/v1/admin/recommended-books/{recommendedBookId}/hide
POST /api/v1/admin/recommended-books/{recommendedBookId}/restore
```

Required Access Level: `ADMIN`

Policy:

```text
공개 라이브러리/홈 조회는 ACTIVE 추천책 전체를 display_order ASC, id ASC로 노출
Admin 목록은 month 없이 조회하면 DELETED가 아닌 추천책 전체를 관리용으로 노출
Admin 목록은 month를 지정하면 해당 월 ACTIVE 추천책을 조회
추천 이유 필수
노출 순서 지정 가능
숨김/복구/삭제 가능
```

---

### 17.6 Admin Regular Meeting Management

정기모임은 Admin이 운영 정보 수정 가능하다.

```http
GET /api/v1/admin/meetings
GET /api/v1/admin/meetings/{meetingId}
PUT /api/v1/admin/meetings/{meetingId}
```

Required Access Level: `ADMIN`

Admin 수정 가능 대상:

```text
REGULAR_READING
REGULAR_ACTION
```

Admin 수정 가능 필드:

```text
title
description
meetingAt
locationRegion
exactLocation
capacity
thumbnailImageId
feeAmount
status
```

Small Meeting policy:

```text
소소모임은 생성자만 수정 가능하다.
Admin은 숨김/삭제만 가능하다.
```

---

### 17.7 Admin Content Moderation

Admin은 회원 콘텐츠의 내용을 직접 수정하지 않는다.

Admin 가능:

```http
POST /api/v1/admin/reading-records/{id}/hide
POST /api/v1/admin/reading-records/{id}/restore
DELETE /api/v1/admin/reading-records/{id}

POST /api/v1/admin/meetings/{id}/hide
POST /api/v1/admin/meetings/{id}/restore
DELETE /api/v1/admin/meetings/{id}

POST /api/v1/admin/reviews/{id}/hide
POST /api/v1/admin/reviews/{id}/restore
DELETE /api/v1/admin/reviews/{id}
```

Required Access Level: `ADMIN`

---

### 17.8 Book Verification Admin API

```http
GET /api/v1/admin/books?verificationStatus=UNVERIFIED
PUT /api/v1/admin/books/{bookId}
POST /api/v1/admin/books/{bookId}/verify
```

Required Access Level: `ADMIN`

Phase 2 candidate:

```http
POST /api/v1/admin/books/merge
```

MVP에서는 병합 UI는 제외 가능하다.

---

## 18. Scheduler Jobs

### 18.1 Create Monthly Regular Meetings

Schedule:

```text
매월 1일 00:10 KST
```

Behavior:

```text
해당 월 정기모임 2개 자동 생성
이미 생성되어 있으면 중복 생성하지 않음
```

Default meetings:

```text
월간 독서기록모임
- 매월 2번째 일요일 오전 10시

월간 실행수다모임
- 매월 4번째 일요일 오전 10시
```

---

### 18.2 Optional Book Verification Scheduler

Purpose:

직접 등록된 UNVERIFIED 책을 향후 자동 검색/검증할 수 있도록 확장한다.

MVP에서는 필수 구현이 아니다.

---

## 19. Acceptance Criteria

### AC-001 Auth

Given 비로그인 사용자가 카카오 로그인을 완료했을 때
When 인증이 성공하면
Then 서버는 JWT 쿠키를 발급하고 온보딩 상태에 따라 적절한 페이지로 이동시킨다.

---

### AC-002 Onboarding

Given 카카오 로그인 사용자가 초대코드를 입력했을 때
When 활성 초대코드와 일치하면
Then invite_verified_at이 저장되고 약관 동의 단계로 이동한다.

---

### AC-003 Reading Record

Given Member가 책 검색 결과에서 책을 선택하거나 직접 책을 등록했을 때
When 독서기록을 저장하면
Then 책 상세, 프로필, 최근 성장 기록에 반영된다.

---

### AC-004 Participation

Given Member가 해당 월에 독서기록 1건 또는 실행계획 1건을 작성했을 때
When 참여 현황을 조회하면
Then completed=true로 반환된다.

---

### AC-005 Meeting Review Images

Given Member가 모임 후기를 작성할 때
When 사진을 10장을 초과하여 첨부하면
Then API는 `REVIEW_IMAGE_LIMIT_EXCEEDED` 에러를 반환한다.

---

### AC-006 Admin Content Policy

Given Admin이 회원의 독서기록 또는 후기를 관리할 때
When 콘텐츠 조치가 필요하면
Then Admin은 숨김/복구/삭제만 할 수 있고 내용을 직접 수정할 수 없다.

---

## 20. Codex Implementation Guidelines

Codex는 이 문서를 기준으로 구현할 때 다음 규칙을 따른다.

```text
1. Access Level을 먼저 구현한다.
2. account_status enum을 만들지 않는다.
3. invite_verified_at, terms_agreed_at, privacy_agreed_at, onboarding_completed_at, deactivated_at은 TIMESTAMPTZ로 관리한다.
4. role과 timestamp 필드 기반으로 INVITE_VERIFIED/MEMBER/ADMIN 권한을 계산한다.
5. 모든 API 응답은 Common Envelope를 사용한다.
6. Controller DTO에는 Bean Validation을 적용한다.
7. Soft delete 리소스는 기본 조회에서 제외한다.
8. 상태 변경 요청에는 Origin/Referer 검증을 적용한다.
9. 모임 후기 사진은 최대 10장으로 제한한다.
10. Open Questions가 생기면 임의 구현하지 말고 TODO로 남긴다.
```

---

## 21. MVP Completion Checklist for API

```text
Auth / Onboarding
- [ ] Kakao OAuth login
- [ ] JWT HttpOnly Cookie 발급
- [ ] 초대코드 검증
- [ ] 약관 동의
- [ ] 온보딩 완료

Member
- [ ] /auth/me
- [ ] /me/dashboard
- [ ] 프로필 조회/수정
- [ ] 성장하는 사람들 목록/상세

Books / Reading
- [ ] Kakao 책 검색
- [ ] 직접 책 등록 UNVERIFIED
- [ ] 독서기록 CRUD
- [ ] 책 상세

Monthly
- [ ] 실행계획 CRUD
- [ ] 월간 회고 저장
- [ ] 참여 현황 계산

Meetings / Reviews
- [ ] 모임 목록/상세
- [ ] 소소모임 생성/수정
- [ ] 참석/취소
- [ ] 후기 CRUD
- [ ] 후기 사진 최대 10장 제한

Admin
- [ ] 회원 비활성/재활성
- [ ] 초대코드 변경
- [ ] 관심 태그 관리
- [ ] 추천책 관리
- [ ] 정기모임 수정
- [ ] 콘텐츠 숨김/삭제
- [ ] 참여 현황 CSV
```
