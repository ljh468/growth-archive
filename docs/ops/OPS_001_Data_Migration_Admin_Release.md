# OPS-001. Data Migration / Admin Operations / Release Checklist

Project: 부자습관 만들기 - Growth Archive  
Version: 1.0 FINAL  
Status: Final  
Owner: Noah  
Last Updated: 2026-06-22

---

## 0. 문서 목적

이 문서는 Growth Archive MVP를 실제 운영 가능한 서비스로 출시하기 위해 필요한 **기존 데이터 이관, 관리자 운영 업무, 릴리즈 체크리스트, QA 기준**을 정의한다.

Growth Archive는 빈 서비스가 아니라, 이미 소모임 앱과 노션, 개인 블로그에 쌓인 기록을 웹으로 옮기는 프로젝트다. 따라서 MVP 성공은 기능 구현뿐 아니라 기존 기록을 얼마나 빠짐없이 옮기고, 운영진이 얼마나 쉽게 관리할 수 있는지에 달려 있다.

이 문서는 Codex가 MVP 구현을 진행할 때 운영/이관/출시 기준으로 참조한다.

---

## 1. 관련 문서

이 문서는 아래 문서들을 기준으로 작성한다.

```text
docs/prd/PRD_001_Growth_Archive.md
docs/prd/PRD_002_Users_Auth_Permissions_Onboarding.md
docs/prd/PRD_003_IA_User_Flows_Screen_Requirements.md
docs/prd/PRD_004_Core_Feature_Requirements.md
docs/design/DESIGN_001_Design_System.md
docs/tech/TSD_001_Architecture_DB_ERD.md
docs/tech/TSD_002_API_Specification.md
```

문서 간 충돌이 있을 경우 해석 우선순위는 다음과 같다.

```text
1. PRD-001 제품 비전
2. PRD-002 사용자/권한
3. PRD-004 핵심 기능 요구사항
4. TSD-001 DB/ERD
5. TSD-002 API
6. OPS-001 운영/이관/릴리즈
```

---

## 2. 결정 로그

| 항목 | 최종 결정 |
|---|---|
| 기존 독서기록 공개 정책 | 작성자 매핑/온보딩 완료된 기존 독서기록은 모두 ACTIVE |
| 작성자 미매핑 기록 | 작성자가 온보딩 완료 및 member_id 매핑되기 전까지 HIDDEN |
| 기존 모임 후기 사진 | 운영진이 직접 선택한 최대 10장만 공개 이관 |
| MVP 출시 전 이관 범위 | 최소 샘플이 아니라, 이관 대상 전체 이관 |
| 기존 회원 온보딩 | 기존 회원도 카카오 로그인 → 초대코드 → 약관 동의 → 온보딩 진행 |
| 기존 회원 참여 현황 시작월 | 서비스 공식 오픈 다음 달부터 계산 |
| 기존 가입인사 | 크롤링/수집 후 이관. 단 Guest 공개 자료가 아니라 회원/운영 목적 데이터로 관리 |
| 기존 모임 후기 사진 10장 초과 | 자동 선택 금지. 운영진이 직접 고른 최대 10장만 이관 |
| 이관 템플릿 | Google Sheet 또는 CSV로 관리. 최종 import는 CSV 기준 |
| 기존 소모임/노션 원본 링크 | 보존하지 않음 |
| 개인 블로그 링크 | 독서기록 핵심 데이터이므로 보존 |

---

## 3. 운영 원칙

### 3.1 간편함 우선

Growth Archive는 기록을 어렵게 만드는 서비스가 아니다.

```text
복잡한 승인 절차보다 간단한 작성
완벽한 데이터보다 기록의 지속성
과한 운영 도구보다 운영진이 실제로 쓸 수 있는 기능
```

을 우선한다.

### 3.2 기존 기록 전체 이관

MVP 출시 전 샘플 데이터만 옮기는 방식이 아니라, 이관 대상으로 정한 기존 기록은 전체 이관을 목표로 한다.

단, 모든 데이터가 무조건 공개된다는 뜻은 아니다.

```text
이관 대상 전체를 정리한다.
작성자 매핑이 가능한 데이터는 서비스 데이터로 등록한다.
공개 정책에 따라 ACTIVE/HIDDEN을 구분한다.
공개하기 부적절하거나 작성자 매핑이 불가능한 데이터는 HIDDEN 또는 SKIPPED로 관리한다.
```

### 3.3 운영진 부담 최소화

운영진 5명이 모두 동일 권한을 가지며, 관리자 기능은 다음 조건을 만족해야 한다.

```text
쉽게 찾을 수 있어야 한다.
잘못 건드려도 복구 가능해야 한다.
회원 기록의 내용을 임의로 수정하지 않아야 한다.
숨김/삭제/메모 중심으로 관리한다.
```

### 3.4 회원 콘텐츠의 주인 존중

Admin은 회원 콘텐츠를 고치는 사람이 아니다.

Admin은 다음을 할 수 있다.

```text
숨김
복구
삭제
운영 메모
부적절 콘텐츠 관리
```

Admin은 다음을 하지 않는다.

```text
회원 독서기록 본문 직접 수정
회원 실행계획 직접 수정
회원 회고 직접 수정
회원 모임 후기 본문 직접 수정
소소모임 내용 직접 수정
```

### 3.5 원본 소스 링크 미보존

기존 소모임/노션 원본 링크는 서비스 DB에 보존하지 않는다.

저장하지 않는 것:

```text
소모임 게시글 원본 URL
노션 원본 URL
소모임 게시글 ID
노션 페이지 ID
external_source
external_id
original_url
```

단, 이관 작업 중 중복 방지를 위해 CSV 내부에서만 사용하는 `migration_key`는 허용한다. `migration_key`는 운영용 임시 식별자이며, 사용자 화면에 노출되지 않는다.

개인 블로그 URL은 독서기록의 본문 원문 링크이므로 저장한다.

```text
blog_url = 저장함
소모임/노션 원본 링크 = 저장하지 않음
```

---

## 4. 운영 역할

## 4.1 Member

일반 회원.

가능한 작업:

```text
독서기록 작성
월간 실행계획 작성
월간 회고 작성
소소모임 생성
모임 참석
모임 후기 작성
내 프로필 수정
내 기록 수정/삭제
```

## 4.2 Admin

운영진. 모든 Admin은 동일 권한을 가진다.

가능한 작업:

```text
초대코드 변경
관심 분야 태그 관리
이달의 추천책 관리
정기모임 운영 정보 수정
소소모임 숨김/삭제
회원 비활성화/재활성화
참여 현황 확인
미참여자 CSV 다운로드
운영 메모 작성
부적절 콘텐츠 숨김/삭제
```

불가능한 작업:

```text
회원 독서기록 내용 직접 수정
회원 실행계획 내용 직접 수정
회원 월간회고 내용 직접 수정
소소모임 내용 직접 수정
모임 후기 내용 직접 수정
```

## 4.3 Owner

MVP에서는 별도 Owner 권한을 두지 않는다. 운영진 권한은 동일하게 관리한다.

단, 실제 배포/서버/DB 접근 권한은 개발자 또는 대표 운영자에게만 제한한다.

---

## 5. 기존 데이터 소스

## 5.1 소모임 앱

이관 대상 데이터:

```text
독서기록 게시글
모임 후기 게시글
가입인사
정기모임 기록
참석자 정보
사진 일부
```

MVP 이관 우선순위:

```text
1순위: 독서기록
2순위: 모임 후기
3순위: 가입인사
4순위: 정기모임 기록
5순위: 참석자 정보
```

댓글은 MVP 이관 대상에서 제외한다. 필요 시 Phase 2에서 검토한다.

## 5.2 노션

이관 대상 데이터:

```text
운영 자료
회원 정보 일부
모임 일정
추천도서 후보
독서기록 일부
실행계획/회고 양식
```

노션 데이터는 정리된 표 형태가 있다면 CSV로 export하여 이관한다.

## 5.3 개인 블로그

독서기록 원문은 개인 블로그에 있다.

MVP에서 저장하는 정보:

```text
블로그 URL
한줄평
평점
대표 사진
책 정보
작성자
작성일
```

블로그 본문 전체를 크롤링하거나 저장하지 않는다.

## 5.4 가입인사

기존 가입인사는 크롤링/수집하여 이관한다.

사용 목적:

```text
기존 회원 식별 보조
가입 당시의 나 데이터 후보
온보딩 프리필 후보
운영진 확인 자료
```

공개 정책:

```text
Guest 공개하지 않음
Member 전체 공개도 기본값 아님
회원 본인과 Admin이 확인 가능한 내부 데이터로 관리
프로필에 반영하려면 회원이 온보딩 또는 프로필 수정 과정에서 확인/수정 후 저장
```

## 5.5 카카오톡 단톡방

MVP 이관 대상에서 제외한다.

이유:

```text
대화 맥락이 복잡함
개인정보 가능성 높음
원문 공개/이관 동의 어려움
데이터 정제 비용 높음
```

다만 향후 좋은 자료/인사이트를 별도 아카이브로 옮기는 기능은 Phase 2에서 검토한다.

---

## 6. 데이터 이관 전략

## 6.1 MVP 이관 방식

MVP에서는 완전 자동화보다 **운영진 검수 가능한 반자동 이관**을 기본으로 한다.

권장 방식:

```text
1. 기존 기록 크롤링/수집
2. Google Sheet 또는 CSV로 목록화
3. 회원 매핑
4. 책 검색/매칭
5. 사진 선별
6. Import Script 실행
7. Admin 검수
8. 공개 상태 확인
```

### 6.1.1 크롤링/수집 원칙

기존 소모임/노션 데이터 수집은 운영진이 접근 권한을 가진 범위에서만 수행한다.

운영 원칙:

```text
운영진 권한 범위 내 데이터만 수집
로그인 계정/비밀번호/토큰을 코드 저장소에 저장하지 않음
크롤링 결과 원본 파일은 비공개 보관
과도한 요청 금지
수집 결과는 CSV로 정리한 뒤 import
```

MVP 구현에서 Codex는 실제 소모임 계정 정보를 알 수 없으므로, 크롤러는 다음 둘 중 하나로 구현한다.

```text
1. 운영자가 수동으로 export/정리한 CSV를 import
2. 별도 로컬 스크립트로 수집 후 CSV 생성
```

서비스 서버가 운영 중에 소모임을 주기적으로 크롤링하지 않는다.

## 6.2 전체 이관 기준

MVP 출시 기준은 최소 샘플 수량이 아니다.

```text
이관 대상으로 정한 기존 독서기록 전체
이관 대상으로 정한 기존 모임 후기 전체
이관 대상으로 정한 기존 가입인사 전체
MVP에서 필요한 정기모임 기록
```

을 이관한다.

단, 아래 데이터는 `SKIPPED` 처리할 수 있다.

```text
작성자 식별 불가
중복 데이터
개인정보/민감정보 포함
사진 공개 부적절
내용이 불완전하여 서비스 데이터로 사용 어려움
운영진이 이관 제외로 판단
```

## 6.3 이관 완료 기준

이관 완료는 단순히 DB에 insert된 상태가 아니다.

아래를 만족해야 한다.

```text
CSV 기준 이관 대상 행이 모두 IMPORTED 또는 SKIPPED 상태
작성자 매핑 가능한 데이터는 member_id 연결 완료
책 매칭 가능한 독서기록은 book_id 연결 완료
사진 선별이 필요한 후기는 운영진 선별 완료
Guest 공개 데이터는 공개 범위 확인 완료
미매핑 작성자 데이터는 HIDDEN 상태 유지
```

---

## 7. 이관 우선순위 및 절차

### Phase A. 회원 매핑

기존 회원 약 40명을 Growth Archive Member와 연결한다.

필요 데이터:

```text
member_alias
real_name(optional)
nickname
display_name
kakao_user_id(after login)
migration_aliases
joined_at
participation_start_month
```

기존 게시글 작성자명과 새 회원 계정을 매핑하기 위해 `migration_aliases` 개념이 필요하다.

예:

```text
소모임 닉네임: 노아
카카오 표시명: Noah
Growth Archive member_id: 42
```

### Phase B. 가입인사 이관

기존 가입인사는 크롤링/수집하여 회원 매핑 자료와 프로필 초기 자료로 활용한다.

이관 데이터 예시:

```text
member_alias
raw_intro_text
parsed_join_reason
parsed_current_concern
parsed_interests_text
parsed_three_year_goal
source_created_at
```

저장 정책:

```text
원본 소모임 링크 저장하지 않음
raw_intro_text는 Member/Admin 전용 내부 데이터
Guest 공개 금지
회원이 확인/수정 후 프로필 필드로 반영 가능
```

### Phase C. 독서기록 이관

기존 독서기록은 가장 먼저 이관한다.

필요 데이터:

```text
migration_key
author_member_alias
member_id
book_title
author
rating
one_line_review
blog_url
image_url(optional)
recorded_at
content_status
```

정책:

```text
작성자 매핑 완료 + 온보딩 완료된 기존 독서기록은 ACTIVE
작성자 미매핑 또는 온보딩 미완료 기록은 HIDDEN
개인 블로그 URL은 보존
소모임/노션 원본 링크는 보존하지 않음
```

권장 CSV 컬럼:

```csv
migration_key,member_alias,book_title,author,rating,one_line_review,blog_url,image_url,recorded_at,content_status
```

### Phase D. 책 매칭

책 매칭 우선순위:

```text
1. ISBN이 있으면 ISBN 기준 매칭
2. ISBN이 없으면 정규화된 제목 + 저자 기준 매칭
3. 카카오 책 검색 API로 후보 검색
4. 후보가 없으면 UNVERIFIED 책으로 임시 등록
```

책 정규화 기준:

```text
공백 제거
대소문자 정리
괄호/부제 일부 정리
저자명 공백 정리
```

직접 등록된 `UNVERIFIED` 책은 향후 스케줄러 또는 Admin 검수로 정리한다.

### Phase E. 모임 후기 이관

필요 데이터:

```text
migration_key
member_alias
member_id
meeting_title
review_title
review_content
selected_image_paths
created_at
content_status
```

정책:

```text
시스템 참석 여부와 관계없이 Active Member는 후기 작성 가능
기존 후기 작성자가 매핑되지 않으면 HIDDEN
본문은 운영진 검수 후 공개 가능
사진은 운영진이 직접 선택한 최대 10장만 공개 이관
```

권장 CSV 컬럼:

```csv
migration_key,member_alias,meeting_title,review_title,review_content,selected_image_paths,created_at,content_status
```

### Phase F. 정기모임 이관

MVP에서는 다음 기록을 우선 이관한다.

```text
기존 후기와 연결되는 정기모임
최근 주요 정기모임
운영진이 중요하다고 판단하는 정기모임
```

정기모임 이관은 후기 연결성을 위해 필요한 범위부터 진행한다.

권장 CSV 컬럼:

```csv
migration_key,meeting_type,title,description,scheduled_at,location_region,exact_location,capacity,fee_amount,status
```

---

## 8. 이관 데이터 상태

이관 작업 관리를 위해 import 작업 단위에는 `import_status`를 둔다.

```text
PENDING
대기 중

MAPPED
회원/책 매핑 완료

IMPORTED
DB 이관 완료

SKIPPED
이관 제외

FAILED
이관 실패

NEEDS_REVIEW
운영진 검수 필요
```

서비스 콘텐츠 공개 상태는 별도의 `content_status`로 관리한다.

```text
ACTIVE
공개

HIDDEN
숨김

DELETED
삭제
```

두 상태는 섞지 않는다.

예:

```text
import_status = IMPORTED
content_status = HIDDEN
```

은 DB 이관은 완료되었지만 사용자에게 공개하지 않는 상태다.

MVP에서 별도 Import Admin UI를 만들 필요는 없지만, import log CSV 또는 import log table로 상태를 남겨야 한다.

---

## 9. 중복 처리 정책

## 9.1 Member 중복

기본 기준:

```text
kakao_user_id가 동일하면 같은 회원
```

이관 중에는 `migration_aliases`로 기존 닉네임과 새 회원을 연결한다.

## 9.2 Book 중복

우선순위:

```text
1. isbn13
2. isbn10
3. normalized_title + normalized_author
```

직접 등록된 `UNVERIFIED` 책은 Admin 또는 스케줄러가 나중에 검증할 수 있어야 한다.

MVP에서는 병합 UI는 필수 아님. 단, DB 구조상 병합 가능하도록 `books.source`, `books.verification_status`를 둔다.

## 9.3 ReadingRecord 중복

중복 판단 기준:

```text
member_id + book_id + blog_url
```

보조 기준:

```text
member_id + normalized_book_title + recorded_at
```

`migration_key`는 import 중복 방지에만 사용하고 사용자 도메인 데이터에는 노출하지 않는다.

## 9.4 MeetingReview 중복

중복 판단 기준:

```text
member_id + review_title + created_at
```

보조 기준:

```text
member_id + meeting_title + normalized_review_content_prefix
```

`migration_key`는 import 중복 방지에만 사용한다.

---

## 10. 공개 범위 및 동의 정책

## 10.1 신규 작성 데이터

Growth Archive에서 신규 작성한 데이터는 각 기능의 공개 정책을 따른다.

```text
독서기록: Guest 공개
블로그 링크: Guest 공개
모임 후기: Guest 공개
성장 프로필 일부: Guest 공개
실행계획: Member 전용
월간회고: Member 전용
```

## 10.2 기존 독서기록

정책:

```text
작성자 매핑/온보딩 완료된 기존 독서기록은 모두 ACTIVE
작성자 미매핑 또는 온보딩 미완료 기록은 HIDDEN
```

기존 독서기록은 개인 블로그 URL 기반으로 운영되므로 공개 가치가 높다. 단, 기록의 주인이 명확해야 하므로 작성자 매핑이 완료되어야 한다.

## 10.3 기존 모임 후기

정책:

```text
본문은 운영진 검수 후 ACTIVE 가능
작성자 미매핑 또는 온보딩 미완료 기록은 HIDDEN
부적절하거나 민감한 내용은 HIDDEN 또는 SKIPPED
```

## 10.4 기존 모임 후기 사진

정책:

```text
운영진이 직접 선택한 최대 10장만 공개 이관
자동으로 첫 10장/최근 10장 선택하지 않음
얼굴이 크게 나온 사진은 특히 신중하게 선택
민감하거나 부적절한 사진은 이관 제외
```

후기 사진은 커뮤니티의 신뢰를 만드는 중요한 자산이지만, 사람의 얼굴과 장소가 노출될 수 있으므로 독서기록보다 더 보수적으로 관리한다.

## 10.5 기존 가입인사

정책:

```text
크롤링/수집하여 이관
Guest 공개 금지
Member 전체 공개도 기본값 아님
회원 본인과 Admin 확인용 내부 데이터로 관리
프로필 반영은 회원 확인/수정 후 진행
```

가입인사는 향후 `가입 당시의 나` 또는 성장 히스토리의 출발점으로 활용할 수 있다.

---

## 11. 기존 회원 온보딩

기존 회원도 신규 회원과 동일하게 온보딩을 진행한다.

```text
카카오 로그인
↓
초대코드 입력
↓
이용약관/개인정보처리방침 동의
↓
온보딩 입력
↓
Member 활성화
```

가입 부담을 낮추기 위해 필수 입력은 최소화한다.

필수:

```text
닉네임
한 줄 소개
관심 분야
50살의 나
공개 표시 방식
이용약관 동의
개인정보처리방침 동의
```

선택:

```text
직업
가입 이유
현재 고민
3년 뒤 목표
프로필 사진
```

기존 가입인사 이관 데이터가 있는 경우, 회원 본인에게 프리필 후보로 보여줄 수 있다. 단, 본인 확인/수정 없이 자동 공개하지 않는다.

---

## 12. 참여 현황 시작월

## 12.1 기존 회원

서비스 공식 오픈월은 적응 기간으로 본다.

```text
서비스 공식 오픈 다음 달부터 참여 현황 계산
```

예:

```text
2026년 7월 공식 오픈
→ 2026년 8월부터 참여 현황 계산
```

## 12.2 신규 회원

신규 회원은 가입 다음 달부터 참여 현황 계산 대상이 된다.

```text
가입월: 적응 기간
가입 다음 달: 참여 현황 계산 시작
```

## 12.3 참여 완료 기준

매월 아래 조건 중 하나 이상 충족하면 참여 완료다.

```text
독서기록 1건 이상 작성
또는
월간 실행계획 1건 이상 작성
```

미참여 기준:

```text
독서기록 0건
그리고
월간 실행계획 0건
```

커피 후원 기준:

```text
투썸 아메리카노 1잔
```

---

## 13. 데이터 이관 템플릿 관리

MVP 이관 작업은 Google Sheet 또는 CSV로 관리한다.

원칙:

```text
작업 중 관리: Google Sheet 가능
최종 import: CSV 기준
import script는 CSV를 입력으로 받음
CSV는 UTF-8 인코딩
같은 CSV를 두 번 실행해도 중복 생성되지 않아야 함
```

### 13.1 Member Mapping CSV

```csv
migration_key,member_alias,real_name,nickname,display_name,joined_at,participation_start_month,note
```

### 13.2 Join Intro CSV

```csv
migration_key,member_alias,raw_intro_text,parsed_join_reason,parsed_current_concern,parsed_interests_text,parsed_three_year_goal,source_created_at
```

### 13.3 Reading Record CSV

```csv
migration_key,member_alias,book_title,author,rating,one_line_review,blog_url,image_url,recorded_at,content_status
```

### 13.4 Meeting Review CSV

```csv
migration_key,member_alias,meeting_title,review_title,review_content,selected_image_paths,created_at,content_status
```

### 13.5 Meeting CSV

```csv
migration_key,meeting_type,title,description,scheduled_at,location_region,exact_location,capacity,fee_amount,status
```

---

## 14. 관리자 운영 기능

## 14.1 초대코드 관리

정책:

```text
활성 초대코드 1개만 유지
새 코드 저장 시 기존 코드 즉시 무효화
대소문자 구분 없이 검증
```

Admin 기능:

```text
현재 코드 조회
초대코드 변경
변경 이력 조회
```

감사 로그:

```text
admin_id
old_code_masked
new_code_masked
changed_at
```

## 14.2 관심 분야 태그 관리

Admin 기능:

```text
태그 생성
태그명 수정
태그 숨김
노출 순서 변경
```

태그 삭제는 MVP에서 soft delete 또는 hidden 처리만 한다.

## 14.3 이달의 추천책 관리

정책:

```text
매월 3~5권 권장
0권이면 섹션 숨김 또는 준비 중 표시
1~2권이면 등록된 만큼 표시
```

Admin 기능:

```text
추천책 등록
추천 이유 작성
노출 순서 지정
노출 시작월/종료월 지정
숨김 처리
```

## 14.4 정기모임 관리

자동 생성:

```text
매월 1일 00:10 KST
해당 월 정기모임 2개 자동 생성
이미 생성되어 있으면 중복 생성하지 않음
```

기본값:

```text
월간 독서기록 모임:
매월 2번째 일요일 오전 10시

월간 실행계획 모임:
매월 4번째 일요일 오전 10시
```

Admin 수정 가능:

```text
일시
지역 수준 장소
정확한 장소
설명
정원
대표 이미지
비용
상태
```

## 14.5 소소모임 관리

Member 생성자는 소소모임을 수정할 수 있다.

Admin은 다음만 가능하다.

```text
숨김
삭제
운영 메모
```

Admin은 소소모임 내용을 직접 수정하지 않는다.

## 14.6 회원 관리

Admin 기능:

```text
회원 목록 조회
회원 상세 조회
회원 비활성화
회원 재활성화
운영 메모 작성
```

MVP 제외:

```text
Admin 권한 UI 부여/회수
회원 완전 삭제
회원 개인정보 직접 수정
```

Admin 권한 부여는 seed 또는 운영 스크립트로 처리한다.

## 14.7 참여 현황 관리

Admin 기능:

```text
월별 참여 현황 조회
미참여자 목록 조회
CSV 다운로드
운영 메모 작성
```

MVP 제외:

```text
결제
정산
자동 알림
카카오톡 발송
```

## 14.8 콘텐츠 관리

Admin 가능:

```text
독서기록 숨김/복구/삭제
모임 후기 숨김/복구/삭제
소소모임 숨김/삭제
부적절 이미지 숨김/삭제
```

Admin 불가:

```text
회원 독서기록 내용 직접 수정
회원 후기 본문 직접 수정
회원 실행계획 직접 수정
회원 회고 직접 수정
```

---

## 15. 월간 운영 루틴

## 15.1 매월 1일

시스템 자동 작업:

```text
00:10 KST 정기모임 2개 자동 생성
월간 회고 슬롯 제공
참여 현황 계산 대상 월 시작
```

운영진 확인:

```text
정기모임 날짜/장소 확인
이달의 추천책 등록
초대코드 변경 필요 여부 확인
```

## 15.2 월중

운영진 확인:

```text
독서기록 등록 현황
실행계획 등록 현황
소소모임 생성 현황
모임 후기 작성 현황
부적절 콘텐츠 여부
```

## 15.3 월말

운영진 확인:

```text
참여 완료 인원
미참여 인원
커피 후원 대상자
운영 메모
```

## 15.4 다음 달 초

운영진 작업:

```text
전월 미참여자 최종 확인
커피 후원 안내
전월 추천책/후기/모임 기록 정리
```

---

## 16. 릴리즈 환경

## 16.1 Local Development

기준:

```text
Docker Compose로 로컬 개발 가능
Backend Dockerfile 제공
Frontend Dockerfile 제공
PostgreSQL은 Supabase 또는 local container 사용 가능
```

필수:

```text
.env.example 제공
README 실행 방법 제공
health check endpoint 제공
```

## 16.2 Staging

권장:

```text
staging.growtharchive.kr
staging API endpoint
staging Kakao OAuth Redirect URL
staging DB
staging Storage bucket
```

MVP에서 별도 staging 서버가 어렵다면 최소한 local/prod 환경변수 분리는 반드시 한다.

## 16.3 Production

방향:

```text
Kubernetes-ready
초기 AWS Kubernetes 계열 검토
최종 개인 서버 Kubernetes/k3s 또는 유사 self-hosted 환경 검토
```

필수 원칙:

```text
Backend stateless
Frontend containerized
이미지는 컨테이너 내부 파일시스템에 저장하지 않음
환경변수로 설정 분리
health check 제공
```

---

## 17. 릴리즈 전 체크리스트

## 17.1 데이터 이관 체크

```text
[ ] 기존 회원 매핑 CSV 작성 완료
[ ] 기존 가입인사 CSV 작성 완료
[ ] 기존 독서기록 CSV 작성 완료
[ ] 기존 모임 후기 CSV 작성 완료
[ ] 기존 후기 사진 최대 10장 선별 완료
[ ] 기존 정기모임 CSV 작성 완료
[ ] 작성자 미매핑 데이터 HIDDEN 처리 확인
[ ] 독서기록 blog_url 보존 확인
[ ] 소모임/노션 원본 링크 미저장 확인
[ ] 이관 대상 전체가 IMPORTED 또는 SKIPPED 상태인지 확인
```

## 17.2 기능 체크

### Auth / Onboarding

```text
[ ] 카카오 로그인 가능
[ ] 초대코드 인증 가능
[ ] 초대코드 오류 메시지 정상
[ ] 이용약관/개인정보 동의 필수
[ ] 온보딩 완료 가능
[ ] 닉네임 중복 방지
[ ] 비활성 회원 접근 제한
```

### Reading Library

```text
[ ] 카카오 책 검색 가능
[ ] 책 검색 결과 선택 가능
[ ] 검색 결과 없을 때 UNVERIFIED 책 직접 등록 가능
[ ] 독서기록 작성 가능
[ ] 평점 없이 저장 가능
[ ] 블로그 URL Guest 공개
[ ] 책 상세에서 독서기록 토글 확인 가능
[ ] 인기 도서 TOP5 노출
[ ] 이달의 추천책 노출
[ ] 기존 독서기록 ACTIVE/HIDDEN 정책 적용 확인
```

### People / Profile

```text
[ ] 성장하는 사람들 목록 조회
[ ] Guest 공개 정보 제한 확인
[ ] Member 전용 정보 제한 확인
[ ] 프로필 URL /people/{memberId} 정상
[ ] 50살의 나 상단 노출
[ ] 최근 기록 3개 노출
```

### Action Plan / Reflection

```text
[ ] 월간 실행계획 작성 가능
[ ] 월 1개 정책 적용
[ ] 월간 회고 작성 가능
[ ] 회고 미작성도 문제 없음
[ ] Member 전용 공개 적용
```

### Participation

```text
[ ] 독서기록 1건 작성 시 참여 완료
[ ] 실행계획 1건 작성 시 참여 완료
[ ] 둘 다 없으면 참여 필요
[ ] 신규 가입자는 다음 달부터 계산
[ ] 기존 회원은 공식 오픈 다음 달부터 계산
[ ] Admin 미참여자 목록 확인
[ ] CSV 다운로드 가능
```

### Meetings

```text
[ ] 정기모임 자동 생성
[ ] 중복 생성 방지
[ ] 소소모임 생성 가능
[ ] 생성자만 소소모임 수정 가능
[ ] Admin은 숨김/삭제 가능
[ ] Guest는 지역 수준 장소만 확인
[ ] Member는 정확한 장소 확인
[ ] 참석하기/취소 가능
```

### Reviews

```text
[ ] Active Member는 시스템 참석 여부와 관계없이 후기 작성 가능
[ ] 후기 사진 최대 10장 제한
[ ] 기존 후기 사진은 운영진 선별 10장 제한 적용
[ ] 사진 공개 안내 문구 표시
[ ] Guest 후기 조회 가능
[ ] Admin 숨김/삭제 가능
```

### Admin

```text
[ ] 초대코드 변경 가능
[ ] 관심 분야 태그 관리 가능
[ ] 추천책 관리 가능
[ ] 정기모임 수정 가능
[ ] 회원 비활성화/재활성화 가능
[ ] 콘텐츠 숨김/삭제 가능
[ ] Admin audit log 기록
```

---

## 18. QA 시나리오

## 18.1 Guest

```text
1. 홈 접속
2. 독서기록 라이브러리 조회
3. 책 상세 조회
4. 블로그 링크 클릭
5. 성장하는 사람들 목록 조회
6. 모임 상세 조회
7. 정확한 장소/참석자 이름이 노출되지 않는지 확인
8. 마이페이지 클릭 시 로그인 유도
```

## 18.2 신규 Member

```text
1. 카카오 로그인
2. 초대코드 입력
3. 약관 동의
4. 온보딩 작성
5. 마이페이지 진입
6. 독서기록 작성
7. 이번 달 참여 완료 확인
```

## 18.3 기존 Member

```text
1. 로그인
2. 기존 가입인사 기반 프리필이 있다면 확인/수정
3. 프로필 수정
4. 실행계획 작성
5. 소소모임 생성
6. 모임 참석
7. 모임 후기 작성
```

## 18.4 Admin

```text
1. 로그인
2. Admin 메뉴 접근
3. 초대코드 변경
4. 추천책 등록
5. 정기모임 수정
6. 미참여자 목록 조회
7. CSV 다운로드
8. 콘텐츠 숨김 처리
9. 이관 데이터 공개 상태 확인
```

---

## 19. 보안 및 개인정보 체크

## 19.1 필수

```text
[ ] JWT는 HttpOnly Secure Cookie 사용
[ ] 운영 환경 HTTPS 필수
[ ] CORS 허용 Origin 제한
[ ] POST/PUT/PATCH/DELETE Origin/Referer 검증
[ ] Supabase Service Role Key 프론트 노출 금지
[ ] Kakao Client Secret 프론트 노출 금지
[ ] .env.example에는 실제 secret 금지
[ ] 크롤링 계정/토큰/쿠키를 저장소에 저장하지 않음
```

## 19.2 개인정보

```text
[ ] 약관 동의 시각 저장
[ ] 개인정보처리방침 동의 시각 저장
[ ] 비활성 회원 개인정보 노출 제한
[ ] 기존 가입인사 Guest 공개 금지
[ ] 기존 사진 공개 전 운영진 선별 확인
```

---

## 20. 백업 및 복구

## 20.1 DB Backup

MVP:

```text
Supabase PostgreSQL 백업 정책 확인
정기 export 또는 dump 절차 문서화
```

운영 전 필수:

```text
restore 테스트 최소 1회
```

## 20.2 이미지 Backup

MVP:

```text
Supabase Storage 사용
중요 이미지 백업 절차 문서화
```

향후 개인 서버 디스크 이전 시:

```text
Persistent Volume 사용
정기 rsync 또는 object storage backup 고려
컨테이너 내부 ephemeral filesystem 저장 금지
```

## 20.3 Rollback

릴리즈 실패 시:

```text
이전 Docker image로 rollback
DB migration rollback 가능 여부 확인
Flyway migration은 destructive 변경 금지
```

---

## 21. Codex 구현 지침

Codex는 이 문서를 구현할 때 다음 원칙을 따른다.

```text
1. 기존 데이터 이관은 import script 단위로 구현한다.
2. import script는 idempotent 해야 한다.
3. 같은 CSV를 두 번 실행해도 중복 데이터가 생기면 안 된다.
4. Admin이 회원 콘텐츠를 직접 수정하는 API를 만들지 않는다.
5. 소모임/노션 원본 링크를 서비스 DB에 저장하지 않는다.
6. 개인 블로그 URL은 독서기록 데이터로 저장한다.
7. 기존 독서기록은 작성자 매핑 완료 시 ACTIVE로 이관한다.
8. 작성자 미매핑 데이터는 HIDDEN으로 둔다.
9. 후기 사진은 최대 10장 제한을 적용한다.
10. 실제 secret 값을 코드에 넣지 않는다.
11. 릴리즈 전 MVP_CHECKLIST.md를 갱신한다.
```

---

## 22. OPS 관점 MVP 완료 기준

OPS 관점에서 MVP가 완료되었다고 판단하는 기준은 다음과 같다.

```text
[ ] 기존 회원 40명 매핑 가능
[ ] 기존 회원 온보딩 가능
[ ] 기존 가입인사 이관 완료
[ ] 기존 독서기록 전체 이관 완료
[ ] 기존 모임 후기 전체 이관 완료
[ ] 기존 후기 사진 운영진 선별 완료
[ ] 이관 대상 중 제외 데이터는 SKIPPED 사유 기록
[ ] 신규 회원 카카오 로그인/초대코드/온보딩 가능
[ ] 독서기록 신규 작성 가능
[ ] 추천책 3권 이상 등록 가능
[ ] 정기모임 자동 생성 가능
[ ] 소소모임 생성 가능
[ ] 모임 후기 작성 가능
[ ] 참여 현황 계산 가능
[ ] 미참여자 CSV 다운로드 가능
[ ] Admin이 주요 운영 업무를 UI에서 수행 가능
[ ] 모바일 웹뷰에서 핵심 플로우 사용 가능
[ ] 운영 배포 전 백업/복구 절차 문서화
```

---

## 23. Resolved Questions

| Question | Decision |
|---|---|
| 기존 독서기록 공개 정책 | 작성자 매핑/온보딩 완료된 기록은 ACTIVE |
| 기존 모임 후기 사진 공개 정책 | 운영진 선별 최대 10장만 공개 이관 |
| MVP 출시 전 최소 이관 수량 | 최소 수량 없음. 이관 대상 전체 이관 |
| 기존 회원 온보딩 방식 | 기존 회원도 동일한 온보딩 진행 |
| 기존 회원 참여 현황 시작월 | 공식 오픈 다음 달부터 계산 |
| 기존 작성자 미온보딩 데이터 | HIDDEN |
| 기존 가입인사 이관 | 크롤링/수집 후 내부 데이터로 이관 |
| 기존 후기 사진 10장 초과 | 운영진이 직접 선택한 최대 10장만 이관 |
| 데이터 이관 템플릿 | Google Sheet/CSV 관리, 최종 import는 CSV 기준 |
| 기존 소모임/노션 링크 보존 | 보존하지 않음 |

---

## 24. Remaining Notes

현재 OPS-001 기준으로 남은 Open Question은 없다.

단, 실제 구현/운영 중 아래 항목은 별도 운영 판단이 필요할 수 있다.

```text
1. 실제 크롤링 방식 및 수집 도구
2. 공식 오픈월 확정
3. 이관 대상 전체 범위 최종 목록
4. 운영진 사진 선별 기준의 세부 룰
5. 개인 서버 이전 시 이미지 저장소 구조
```

위 항목은 OPS-001의 정책을 바꾸는 결정이 아니라, 실행 단계에서 필요한 운영 세부 결정이다.

---

## 25. Revision History

| Version | Date | Description |
|---|---|---|
| 0.1 | 2026-06-22 | Initial draft |
| 1.0 | 2026-06-22 | Finalized migration/publication/onboarding/release policies |
