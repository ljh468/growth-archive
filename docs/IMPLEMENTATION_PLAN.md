# IMPLEMENTATION_PLAN

# 부자습관 만들기 - Growth Archive MVP 구현 계획

Version: 1.0  
Status: FINAL  
Audience: Noah, Codex, Backend/Frontend Developer  
Last Updated: 2026-06-22

---

## 0. 문서 목적

이 문서는 `부자습관 만들기 - Growth Archive` MVP를 Codex에게 구현시키기 위한 실행 계획서다.

이 문서는 PRD/TSD/OPS 문서를 실제 구현 순서로 변환한다.

Codex는 이 문서를 기준으로 다음을 수행해야 한다.

1. 문서를 순서대로 읽는다.
2. MVP 범위를 벗어난 기능을 추가하지 않는다.
3. Phase 단위로 구현한다.
4. 각 Phase 종료 시 테스트를 실행한다.
5. 구현 결과를 체크리스트와 비교한다.
6. 애매한 요구사항은 임의로 확정하지 않고 TODO 또는 질문으로 남긴다.

---

## 1. 최종 목표

Growth Archive MVP는 다음을 제공해야 한다.

- 카카오 로그인
- 초대코드 기반 멤버 인증
- 온보딩
- 성장 프로필
- 독서기록 라이브러리
- 카카오 책 검색
- 검색 결과 없는 책 직접 등록
- 월간 실행계획
- 월간 회고
- 이번 달 참여 현황
- 정기모임 / 소소모임
- 모임 후기
- 이미지 업로드
- 관리자 기능
- 기존 데이터 이관 기반
- 모바일 퍼스트 웹
- Docker Compose 로컬 개발
- Kubernetes-ready 구조

---

## 2. Codex가 먼저 읽어야 할 문서 순서

Codex는 구현 전 아래 문서를 반드시 순서대로 읽어야 한다.

```text
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
```

현재 작업 저장소에 `docs/` 구조가 없다면, 위 문서들을 먼저 해당 경로로 복사하거나 문서 경로를 실제 저장소 구조에 맞게 정리한다.

---

## 3. 구현 원칙

### 3.1 MVP 우선

Codex는 PRD에 없는 기능을 임의로 추가하지 않는다.

MVP 제외 기능:

- 결제
- 채팅
- 댓글
- 좋아요
- 포인트
- 랭킹
- 경쟁형 배지
- 푸시 알림
- 네이티브 앱
- 고도화된 성장 로드맵
- 통합 검색
- 대기자 기능
- 복잡한 권한 체계

---

### 3.2 단순함 우선

이 서비스는 기록 저장이 쉬워야 한다.

따라서 Codex는 기능을 만들 때 다음 원칙을 지킨다.

```text
기록을 막지 않는다.
작성 흐름을 복잡하게 만들지 않는다.
회원이 직접 관리할 수 있는 것은 단순하게 만든다.
운영진이 모든 것을 관리해야 하는 구조를 피한다.
```

예외:

- 공개 범위
- 이미지 관리
- 회원 비활성화
- 미참여자 운영 확인
- 추천책 관리

위 항목은 Admin이 관리한다.

---

### 3.3 회원 상태 관리 원칙

회원 계정에는 별도의 `account_status` enum을 사용하지 않는다.

회원 접근 권한은 다음 timestamp 필드로 계산한다.

```text
invite_verified_at
terms_agreed_at
privacy_agreed_at
onboarding_completed_at
deactivated_at
```

Role은 단순하게 유지한다.

```text
MEMBER
ADMIN
```

활성 멤버 조건:

```text
onboarding_completed_at IS NOT NULL
AND deactivated_at IS NULL
```

Admin 조건:

```text
role = ADMIN
AND onboarding_completed_at IS NOT NULL
AND deactivated_at IS NULL
```

---

### 3.4 콘텐츠 상태 관리 원칙

회원 상태 enum은 사용하지 않지만, 콘텐츠/도서/모임에는 각 도메인별 상태가 필요하다.

일반 콘텐츠 상태:

```text
ACTIVE
HIDDEN
DELETED
```

도서 검증 상태:

```text
VERIFIED
UNVERIFIED
```

모임 상태:

```text
SCHEDULED
HELD
CANCELED
HIDDEN
DELETED
```

Codex는 회원 상태 단순화와 도메인별 상태를 혼동하지 않는다.

---

### 3.5 API 응답 원칙

모든 API는 공통 Envelope 구조를 사용한다.

성공:

```json
{
  "success": true,
  "data": {},
  "message": null
}
```

실패:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "사용자에게 보여줄 메시지"
  }
}
```

---

### 3.6 인증 원칙

인증은 다음 방식으로 구현한다.

```text
JWT + HttpOnly Secure Cookie
```

MVP CSRF 정책:

- SameSite=Lax
- HttpOnly Cookie
- 운영 환경 Secure Cookie
- POST/PUT/PATCH/DELETE 요청 Origin/Referer 검증
- CORS 허용 Origin whitelist

---

### 3.7 배포 원칙

MVP 구현 기준:

```text
Docker Compose로 로컬 개발 가능
```

배포 확장 기준:

```text
Kubernetes-ready
```

초기부터 반드시 포함:

- Backend Dockerfile
- Frontend Dockerfile
- docker-compose.yml
- `.env.example`
- health check endpoint
- stateless backend
- 환경변수 기반 설정

MVP 단계에서 Helm Chart나 Kubernetes manifest는 필수가 아니다.  
OPS 또는 배포 단계에서 별도 작성한다.

---

## 4. 권장 저장소 구조

최종 저장소 구조는 아래를 권장한다.

```text
growth-archive/
├─ backend/
│  ├─ build.gradle.kts
│  ├─ settings.gradle.kts
│  ├─ Dockerfile
│  └─ src/
│
├─ frontend/
│  ├─ package.json
│  ├─ Dockerfile
│  └─ src/
│
├─ docs/
│  ├─ prd/
│  ├─ design/
│  ├─ tech/
│  ├─ ops/
│  └─ IMPLEMENTATION_PLAN.md
│
├─ scripts/
│  ├─ import/
│  └─ maintenance/
│
├─ docker-compose.yml
├─ .env.example
├─ AGENTS.md
├─ MVP_CHECKLIST.md
└─ CODEX_MVP_BUILD_PROMPT.md
```

---

## 5. Phase별 구현 계획

---

# Phase 0. Repository Bootstrap

## 목적

프로젝트의 기본 구조를 만든다.

## 구현 범위

### 공통

- 저장소 기본 구조 생성
- 문서 폴더 정리
- `.gitignore` 작성
- `.env.example` 작성
- README 초안 작성

### Backend

- Java 25 기준 Spring Boot 4.1.0 프로젝트 생성
- Gradle Kotlin DSL 사용 권장
- 기본 패키지 구조 생성
- Profile 분리
  - local
  - test
  - prod
- Health Check endpoint 생성

### Frontend

- Next.js + TypeScript 프로젝트 생성
- Tailwind CSS 설정
- shadcn/ui 설치 준비
- 기본 라우팅 구조 생성
- 모바일 퍼스트 layout shell 생성

### DevOps

- backend Dockerfile
- frontend Dockerfile
- docker-compose.yml
- PostgreSQL 연결 env placeholder

## 완료 조건

- Backend 실행 가능
- Frontend 실행 가능
- Docker Compose 실행 가능
- `/api/v1/health` 응답 가능
- README에 로컬 실행 방법 작성

## 권장 테스트

```bash
cd backend && ./gradlew test
cd frontend && pnpm lint
```

---

# Phase 1. Database Foundation & Migrations

## 목적

ERD 기반 DB 스키마와 migration 구조를 만든다.

## 관련 문서

- TSD-001
- PRD-002
- PRD-004

## 구현 범위

### Backend

- Flyway 설정
- Runtime ORM schema validation은 사용하지 않고 Flyway SQL로 스키마를 관리
- 공통 BaseEntity 생성
  - id
  - created_at
  - updated_at
- soft delete 패턴 정의
  - deleted_at
  - hidden_at 또는 status 컬럼은 엔티티별 정책에 맞게 사용

### DB Migration

아래 주요 테이블을 생성한다.

- members
- oauth_accounts
- interest_tags
- member_interest_tags
- member_join_intro_sources
- invite_codes
- books
- reading_records
- monthly_action_plans
- monthly_reflections
- meetings
- meeting_attendances
- meeting_reviews
- meeting_review_images
- recommended_books
- activity_events
- image_assets
- participation_admin_notes
- admin_audit_logs

### Seed Data

- 기본 interest_tags
- 활성 초대코드 1개
- 초기 Admin 지정용 seed 또는 script

## 완료 조건

- Flyway migration 성공
- Application boot 시 schema validation 통과
- 기본 seed 데이터 생성 가능

## 주의사항

회원 상태 enum을 만들지 않는다.

---

# Phase 2. Auth, Member, Onboarding

## 목적

카카오 로그인, 초대코드, 약관 동의, 온보딩, 권한 계산을 구현한다.

## 관련 문서

- PRD-002
- TSD-002

## Backend 구현 범위

### Auth

- Kakao OAuth login endpoint
- Kakao OAuth callback endpoint
- JWT 발급
- HttpOnly cookie 저장
- refresh token 처리
- logout
- `/api/v1/auth/me`

### Onboarding

- 초대코드 검증
- 약관 동의
- 온보딩 프로필 입력
- 닉네임 중복 확인
- 온보딩 완료 처리

### Member

- 내 프로필 조회
- 내 프로필 수정
- 프로필 이미지 연결
- 관심 분야 태그 선택

### Admin

- 회원 목록 조회
- 회원 비활성화
- 회원 재활성화
- 활성 초대코드 변경

## Frontend 구현 범위

- 로그인 화면
- 초대코드 입력 화면
- 약관 동의 화면
- 온보딩 화면
- 내 프로필 기본 화면
- 로그인 상태에 따른 헤더/마이 메뉴 처리

## 완료 조건

- 카카오 로그인 mock 또는 실제 연동 흐름 구현
- 초대코드가 맞아야 온보딩 가능
- 온보딩 완료 전 Member 기능 접근 불가
- deactivated_at이 있으면 접근 차단
- role=ADMIN인 활성 멤버만 Admin API 접근 가능

## 테스트

- 인증 성공/실패
- 초대코드 성공/실패
- 닉네임 중복
- 비활성 회원 차단
- Admin 권한 차단

---

# Phase 3. Frontend App Shell & Design System Base

## 목적

모바일 퍼스트 UI 뼈대를 만든다.

## 관련 문서

- PRD-003
- DESIGN-001

## 구현 범위

### Layout

- PC 상단 내비게이션
- 모바일 하단 내비게이션
- 모바일 상단 햄버거 메뉴
- 공통 page container
- 공통 card component
- 공통 button component
- 공통 empty state
- 공통 loading state

### Navigation

PC 메뉴:

- 독서기록 라이브러리
- 모임
- 성장하는 사람들
- 모임 후기
- 소개

Mobile bottom tabs:

- 홈
- 라이브러리
- 모임
- 사람들
- 마이

Mobile hamburger:

- 모임 후기
- 소개
- 이용약관
- 개인정보처리방침

### Brand Tokens

- Quiet Luxury Archive palette 적용
- Premium / Editorial / Real-photo-first 방향 반영

## 완료 조건

- 주요 페이지 placeholder 이동 가능
- 모바일 하단 탭 동작
- 햄버거 메뉴 동작
- 색상 토큰 적용

---

# Phase 4. Book Search & Reading Library

## 목적

독서기록 라이브러리의 핵심 기능을 구현한다.

## 관련 문서

- PRD-004
- TSD-001
- TSD-002

## Backend 구현 범위

### Book Search

- Kakao Book Search API 연동
- BookSearchProvider 인터페이스 생성
- KakaoBookSearchProvider 구현
- 검색 결과 normalize

### Book

- 책 검색 결과 저장
- 검색 결과 없을 때 직접 책 등록
- 직접 등록 책은 UNVERIFIED 상태
- 책 상세 조회
- 책 목록/검색

### ReadingRecord

- 독서기록 작성
- 독서기록 수정: 작성자만
- 독서기록 삭제: soft delete
- Admin 숨김/복구/삭제
- 블로그 URL Guest 공개
- 평점 선택 입력
- 대표 이미지 연결

### Library

- 이달의 추천책
- 인기 도서 TOP5
- 최근 독서기록
- 책 상세 작성자별 기록
- 이 책을 읽은 사람

## Frontend 구현 범위

- `/library`
- `/books`
- `/books/{bookId}`
- `/reading-records/new`
- 독서기록 수정 화면
- 책 검색 UI
- 검색 결과 없음 → 직접 등록 flow
- 작성자별 기록 카드 UI

## 완료 조건

- Member가 독서기록 작성 가능
- 책 검색 실패가 글 작성을 막지 않음
- Guest가 독서기록과 블로그 URL 조회 가능
- Admin은 독서기록 내용 직접 수정 불가
- 인기 도서 TOP5 표시
- 추천책 표시

---

# Phase 5. Growth People, Profile, My Page

## 목적

사람 중심 성장 아카이브를 구현한다.

## 관련 문서

- PRD-003
- PRD-004
- DESIGN-001

## Backend 구현 범위

- 성장하는 사람들 목록 API
- 공개 프로필 API
- Member 전용 프로필 상세 API
- `/api/v1/me/dashboard`
- 성장 통계 계산
- 최근 공개 활동 조회
- 최근 독서기록 3개
- 최근 실행계획 3개
- 최근 회고 3개

## Frontend 구현 범위

- `/people`
- `/people/{memberId}`
- `/mypage`
- `/mypage/profile`
- 성장하는 사람들 카드 UI
- 50살의 나 Hero
- 성장 통계 카드
- 최근 활동 카드

## 공개 범위

Guest 공개:

- 프로필 사진
- 표시명
- 한 줄 소개
- 관심 분야
- 50살의 나
- 성장 통계 요약
- 최근 공개 활동

Member 전용:

- 가입 이유
- 현재 고민
- 3년 뒤 목표
- 실행계획
- 회고

## 완료 조건

- `/people/{memberId}` URL 사용
- Guest와 Member의 정보 노출 범위 차이 구현
- 회원카드는 쇼케이스 느낌으로 표시
- 마이페이지 상단에 이번 달 참여 현황 표시 준비

---

# Phase 6. Monthly Action Plan, Reflection, Participation

## 목적

커뮤니티의 월간 참여 규칙과 개인 성장 기록을 구현한다.

## 관련 문서

- PRD-004
- TSD-002

## Backend 구현 범위

### Monthly Action Plan

- 월 1개 작성
- 자유 입력
- 수정 가능
- soft delete
- Member 전용 공개

### Monthly Reflection

- 월간 회고 슬롯 제공
- 실제 저장 시에만 회고 작성 이벤트 생성
- 작성 안 해도 문제 없음
- 참여 현황에 반영하지 않음
- Member 전용 공개

### Participation

참여 완료 조건:

```text
해당 월 독서기록 1건 이상
OR
해당 월 실행계획 1건 이상
```

미참여:

```text
투썸 아메리카노 1잔 후원 대상
```

- 신규 가입자는 공식 오픈/가입 다음 달부터 계산
- soft delete된 독서기록/실행계획은 계산에서 제외
- Admin 월별 참여 현황 조회
- Admin 미참여자 CSV 다운로드
- Admin 운영 메모

## Frontend 구현 범위

- 실행계획 작성/수정
- 월간 회고 작성/수정
- 마이페이지 참여 현황 카드
- Admin 참여 현황 화면

## 완료 조건

- 독서기록 또는 실행계획 중 하나만 있어도 참여 완료
- 월간 회고는 참여 현황에 영향 없음
- 미참여자 목록 정확히 계산
- CSV 다운로드 가능

---

# Phase 7. Meetings & Small Meetings

## 목적

정기모임과 소소모임을 구현한다.

## 관련 문서

- PRD-003
- PRD-004
- TSD-002

## Backend 구현 범위

### Scheduler

매월 1일 00:10 KST에 해당 월 정기모임 2개 자동 생성.

- 월간 독서기록 모임
  - 매월 2번째 일요일 오전 10시
- 월간 실행계획 모임
  - 매월 4번째 일요일 오전 10시

중복 생성 금지.

### Meeting

- 모임 목록
- 모임 상세
- 참석하기
- 참석 취소
- 정원 제한
- 모임 상태
  - SCHEDULED
  - HELD
  - CANCELED
  - HIDDEN
  - DELETED

### Small Meeting

- Active Member 누구나 생성
- 생성자만 수정
- 생성자 삭제 가능
- Admin은 숨김/삭제만 가능

### Admin Regular Meeting Management

- 정기모임 목록
- 정기모임 상세
- 정기모임 운영 정보 수정

Admin은 정기모임 운영 정보만 수정한다.  
소소모임 내용은 직접 수정하지 않는다.

## Frontend 구현 범위

- `/meetings`
- `/meetings/{meetingId}`
- `/meetings/new`
- 모임 상세 참석자 스택 UI
- 소소모임 생성/수정 화면
- Admin 정기모임 관리 화면

## Guest 공개 정책

Guest는 다음만 볼 수 있다.

- 모임명
- 설명
- 일시
- 지역 수준 장소
- 참석자 수
- 아주 작은 원형 프로필 이미지 일부

Guest는 다음을 볼 수 없다.

- 정확한 장소
- 참석자 이름
- 참석자 프로필 이동
- 참석하기 버튼

## 완료 조건

- 정기모임 자동 생성
- Member 참석 가능
- 정원 초과 시 참석 불가
- Guest 공개 범위 제한
- Admin 정기모임 수정 가능
- Admin 소소모임 내용 직접 수정 불가

---

# Phase 8. Meeting Reviews & Images

## 목적

모임 후기와 사진 업로드를 구현한다.

## 관련 문서

- PRD-004
- DESIGN-001
- TSD-002

## Backend 구현 범위

### Image Upload

- 단일 이미지 최대 10MB
- 후기 사진 최대 10장
- 전체 권장 업로드 용량 50MB 이하
- 가능하면 WebP 변환, 지원 환경이 없으면 리사이즈된 JPEG 저장
- 리사이징
- 원본 미저장
- Supabase Storage 저장
- 향후 storage provider 교체 가능하도록 인터페이스 구조

### Meeting Review

- Active Member는 시스템 참석 여부와 관계없이 후기 작성 가능
- 후기 생성
- 후기 수정: 작성자만
- 후기 삭제: 작성자 soft delete
- Admin 숨김/복구/삭제
- 사진 최대 10장 연결
- 사진 공개 안내 문구 제공

## Frontend 구현 범위

- `/reviews`
- `/reviews/{reviewId}`
- `/reviews/new`
- 후기 작성 화면
- 이미지 업로드 UI
- 후기 갤러리 UI
- 공개 안내 문구

## 완료 조건

- 참석 버튼을 누르지 않아도 Active Member 후기 작성 가능
- 후기 사진 최대 10장 제한
- Guest가 공개 후기 조회 가능
- Admin은 후기 내용 직접 수정 불가
- 사진 공개 안내 문구 노출

---

# Phase 9. Admin Console

## 목적

운영진이 MVP를 관리할 수 있는 최소 관리자 기능을 구현한다.

## 관련 문서

- PRD-004
- OPS-001
- TSD-002

## 구현 범위

### Admin Dashboard

- 회원 수
- 독서기록 수
- 모임 수
- 후기 수
- 이번 달 참여 완료/미참여 수

### Member Management

- 회원 목록
- 회원 상세
- 비활성화
- 재활성화
- 참여 시작월 조정

### Invite Code

- 활성 초대코드 1개 관리
- 변경 시 기존 코드 즉시 무효화

### Tags

- 관심 분야 태그 관리

### Recommended Books

- 이달의 추천책 3~5권 관리
- 추천 이유
- 노출 순서
- 시작/종료월

### Participation

- 월별 참여 현황
- 미참여자 목록
- CSV 다운로드
- 운영 메모

### Content Moderation

- 독서기록 숨김/복구/삭제
- 소소모임 숨김/삭제
- 후기 숨김/복구/삭제

### Meeting Management

- 정기모임 운영 정보 수정
- 모임 상태 변경

## 완료 조건

- Admin 권한으로만 접근 가능
- 운영진 5명이 사용할 수 있는 최소 기능 제공
- 콘텐츠 직접 수정 범위는 PRD 정책을 지킴

---

# Phase 10. Data Migration Tools

## 목적

기존 소모임/노션/블로그 데이터 전체 이관을 준비한다.

## 관련 문서

- OPS-001

## 구현 범위

### Import Template

CSV 또는 Google Sheet 기반 이관 템플릿.

대상:

- 기존 독서기록
- 기존 모임 후기
- 기존 가입인사
- 기존 회원 매핑

### Import Script

`scripts/import/` 아래에 실행 스크립트 제공.

가능하면 dry-run 모드를 제공한다.

```text
--dry-run
--commit
```

### Import Policy

- 기존 독서기록은 가능한 전체 이관
- 작성자 매핑 완료 시 ACTIVE
- 작성자 미매핑/온보딩 미완료 시 HIDDEN
- 기존 모임 후기 사진은 운영진이 선택한 최대 10장만 이관
- 기존 가입인사는 이관하되 Guest 공개 금지
- 기존 소모임/노션 원본 링크는 보존하지 않음
- 개인 블로그 URL은 독서기록 핵심 데이터이므로 보존

## 완료 조건

- CSV 기반 import 가능
- dry-run 결과 확인 가능
- member 매핑 실패 항목 식별 가능
- 기존 링크 보존 정책 준수

---

# Phase 11. QA, Security, Release Readiness

## 목적

MVP 출시 전 필수 검증을 완료한다.

## QA 범위

### Auth

- 카카오 로그인
- 초대코드
- 약관 동의
- 온보딩
- 쿠키 인증
- 로그아웃
- 비활성 회원 차단

### Reading Library

- 책 검색
- 직접 책 등록
- 독서기록 작성/수정/삭제
- 평점 선택 입력
- Guest 공개
- Admin 숨김/삭제

### Participation

- 독서기록만 있는 경우 완료
- 실행계획만 있는 경우 완료
- 둘 다 없는 경우 미참여
- 월간 회고는 계산 제외
- soft delete 기록 계산 제외

### Meetings

- 정기모임 자동 생성
- 중복 생성 방지
- 참석/취소
- 정원 초과
- Guest 장소 제한
- 작은 프로필 이미지 공개 범위

### Reviews

- 후기 작성
- 참석 여부 제한 없음
- 이미지 최대 10장
- Guest 공개
- Admin 숨김/삭제

### Admin

- 회원 비활성화/재활성화
- 추천책 관리
- 초대코드 변경
- 참여 현황 CSV
- 콘텐츠 숨김/복구/삭제

### Mobile

- 모바일 하단 탭
- 햄버거 메뉴
- 모바일 웹뷰 터치 영역
- 이미지 업로드
- 로그인 리다이렉트

### Security

- HttpOnly cookie 확인
- Secure cookie 운영 설정
- SameSite=Lax
- Origin/Referer 검증
- CORS whitelist
- Admin API 권한 검증

## 완료 조건

- MVP_CHECKLIST.md의 필수 항목이 모두 체크됨
- 치명적 버그 없음
- 문서와 구현의 주요 불일치 없음
- 로컬 Docker Compose 실행 가능
- 배포 가능한 Docker 이미지 생성 가능

---

## 6. 권장 Git Branch 전략

Phase별로 branch를 나누는 것을 권장한다.

```text
main
└─ develop
   ├─ feature/phase-0-bootstrap
   ├─ feature/phase-1-db
   ├─ feature/phase-2-auth-member
   ├─ feature/phase-3-ui-shell
   ├─ feature/phase-4-reading-library
   ├─ feature/phase-5-growth-profile
   ├─ feature/phase-6-participation
   ├─ feature/phase-7-meetings
   ├─ feature/phase-8-reviews-images
   ├─ feature/phase-9-admin
   ├─ feature/phase-10-import
   └─ release/mvp
```

Codex 작업 시에는 한 번에 main에 직접 커밋하지 않는다.

---

## 7. Phase별 Codex 작업 명령 예시

### Phase 시작 프롬프트 예시

```text
Read AGENTS.md and all documents under docs/.
Then read docs/IMPLEMENTATION_PLAN.md.

Implement Phase 2: Auth, Member, Onboarding only.
Do not implement later phases.
Do not add features outside the MVP scope.
After implementation, run tests and update MVP_CHECKLIST.md.
If a requirement is ambiguous, leave a TODO and record the question.
```

### Phase 종료 검증 프롬프트 예시

```text
Review the implementation against PRD-002, TSD-001, TSD-002, and IMPLEMENTATION_PLAN Phase 2.
List mismatches.
Fix only critical mismatches.
Run tests.
Update MVP_CHECKLIST.md.
```

---

## 8. Codex가 임의로 결정하면 안 되는 것

Codex는 아래 항목을 임의로 변경하지 않는다.

- 인증 방식
- 회원 상태 관리 방식
- 참여 완료 규칙
- 도서 API 제공자
- 이미지 최대 업로드 개수
- 공개/비공개 정책
- Admin 권한 범위
- 정기모임 자동 생성 규칙
- 데이터 이관 공개 정책
- 디자인 키워드
- 컬러 팔레트
- MVP 제외 기능

애매한 경우 TODO를 남기고 Noah에게 질문한다.

---

## 9. MVP 완료 정의

MVP는 다음 조건을 만족할 때 완료로 본다.

```text
1. 기존 회원이 카카오 로그인과 초대코드로 가입할 수 있다.
2. Member가 독서기록을 작성할 수 있다.
3. 책 검색이 실패해도 직접 책 등록으로 기록을 계속할 수 있다.
4. 독서기록 라이브러리와 책 상세를 Guest가 볼 수 있다.
5. 성장하는 사람들 페이지에서 회원 카드와 공개 프로필을 볼 수 있다.
6. Member가 월간 실행계획을 작성할 수 있다.
7. 이번 달 참여 현황이 정확히 계산된다.
8. 정기모임이 자동 생성된다.
9. Member가 소소모임을 만들고 참석할 수 있다.
10. Member가 모임 후기를 작성하고 사진 최대 10장을 올릴 수 있다.
11. Admin이 초대코드, 추천책, 태그, 참여 현황, 콘텐츠 숨김/삭제를 관리할 수 있다.
12. 기존 데이터 전체 이관을 위한 CSV/import 흐름이 준비되어 있다.
13. 모바일 웹뷰에서 핵심 기능을 사용할 수 있다.
14. Docker Compose로 로컬 개발 환경이 실행된다.
15. Backend/Frontend가 Kubernetes-ready 구조를 가진다.
```

---

## 10. 다음 문서

이 문서 다음에는 아래 문서를 작성한다.

```text
MVP_CHECKLIST.md
CODEX_MVP_BUILD_PROMPT.md
```

`MVP_CHECKLIST.md`는 각 Phase의 완료 여부를 체크하는 실행 검증표다.

`CODEX_MVP_BUILD_PROMPT.md`는 Codex에게 실제로 줄 최종 작업 지시문이다.
