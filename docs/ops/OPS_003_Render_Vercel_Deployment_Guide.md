# OPS-003 Render + Vercel 배포 가이드

상태: DRAFT
최종 수정일: 2026-07-04
범위: Render Free 백엔드, Vercel 프론트엔드, Supabase DB/Storage 배포

---

## 1. 배포 구조

Oracle Cloud 가입이 막힌 경우, 임시 운영은 아래 구조로 간다.

```text
Frontend: Vercel
Backend: Render Free Web Service
DB: Supabase PostgreSQL
Storage: Supabase Storage
Domain: 각 서비스 기본 무료 도메인 우선 사용
```

초기 URL 기준:

```text
Frontend: https://growth-archive.vercel.app
Backend: https://growth-archive-api.onrender.com
```

Caddy, Nginx, Docker Compose 배포는 이 구성에서 사용하지 않는다.

---

## 2. 기존 `.env`와 다른 점

기존 `.env`, `.env.example`, `backend/.env.example`, `deploy.env.example`은 목적이 다르다.

```text
.env
로컬 Docker Compose 실행용. 로컬 PostgreSQL, localhost URL, non-secure cookie 기준.

backend/.env.example
백엔드 로컬 실행 템플릿. localhost 기준.

deploy.env.example
한 서버에서 Docker Compose + Caddy로 frontend/backend를 같이 띄울 때 쓰는 템플릿.

Render Environment Variables
Render 백엔드 서비스에 직접 넣는 운영형 환경변수. Supabase DB, Render URL, Vercel URL, Secure cookie 기준.

Vercel Environment Variables
Vercel 프론트엔드 서비스에 직접 넣는 공개 API URL 설정.
```

Render/Vercel 배포에서는 `.env` 파일을 서버에 올리지 않는다. 각 서비스의 Environment Variables 화면에 직접 입력한다.

---

## 3. Render 백엔드 서비스 설정

Render에서 새 Web Service를 만든다.

```text
Source Code: ljh468/growth-archive
Name: growth-archive-api
Language: Docker
Branch: main
Region: Singapore (Southeast Asia)
Root Directory: backend
Dockerfile Path: ./Dockerfile
Instance Type: Free
```

주의:

```text
Root Directory가 backend이면 Dockerfile Path는 ./Dockerfile 이다.
Root Directory를 비우면 Dockerfile Path는 backend/Dockerfile 이다.
```

---

## 4. Render 백엔드 환경변수

Render의 Environment Variables에서 `Add from .env`를 누르고 아래 내용을 붙여넣는다.

`REPLACE_ME`로 시작하는 값은 실제 Supabase/Kakao/JWT 값으로 바꾼다.

```env
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=10000

SUPABASE_DATABASE_URL=jdbc:postgresql://REPLACE_ME_SUPABASE_DB_HOST:5432/REPLACE_ME_SUPABASE_DB_NAME?sslmode=require
SUPABASE_DATABASE_USERNAME=REPLACE_ME_SUPABASE_DB_USERNAME
SUPABASE_DATABASE_PASSWORD=REPLACE_ME_SUPABASE_DB_PASSWORD

FLYWAY_ENABLED=true
FLYWAY_LOCATIONS=classpath:db/migration,classpath:db/demo

CORS_ALLOWED_ORIGINS=https://growth-archive.vercel.app
FRONTEND_BASE_URL=https://growth-archive.vercel.app

JWT_SECRET=REPLACE_ME_LONG_RANDOM_JWT_SECRET_AT_LEAST_32_CHARS
JWT_ACCESS_TOKEN_SECONDS=3600
JWT_REFRESH_TOKEN_SECONDS=1209600
JWT_SECURE_COOKIE=true

KAKAO_REST_API_KEY=REPLACE_ME_KAKAO_REST_API_KEY
KAKAO_CLIENT_SECRET=REPLACE_ME_KAKAO_LOGIN_CLIENT_SECRET
KAKAO_ADMIN_KEY=
KAKAO_WEBHOOK_SECRET=REPLACE_ME_LONG_RANDOM_KAKAO_WEBHOOK_SECRET
KAKAO_REDIRECT_URI=https://growth-archive.vercel.app/api/v1/auth/kakao/callback
KAKAO_TOKEN_URI=https://kauth.kakao.com/oauth/token
KAKAO_USER_INFO_URI=https://kapi.kakao.com/v2/user/me
KAKAO_UNLINK_URI=https://kapi.kakao.com/v1/user/unlink
KAKAO_INITIAL_ADMIN_PROVIDER_IDS=

KAKAO_BOOK_REST_API_KEY=REPLACE_ME_KAKAO_REST_API_KEY
KAKAO_BOOK_SEARCH_ENDPOINT=https://dapi.kakao.com/v3/search/book

INITIAL_INVITE_CODE=test
INITIAL_ADMIN_INVITE_CODE=admin

SUPABASE_URL=https://REPLACE_ME_SUPABASE_PROJECT_REF.supabase.co
SUPABASE_SERVICE_ROLE_KEY=REPLACE_ME_SUPABASE_SERVICE_ROLE_KEY
SUPABASE_STORAGE_BUCKET=images
SUPABASE_STORAGE_S3_ENDPOINT=https://REPLACE_ME_SUPABASE_PROJECT_REF.storage.supabase.co/storage/v1/s3
SUPABASE_STORAGE_S3_REGION=ap-northeast-2
SUPABASE_STORAGE_S3_ACCESS_KEY_ID=REPLACE_ME_SUPABASE_STORAGE_S3_ACCESS_KEY_ID
SUPABASE_STORAGE_S3_SECRET_ACCESS_KEY=REPLACE_ME_SUPABASE_STORAGE_S3_SECRET_ACCESS_KEY
SUPABASE_STORAGE_PUBLIC_BASE_URL=https://REPLACE_ME_SUPABASE_PROJECT_REF.supabase.co/storage/v1/object/public/images
STORAGE_ENVIRONMENT_PREFIX=dev
LOCAL_STORAGE_FALLBACK_ENABLED=false
```

Render 배포 후 아래 URL이 응답해야 한다.

```text
https://growth-archive-api.onrender.com/api/v1/health
```

---

## 5. Vercel 프론트엔드 설정

Vercel에서 새 Project를 만든다.

```text
Git Repository: ljh468/growth-archive
Project Name: growth-archive
Framework Preset: Next.js
Root Directory: frontend
Build Command: npm run build
Install Command: npm install
Output Directory: 비워둠
```

Vercel Environment Variables:

```env
NEXT_PUBLIC_API_BASE_URL=/api/v1
API_INTERNAL_BASE_URL=https://growth-archive-api.onrender.com/api/v1
API_PROXY_TARGET=https://growth-archive-api.onrender.com/api/v1
```

배포 후 아래 URL이 열려야 한다.

```text
https://growth-archive.vercel.app
```

만약 Vercel이 다른 URL을 발급하면, Render 백엔드의 아래 값도 같은 URL로 수정하고 재배포한다.

```env
CORS_ALLOWED_ORIGINS=https://REPLACE_ME_ACTUAL_VERCEL_URL
FRONTEND_BASE_URL=https://REPLACE_ME_ACTUAL_VERCEL_URL
```

---

## 6. Kakao Developers 설정

Vercel/Render URL이 확정되면 Kakao Developers에 아래 값을 등록한다.

```text
Web platform domain:
https://growth-archive.vercel.app

Redirect URI:
https://growth-archive.vercel.app/api/v1/auth/kakao/callback
```

Vercel URL이 다르면 `Web platform domain`도 실제 Vercel URL로 맞춘다.

---

## 7. 배포 순서

```text
1. develop -> main PR merge
2. Render 백엔드 Web Service 생성
3. Render 백엔드 환경변수 입력
4. Render 백엔드 배포
5. 백엔드 health 확인
6. Vercel 프론트엔드 Project 생성
7. Vercel 프론트엔드 환경변수 입력
8. Vercel 프론트엔드 배포
9. Kakao Developers URL 확인
10. 로그인, 온보딩, 이미지 업로드 smoke test
```

---

## 8. Smoke Test

```text
Backend health:
https://growth-archive-api.onrender.com/api/v1/health

Frontend:
https://growth-archive.vercel.app

확인 항목:
- 공개 홈 접속
- Kakao 로그인
- 초대코드 test
- 온보딩 프로필 저장
- 프로필 이미지 업로드
- 독서기록 작성
- 모임 후기 이미지 업로드
- 운영관리 접근
```

---

## 9. 주의사항

Render Free는 비활성 상태가 지속되면 백엔드가 sleep 상태로 들어갈 수 있다.

```text
첫 요청이 느릴 수 있다.
처음 접속 시 30초 이상 걸릴 수 있다.
Spring Boot가 메모리 512MB에서 빡빡하면 백엔드만 유료/저가 VM으로 옮긴다.
```

정식 운영 전환 시에는 `FLYWAY_LOCATIONS`에서 demo seed를 제거한다.

```env
FLYWAY_LOCATIONS=classpath:db/migration
```
