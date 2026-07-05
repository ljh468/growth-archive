# OPS-002 개발 배포 및 도메인 구성

상태: ALTERNATIVE_PLAN
최종 수정일: 2026-07-05
범위: Vercel/Render 임시 운영 이후의 비용 최소화 이전 전략, DuckDNS 무료 도메인, 로컬 데스크톱 서버 운영, 필요 시 AWS 프리티어 보조 사용

---

## 1. 문서 위치와 추천 방향

현재 dev 배포 기준은 `OPS_003_Render_Vercel_Deployment_Guide.md`의 Vercel + Render Free + Supabase 구조다.

본 문서는 Render sleep, 장기 비용, 개인 서버 이전을 고려할 때 검토할 후속 대안이다. 즉, 지금 당장 따라야 하는 기본 배포 가이드가 아니라 로컬 데스크톱 서버/DuckDNS/AWS 보조안을 정리한 이전 계획이다.

Growth Archive는 소수 인원이 사용하는 프라이빗 서비스다. 장기적으로 비용이 거의 들지 않는 방향을 유지하려면 가능한 한 로컬 데스크톱 서버와 무료 DuckDNS를 중심으로 이전할 수 있다.

추천 경로:

```text
1단계 - 후속 비용 최소 이전안
로컬 데스크톱 서버
+ Docker Compose
+ Caddy 리버스 프록시
+ DuckDNS 무료 도메인
+ Supabase DB
+ Supabase Storage

2단계 - 집 네트워크 인바운드가 막힐 때만 검토
AWS EC2 Free Tier
+ Docker Compose
+ Caddy 리버스 프록시
+ DuckDNS 무료 도메인
+ Supabase DB/Storage 유지

3단계 - 필요해졌을 때만
가장 저렴하고 갱신비가 낮은 실제 도메인 구매
```

프론트엔드와 백엔드는 같은 서버에서 컨테이너로 실행한다.

```text
frontend container -> 내부 포트 3000
backend container  -> 내부 포트 8080
caddy container    -> 외부 포트 80/443
```

외부 접근 주소:

```text
https://growth-archive.duckdns.org      -> frontend
https://api-growth-archive.duckdns.org  -> backend
```

프론트엔드와 백엔드는 DuckDNS 서브도메인을 분리한다. 이렇게 해야 CORS, Kakao Redirect URI, 로그, 이후 실제 도메인 이전이 명확하다.

---

## 2. 비용 및 IP 판단

### 2.1 가장 싼 방향은 로컬 데스크톱 서버다

현재 단계에서 가장 싼 방향은 로컬 데스크톱 서버에 프론트엔드와 백엔드를 함께 올리는 것이다.

```text
로컬 데스크톱 서버
+ Docker Compose
+ Caddy
+ DuckDNS
+ Supabase Free
```

이 경우 추가 클라우드 서버 비용은 없다. 단, 집 인터넷 환경에서 80/443 포트 인바운드가 가능해야 한다.

### 2.2 탄력적 IP는 필수가 아니다

이 프로젝트에서 AWS Elastic IP는 필수가 아니다.

초기에는 EC2 인스턴스에 자동 할당되는 Public IPv4를 그대로 사용한다.

```text
EC2 자동 Public IPv4 -> DuckDNS 레코드
```

단, EC2를 stop/start 하면 Public IPv4가 바뀔 수 있다. 이 경우 DuckDNS IP를 다시 갱신하면 된다.

### 2.3 Public IPv4는 과금 대상이다

AWS는 현재 Elastic IP 여부와 관계없이 public IPv4 주소 사용에 과금한다. 즉, Elastic IP를 만들지 않아도 EC2에 public IPv4가 붙어 있으면 비용이 발생할 수 있다.

운영 판단:

```text
Elastic IP는 필요 없다.
하지만 AWS public IPv4 사용은 완전 무료가 아니다.
```

엄격하게 0원에 가깝게 운영하려면 AWS EC2를 상시 운영하는 것은 우선순위가 낮다. AWS EC2는 집 네트워크 인바운드가 막히거나 로컬 서버 운영이 불안정할 때만 보조안으로 검토한다.

참고:

- AWS VPC pricing: https://aws.amazon.com/vpc/pricing/
- DuckDNS: https://www.duckdns.org/about.jsp

---

## 3. 우선 적용 구조: 로컬 데스크톱 서버

```text
Internet
  |
DuckDNS
  |
Home Public IP
  |
Router port forwarding :80/:443
  |
Local desktop server
  |
Caddy :80/:443
  |--------------------|
  |                    |
frontend:3000          backend:8080
Next.js                Spring Boot
  |                    |
  |                    Supabase PostgreSQL
  |                    Supabase Storage
```

로컬 데스크톱 서버 구성:

```text
Docker
Docker Compose
Caddy
frontend container
backend container
```

공유기 포트포워딩:

```text
80  -> desktop server 80
443 -> desktop server 443
```

로컬 서버 운영 조건:

- 데스크톱 서버가 절전 모드로 들어가지 않아야 한다.
- 집 인터넷 public IP가 DuckDNS에 주기적으로 갱신되어야 한다.
- 통신사가 80/443 인바운드를 막지 않아야 한다.
- 공유기 포트포워딩이 유지되어야 한다.

---

## 4. 보조안: AWS EC2 구조

```text
Internet
  |
DuckDNS
  |
EC2 Public IPv4
  |
Caddy :80/:443
  |--------------------|
  |                    |
frontend:3000          backend:8080
Next.js                Spring Boot
  |                    |
  |                    Supabase PostgreSQL
  |                    Supabase Storage
```

AWS 리소스:

```text
EC2 instance: Free Tier eligible instance
Security Group inbound:
  - 22: 내 IP만 허용
  - 80: 0.0.0.0/0 허용
  - 443: 0.0.0.0/0 허용
Elastic IP: 사용하지 않음
RDS: 사용하지 않음
S3: 사용하지 않음
Load Balancer: 사용하지 않음
```

DB와 이미지 업로드는 Supabase를 사용한다.

AWS EC2는 아래 경우에만 사용한다.

- 집 인터넷에서 80/443 인바운드가 막힌다.
- 로컬 데스크톱 서버를 당장 항상 켜둘 수 없다.
- 외부 테스트를 빠르게 해야 한다.

---

## 5. DuckDNS 설정

DuckDNS에서 서브도메인 2개를 만든다.

```text
growth-archive.duckdns.org
api-growth-archive.duckdns.org
```

두 서브도메인 모두 현재 서버의 public IP를 바라보게 설정한다.

```text
로컬 데스크톱 운영 시: 집 인터넷 Public IP
AWS EC2 보조 운영 시: EC2 Public IPv4
```

수동 갱신:

```text
DuckDNS dashboard -> 두 서브도메인의 IP를 현재 서버 Public IP로 갱신
```

서버에서 DuckDNS 갱신 명령:

```bash
curl "https://www.duckdns.org/update?domains=growth-archive,api-growth-archive&token={DUCKDNS_TOKEN}&ip="
```

권장 cron:

```cron
*/10 * * * * curl -fsS "https://www.duckdns.org/update?domains=growth-archive,api-growth-archive&token={DUCKDNS_TOKEN}&ip=" >/tmp/duckdns.log 2>&1
```

주의:

- DuckDNS token은 절대 커밋하지 않는다.
- 서버 public IP가 바뀌었는데 DuckDNS가 갱신되지 않으면 서비스 접속이 실패한다.

---

## 6. Caddy 리버스 프록시

저장소 루트의 `Caddyfile`을 사용한다.

```caddyfile
{$FRONTEND_HOST:growth-archive.duckdns.org} {
    reverse_proxy frontend:3000
}

{$BACKEND_HOST:api-growth-archive.duckdns.org} {
    reverse_proxy backend:8080
}
```

Caddy는 조건이 맞으면 HTTPS 인증서를 자동 발급한다.

필수 조건:

- DuckDNS가 현재 서버 public IP를 바라본다.
- 로컬 운영 시 공유기에서 80, 443 포트포워딩이 되어 있다.
- AWS 운영 시 보안 그룹에서 80, 443 포트가 열려 있다.
- Caddy 컨테이너가 frontend/backend 컨테이너에 접근할 수 있다.

---

## 7. Docker Compose 배포 구조

배포용 Compose는 저장소 루트의 `docker-compose.deploy.yml`을 사용한다. 이 Compose에는 아래 서비스만 둔다.

```text
caddy
frontend
backend
```

이 단계에서는 PostgreSQL을 EC2에 올리지 않는다.

```text
backend -> Supabase PostgreSQL 연결
backend -> Supabase Storage 이미지 업로드
```

내부 URL:

```text
frontend -> backend 내부 호출: http://backend:8080/api/v1
caddy -> frontend: http://frontend:3000
caddy -> backend: http://backend:8080
```

외부 URL:

```text
frontend public: https://growth-archive.duckdns.org
backend public:  https://api-growth-archive.duckdns.org/api/v1
```

배포 실행 기준:

```bash
docker compose --env-file .env.deploy -f docker-compose.deploy.yml build
docker compose --env-file .env.deploy -f docker-compose.deploy.yml up -d
```

`docker-compose.deploy.yml`은 프론트엔드 빌드 단계에 `NEXT_PUBLIC_API_BASE_URL`을 build arg로 주입한다. 이 값은 브라우저 번들에 포함되므로 API 도메인이 바뀌면 프론트 이미지를 다시 빌드해야 한다.

보안 기준:

- `backend`만 전체 `.env.deploy`를 `env_file`로 받는다.
- `frontend`는 `NEXT_PUBLIC_API_BASE_URL`, `API_INTERNAL_BASE_URL`만 받는다.
- `caddy`는 `FRONTEND_HOST`, `BACKEND_HOST`만 받는다.
- Supabase service role key, Supabase S3 secret, Kakao client secret, JWT secret은 frontend/caddy 컨테이너 환경변수로 넘기지 않는다.

템플릿으로 Compose 설정만 검증할 때는 실제 secret 파일 대신 아래처럼 실행할 수 있다.

```bash
DEPLOY_ENV_FILE=deploy.env.example docker compose --env-file deploy.env.example -f docker-compose.deploy.yml config
```

---

## 8. 백엔드 환경변수

서버의 비공개 `.env.deploy` 파일 또는 배포 환경변수로 설정한다. 저장소의 `deploy.env.example`은 템플릿이며 secret 값을 넣지 않는다.

```env
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080

FRONTEND_HOST=growth-archive.duckdns.org
BACKEND_HOST=api-growth-archive.duckdns.org

SUPABASE_DATABASE_URL=jdbc:postgresql://{supabase-host}:{port}/{database}?sslmode=require
SUPABASE_DATABASE_USERNAME={supabase-db-user}
SUPABASE_DATABASE_PASSWORD={supabase-db-password}

FLYWAY_ENABLED=true
FLYWAY_LOCATIONS=classpath:db/migration,classpath:db/demo

CORS_ALLOWED_ORIGINS=https://growth-archive.duckdns.org
FRONTEND_BASE_URL=https://growth-archive.duckdns.org
NEXT_PUBLIC_API_BASE_URL=https://api-growth-archive.duckdns.org/api/v1
API_INTERNAL_BASE_URL=http://backend:8080/api/v1

JWT_SECRET={long-random-secret}
JWT_ACCESS_TOKEN_SECONDS=3600
JWT_REFRESH_TOKEN_SECONDS=1209600
JWT_SECURE_COOKIE=true

KAKAO_REST_API_KEY={kakao-rest-api-key}
KAKAO_CLIENT_SECRET={kakao-login-client-secret}
KAKAO_ADMIN_KEY={kakao-admin-key-if-used}
KAKAO_WEBHOOK_SECRET={random-webhook-secret}
KAKAO_REDIRECT_URI=https://api-growth-archive.duckdns.org/api/v1/auth/kakao/callback
KAKAO_INITIAL_ADMIN_PROVIDER_IDS={comma-separated-kakao-provider-ids-if-needed}

KAKAO_BOOK_REST_API_KEY={kakao-rest-api-key}
KAKAO_BOOK_SEARCH_ENDPOINT=https://dapi.kakao.com/v3/search/book

INITIAL_INVITE_CODE=test
INITIAL_ADMIN_INVITE_CODE=admin

SUPABASE_URL=https://{project-ref}.supabase.co
SUPABASE_SERVICE_ROLE_KEY={supabase-service-role-key}
SUPABASE_STORAGE_BUCKET=images
SUPABASE_STORAGE_S3_ENDPOINT=https://{project-ref}.storage.supabase.co/storage/v1/s3
SUPABASE_STORAGE_S3_REGION=ap-northeast-2
SUPABASE_STORAGE_S3_ACCESS_KEY_ID={supabase-s3-access-key-id}
SUPABASE_STORAGE_S3_SECRET_ACCESS_KEY={supabase-s3-secret-access-key}
SUPABASE_STORAGE_PUBLIC_BASE_URL=https://{project-ref}.supabase.co/storage/v1/object/public
STORAGE_ENVIRONMENT_PREFIX=dev
LOCAL_STORAGE_FALLBACK_ENABLED=false

DB_MAX_POOL_SIZE=5
DB_MIN_IDLE=0
DB_IDLE_TIMEOUT_MS=30000
DB_MAX_LIFETIME_MS=600000
DB_CONNECTION_TIMEOUT_MS=10000
```

주의:

- `FLYWAY_LOCATIONS=classpath:db/migration,classpath:db/demo`는 dev DB에 더미 데이터를 넣는다.
- 실제 운영 전환 시에는 `FLYWAY_LOCATIONS=classpath:db/migration`만 사용한다.
- JWT secret, Kakao secret, Supabase service role key, Supabase S3 secret은 절대 커밋하지 않는다.
- 실제 서버의 `.env.deploy`는 저장소 밖에 보관하거나, 저장소 루트에 둘 경우 `.gitignore`로 커밋되지 않는지 확인한다.
- 서버 파일 권한은 가능하면 owner만 읽을 수 있게 `chmod 600 .env.deploy`로 제한한다.

---

## 9. 프론트엔드 환경변수

프론트엔드 Docker build/runtime에서 사용한다.

```env
NEXT_PUBLIC_API_BASE_URL=https://api-growth-archive.duckdns.org/api/v1
API_INTERNAL_BASE_URL=http://backend:8080/api/v1
```

주의:

- `NEXT_PUBLIC_API_BASE_URL`은 브라우저 코드에 포함된다.
- 이 값이 바뀌면 프론트엔드 이미지를 다시 빌드해야 한다.
- `API_INTERNAL_BASE_URL`은 서버 사이드 Next.js 코드에서 내부 백엔드 호출에 사용한다.
- 현재 Dockerfile은 `NEXT_PUBLIC_API_BASE_URL`을 build arg로 받는다.

---

## 10. Kakao Developers 설정

DuckDNS 단계에서는 아래 값을 등록한다.

Web platform domain:

```text
https://growth-archive.duckdns.org
```

Redirect URI:

```text
https://api-growth-archive.duckdns.org/api/v1/auth/kakao/callback
```

도메인이 바뀌면 Kakao Developers와 백엔드/프론트 환경변수를 함께 수정해야 한다.

---

## 11. Supabase 운영 기준

Supabase는 아래 용도로 계속 사용한다.

- PostgreSQL 데이터 저장
- 이미지 업로드 저장

Storage object prefix:

```text
dev/profile/...
dev/book-cover/...
dev/reading-record/...
dev/meeting/...
dev/meeting-review/...
```

버킷명:

```text
images
```

배포 환경에서는 local fallback을 끈다.

```env
LOCAL_STORAGE_FALLBACK_ENABLED=false
```

Supabase Storage 버킷 확인:

```text
Bucket name: images
Public bucket: enabled
Recommended MIME type: image/webp
Single upload limit: app 기준 10MB, Supabase bucket limit은 그 이상 또는 동일하게 설정
Public base URL: https://{project-ref}.supabase.co/storage/v1/object/public/images
S3 endpoint: https://{project-ref}.storage.supabase.co/storage/v1/s3
Region: ap-northeast-2
```

브라우저에서 이미지를 WebP로 리사이징/압축한 뒤 업로드하므로, 운영 버킷은 WebP 업로드가 막히지 않아야 한다.

---

## 12. 로컬 데스크톱 서버 운영 기준

로컬 데스크톱 서버에서도 컨테이너 경계를 유지한다.

```text
desktop server
├─ caddy
├─ frontend
└─ backend
```

준비 순서:

1. 데스크톱 서버에 Docker와 Docker Compose를 설치한다.
2. 비공개 `.env.deploy` 파일을 데스크톱 서버에 만든다.
3. 공유기 포트포워딩을 설정한다.
   - 80 -> 데스크톱 서버
   - 443 -> 데스크톱 서버
4. DuckDNS IP를 집 인터넷 Public IP로 지정한다.
5. Compose stack을 실행한다.
6. Caddy HTTPS 인증서 발급/갱신을 확인한다.
7. Smoke test를 진행한다.

로컬 서버 리스크:

- 통신사가 80/443 인바운드를 막을 수 있다.
- 집 Public IP가 바뀔 수 있다.
- 공유기 포트포워딩 설정이 필요하다.
- 데스크톱 절전/재부팅 시 서비스가 내려간다.
- 백업 정책을 별도로 세워야 한다.

인바운드 접속이 막히면 그때 아래 대안을 검토한다.

```text
Cloudflare Tunnel
Tailscale Funnel
저가 VPS reverse proxy
```

현재 단계에서는 위 대안을 먼저 도입하지 않는다.

AWS EC2는 로컬 데스크톱 서버 운영이 막힐 때만 보조안으로 사용한다. EC2를 사용하게 되더라도 Elastic IP는 만들지 않고, 자동 Public IPv4와 DuckDNS 갱신으로 시작한다.

---

## 13. 이후 저가 실제 도메인

DuckDNS는 초기 개발/검증에는 충분하지만, 실제 멤버 대상 운영에는 부족하다.

나중에는 갱신비까지 저렴한 실제 도메인을 구매한다.

도메인 구매 기준:

```text
첫해 가격만 보지 않는다.
갱신 가격을 반드시 확인한다.
이상하게 싼 도메인은 갱신비가 비쌀 수 있다.
구매 후 DNS는 Cloudflare에서 관리한다.
```

이후 도메인 매핑:

```text
www.{domain}  -> frontend
api.{domain}  -> backend
```

이때도 Caddy와 Docker Compose 구조는 그대로 유지할 수 있다.

---

## 14. 배포 순서

1. 로컬 데스크톱 서버에 Docker와 Docker Compose를 설치한다.
2. 데스크톱 절전 모드를 끄고, 재부팅 후 자동 실행 정책을 정한다.
3. DuckDNS 서브도메인 2개를 만든다.
   - `growth-archive`
   - `api-growth-archive`
4. 두 DuckDNS 레코드를 집 인터넷 Public IP로 지정한다.
5. 공유기 포트포워딩을 설정한다.
   - 80 -> 데스크톱 서버
   - 443 -> 데스크톱 서버
6. `deploy.env.example`을 기준으로 `.env.deploy` 값을 채운다.
7. `docker-compose.deploy.yml`로 컨테이너를 빌드하고 실행한다.
8. Caddy HTTPS 인증서 발급을 확인한다.
9. Kakao Developers 설정을 수정한다.
10. Smoke test를 진행한다.

로컬 인바운드가 막혀서 접속이 실패할 때만 AWS EC2 보조안을 진행한다.

---

## 15. Smoke Test 체크리스트

공개 페이지:

- `https://growth-archive.duckdns.org` 접속 성공
- `https://api-growth-archive.duckdns.org/api/v1/health` 응답 성공
- 독서기록 라이브러리 접속 성공
- 모임 접속 성공
- 사람들 접속 성공
- 모임 후기 접속 성공
- 소개 페이지 접속 성공

인증:

- Kakao 로그인 redirect 성공
- 기존 회원 로그인 성공
- 신규 회원 초대코드 플로우 성공
- 초대코드 `test`는 MEMBER 권한 생성
- 초대코드 `admin`은 ADMIN 권한 생성
- 로그아웃 시 Growth Archive 세션 쿠키 제거

멤버 기능:

- 온보딩 프로필 저장 성공
- 프로필 이미지 업로드 성공
- 독서기록 생성 성공
- 직접 책 등록 성공
- Kakao 책 검색 성공
- 실행계획 생성 성공
- 모임 후기 이미지 업로드 성공

운영 기능:

- 운영진만 운영관리 접근 가능
- 추천책 등록/숨김/복구 가능
- 회원 비활성화/재활성화 가능
- 회원 권한 변경 가능
- 참여 현황 수동 처리 가능
- 카카오톡 미참여자 메시지 복사 가능

보안:

- 쿠키는 HttpOnly
- HTTPS에서 Secure cookie 사용
- CORS는 프론트 DuckDNS 도메인만 허용
- secret 값은 저장소에 없음

---

## 16. 남은 결정 사항

실제 운영 전 결정할 것:

- 최종 DuckDNS 서브도메인 이름
- 집 인터넷 80/443 인바운드 가능 여부
- AWS EC2 보조안이 필요한지 여부
- 구매할 저가 실제 도메인 후보
- Supabase 백업 정책
- Supabase Storage 고아 이미지 정리 스케줄
- DB/Storage를 나중에 자체 서버로 옮길지 여부
