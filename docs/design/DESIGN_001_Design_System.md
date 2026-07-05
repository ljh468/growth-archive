# DESIGN-001. Design System

# 부자습관 만들기 - Growth Archive

Version: 1.3 Implementation Aligned
Status: IMPLEMENTATION_ALIGNED
Last Updated: 2026-07-05
Related Documents:

- PRD-001. Product Vision & MVP Requirements
- PRD-002. Users / Auth / Permissions / Onboarding
- PRD-003. IA / User Flows / Screen Requirements
- PRD-004. Core Feature Requirements

---

## 1. 문서 목적

이 문서는 `부자습관 만들기 - Growth Archive` MVP의 디자인 방향, UI 원칙, 컴포넌트 기준, 사진/이미지 운영 원칙, 모바일 웹뷰 대응 정책을 정의한다.

Growth Archive는 단순 게시판, 카페형 커뮤니티, 가벼운 SNS 피드가 아니다.
이 서비스는 실제로 읽고, 실행하고, 기록하는 사람들이 쌓아온 성장의 흔적을 고급스럽고 차분하게 보여주는 **성장 아카이브**다.

따라서 디자인은 다음 감정을 만들어야 한다.

```text
진지하게 성장하는 사람들의 기록실
리얼한 사진이 살아있는 프리미엄 커뮤니티
고급스럽고 깔끔한 성장 아카이브
시간이 지나도 품격 있게 남는 개인 성장 히스토리
```

---

## 2. 디자인 목표

### 2.1 Primary Goal

사용자가 사이트에 들어왔을 때 다음 인상을 받아야 한다.

```text
여기는 진짜로 성장하려는 사람들이 모인 곳이구나.
가볍게 떠드는 커뮤니티가 아니라, 각자의 삶을 진지하게 바꿔가는 사람들이 있구나.
나도 이곳에 기록을 남기면 내 성장의 흔적이 품격 있게 쌓이겠다.
```

### 2.2 Secondary Goal

Guest는 커뮤니티의 수준과 분위기를 빠르게 이해할 수 있어야 한다.

Member는 기록 작성과 참여 현황 확인을 부담 없이 수행할 수 있어야 한다.

Admin은 운영에 필요한 정보를 명확하고 실용적으로 확인할 수 있어야 한다.

### 2.3 Design Success Criteria

```text
1. 사이트 첫인상이 게시판처럼 보이지 않아야 한다.
2. 실제 모임과 실제 사람의 온도가 사진으로 느껴져야 한다.
3. 회원카드는 단순 회원 목록이 아니라 성장하는 사람들의 쇼케이스처럼 보여야 한다.
4. 디자인은 고급스럽지만, 기록 작성 UX는 가벼워야 한다.
5. 부자/성공 이미지를 과장하지 않고, 진지한 성장 태도를 보여줘야 한다.
```

---

## 3. 디자인 키워드

Growth Archive의 디자인 키워드는 아래와 같다.

```text
Premium
Editorial
Real-photo-first
Clean
Focused
Archive
Mobile-first
Human-centered
Calm but serious
```

### 3.1 지향하는 느낌

기존의 “Notion 중심” 방향을 유지하되, 사진과 브랜딩의 무게감을 높인다.

```text
Premium Editorial 40%
Trevari-like Community Mood 25%
Notion-like Clean Archive 20%
GitHub Profile-like Growth Record 15%
```

### 3.2 피해야 할 느낌

```text
게시판처럼 보이는 UI
가벼운 카페/동호회 앱 느낌
귀여운 캐릭터/일러스트 중심 UI
과한 이모지/배지/포인트 시스템
랭킹/경쟁 중심 UI
성공팔이/돈자랑 느낌
저품질 스톡 이미지 느낌
너무 화려한 SNS 피드
정보가 빽빽한 관리자툴 느낌의 일반 화면
```

---

## 4. 브랜드 구조

### 4.1 서비스명

```text
부자습관 만들기
Growth Archive
```

### 4.2 Tagline

```text
읽고, 실행하고, 성장한 기록을 남기는 사람들
```

### 4.3 Hero 문구 기본형

```text
부자습관 만들기
Growth Archive

읽고, 실행하고,
성장한 기록을 남기는 사람들
```

### 4.4 브랜드 톤

문장은 짧고 단단해야 한다.
따뜻하되 가볍지 않고, 고급스럽되 허세처럼 보이지 않아야 한다.

브랜드 톤은 다음을 강조한다.

```text
진지함
꾸준함
기록
실행
성장
함께하는 태도
```

Good:

```text
성장은 기록될 때 자산이 됩니다.
이번 달의 실행이 다음 달의 나를 만듭니다.
읽고 끝내지 않습니다. 기록하고 실행합니다.
진지하게 성장하려는 사람들이 모였습니다.
작은 기록이 오래 남는 성장의 흔적이 됩니다.
```

Avoid:

```text
이번 달 TOP 성장 회원
상위 1% 부자 습관
최고의 성과를 낸 사람
인생역전 성공 커뮤니티
누구나 부자가 되는 비밀
```

### 4.5 Emoji 사용 정책

MVP에서 이모지는 최소화한다.

허용:

```text
마이페이지 참여 완료 상태에서 제한적으로 사용
예: 이번 달 참여 완료
```

지양:

```text
회원카드, Hero, 모임 후기, 브랜드 문구에 과도한 이모지 사용
귀엽거나 가벼운 톤의 이모지 남발
```

---

## 5. Photography & Image Direction

Growth Archive는 **리얼한 사진 중심**으로 브랜딩한다.

사진은 단순 장식이 아니라, 이 커뮤니티가 실제로 존재하고 실제 사람들이 모여 성장하고 있다는 증거다.

### 5.1 Photography Principles

```text
실제 회원
실제 모임
실제 책
실제 장소
실제 대화의 순간
```

이미지는 가능하면 직접 촬영한 사진을 우선 사용한다.

스톡 이미지는 MVP 초기 임시 용도로만 사용하며, 공개 브랜딩의 중심 이미지로 사용하지 않는다.

### 5.2 Hero Photography

홈 Hero 또는 주요 랜딩 영역에는 다음 분위기의 사진을 사용한다.

```text
책을 앞에 두고 진지하게 대화하는 사람들
모임 테이블 위의 책, 노트, 커피, 노트북
따뜻한 조명 아래 집중하고 있는 장면
오프라인 모임의 실제 분위기가 느껴지는 사진
```

Avoid:

```text
엄지척 포즈
과한 단체 포즈
인위적인 비즈니스 스톡 이미지
지나치게 화려한 성공 이미지
돈다발, 슈퍼카, 명품 중심 이미지
```

### 5.3 Member Profile Photo

회원 프로필 사진은 성장하는 사람들의 신뢰감을 만든다.

권장:

```text
실제 인물 사진
얼굴이 알아볼 수 있는 선명한 사진
깔끔한 배경
정면 또는 자연스러운 반측면
과한 보정 없는 사진
```

Fallback 순서:

```text
회원 업로드 이미지
↓
카카오 프로필 이미지
↓
기본 프로필 이미지
```

기본 프로필 이미지는 캐릭터보다 고급스러운 추상형 아바타를 사용한다.

### 5.4 Meeting Photos

모임 사진은 커뮤니티 신뢰를 만드는 핵심 자산이다.

권장:

```text
책상 위에 놓인 책과 노트
멤버들이 대화하는 장면
발표나 공유를 듣는 장면
모임 후 자연스럽게 찍은 단체 사진
카페/스터디룸의 실제 분위기
```

사진 톤:

```text
따뜻한 자연광 또는 실내 조명
낮은 채도
선명하지만 과하지 않은 대비
차분하고 고급스러운 분위기
```

### 5.5 Reading Photos

독서기록의 대표 사진은 다음을 모두 허용한다.

```text
책 표지 사진
실제 독서 인증 사진
책과 노트가 함께 있는 사진
독서모임에서 찍은 책 사진
```

단, 책 표지는 외부 도서 API에서 가져온 표지와 중복될 수 있으므로, 가능하면 실제 독서 상황이 담긴 사진을 권장한다.

### 5.6 Image Treatment

이미지는 서비스 전체에서 일관된 톤으로 보여야 한다.

권장 처리:

```text
object-fit: cover
부드러운 radius
얇은 border
필요 시 아주 약한 dark overlay
과한 필터 사용 금지
```

권장 비율:

```text
Hero image: 16:9 또는 4:3
Meeting card image: 4:3
Review gallery thumbnail: 1:1 또는 4:3
Member profile photo: 1:1
Member showcase card image: 4:5 또는 1:1
Book cover: 2:3
```

### 5.7 Photo Privacy Notice

모임 후기 작성 화면에는 아래 안내를 표시한다.

```text
업로드한 사진은 공개 모임 후기에 노출될 수 있어요.
함께 나온 사람들에게 공개 가능 여부를 확인해주세요.
```

Admin은 부적절하거나 공개에 문제가 있는 사진을 숨김/삭제할 수 있다.

---

## 6. Layout Principles

### 6.1 Mobile First

MVP는 모바일 웹뷰 사용을 전제로 한다.

모든 주요 기능은 모바일 기준으로 먼저 설계한다.

```text
Primary viewport: 360px ~ 430px
Secondary viewport: tablet
Tertiary viewport: desktop
```

### 6.2 콘텐츠 최대 너비

일반 콘텐츠 영역은 너무 넓게 펼치지 않는다.

```text
Mobile: full width with 20px horizontal padding
Tablet: max-width 720px
Desktop main content: max-width 1080px
Reading/detail content: max-width 760px
Admin content: max-width 1280px
```

### 6.3 화면 여백

프리미엄 아카이브는 여백이 중요하다.
정보를 빽빽하게 몰아넣지 않는다.

```text
Mobile horizontal padding: 20px
Section vertical spacing: 56px ~ 88px
Card inner padding: 18px ~ 28px
List item vertical gap: 16px ~ 24px
```

### 6.4 Editorial Layout

주요 공개 화면은 단순 리스트보다 에디토리얼 구조를 사용한다.

예:

```text
큰 이미지 + 짧은 문장
카드 그리드 + 넉넉한 여백
섹션 타이틀 + 짧은 설명
실제 사진 + 기록 데이터
```

---

## 7. Navigation System

### 7.1 Desktop Header

Desktop 상단 메뉴는 아래 순서를 사용한다.

```text
독서기록 라이브러리
모임
성장하는 사람들
모임 후기
소개
```

우측 영역:

```text
Guest: 로그인
Member: 마이페이지
Admin: 관리자
```

### 7.2 Mobile Bottom Navigation

모바일 하단 탭은 핵심 5개만 제공한다.

```text
홈
라이브러리
모임
사람들
마이
```

각 탭의 목적:

| Tab | 목적 |
|---|---|
| 홈 | 최근 성장 기록과 주요 섹션 진입 |
| 라이브러리 | 독서기록, 책 검색, 인기 도서 |
| 모임 | 정기모임, 소소모임 확인 |
| 사람들 | 성장하는 사람들 쇼케이스 |
| 마이 | 내 기록, 참여 현황, 빠른 작성 |

### 7.3 Mobile Hamburger Menu

모바일 상단 우측에는 햄버거 메뉴를 제공한다.

햄버거 메뉴 포함 항목:

```text
모임 후기
소개
이용약관
개인정보처리방침
로그아웃
관리자 페이지(Admin only)
```

### 7.4 Safe Area

모바일 웹뷰와 iOS Safari를 고려해 하단 내비게이션에는 safe-area padding을 적용한다.

```css
padding-bottom: env(safe-area-inset-bottom);
```

---

## 8. Color System

MVP의 기본 컬러 방향은 **Quiet Luxury Archive**로 한다.
과한 금색, 검정/금색 성공팔이 톤, 투자 광고처럼 보이는 컬러 사용을 피하고, 실제 사진을 품격 있게 받쳐주는 아이보리/차콜/브론즈/딥그린 계열을 사용한다.

컬러는 사용자의 시선을 빼앗는 장식이 아니라, 실제 사람과 기록과 사진을 조용히 받쳐주는 배경이어야 한다.

### 8.1 Semantic Tokens

권장 Tailwind / CSS variable token:

```text
--background
--foreground
--card
--card-foreground
--muted
--muted-foreground
--border
--primary
--primary-foreground
--secondary
--secondary-foreground
--accent
--accent-foreground
--success
--warning
--danger
```

### 8.2 Adopted MVP Palette: Quiet Luxury Archive

MVP에서는 아래 팔레트를 기본값으로 사용한다. 실제 사진 톤과 구현 결과에 따라 소폭 조정은 가능하지만, 브랜드 방향은 이 팔레트를 기준으로 유지한다.

```text
Background / Soft Ivory  #F8F6EE
Surface / Warm White     #FFFEFA
Foreground / Ink         #22221C
Primary / Charcoal       #342F26
Secondary Text           #6E685F
Border / Line            #E5DCCF
Accent / Bronze          #8A6A45
Growth Accent / Green    #2F5A43
Wood Brown               #4A3424
Success                  #2F855A
Warning                  #B7791F
Danger                   #C53030
```

### 8.3 Token Mapping

```text
--background: #F8F6EE
--foreground: #22221C
--card: #FFFEFA
--card-foreground: #22221C
--muted: #EFE7DB
--muted-foreground: #6E685F
--border: #E5DCCF
--primary: #2F5A43
--primary-foreground: #FFFEFA
--accent: #8A6A45
--accent-foreground: #FFFEFA
--success: #2F5A43
--wood-brown: #4A3424
--warning: #B7791F
--danger: #C53030
```

### 8.4 Usage Rules

```text
Deep Green은 주요 CTA와 참여 완료, 성장 체크 완료, 긍정 상태에 사용한다.
Wood Brown은 녹색 CTA hover/focus와 우드톤 포인트에 사용한다.
Charcoal은 헤더 텍스트와 본문 강조에 사용한다.
Soft Ivory 배경은 전체 서비스의 차분한 바탕으로 사용한다.
Warm White는 카드, 폼, 모달, 프로필 영역의 표면색으로 사용한다.
Bronze는 추천책, 큐레이션, 중요한 라벨에 제한적으로 사용한다.
Danger는 삭제/숨김 같은 파괴적 액션에만 사용한다.
화면 전체가 금색/초록색으로 느껴지지 않도록 포인트 컬러는 10% 이하로 제한한다.
```

### 8.5 Color Impression

이 컬러 시스템은 다음 인상을 목표로 한다.

```text
고급 호텔 라운지
프리미엄 북클럽
차분한 비즈니스 커뮤니티
실제 성장 기록이 쌓이는 프라이빗 아카이브
```

---

## 9. Typography

### 9.1 Font Family

MVP 기본 폰트는 북클럽 무드에 맞춰 아래 조합을 사용한다.

```css
--font-body: "Gowun Batang", "Noto Serif KR", "Apple SD Gothic Neo", serif;
--font-display: "NanumSquare", "Gowun Batang", "Noto Serif KR", sans-serif;
--font-hand: "NanumBeomSomCe", "Nanum Pen Script", "Nanum Brush Script", cursive;
--font-latin: "Caveat", "Nanum Pen Script", cursive;
```

현재 구현은 핵심 폰트 파일을 `frontend/public/fonts/`에 포함한다.

```text
caveat.ttf          영어 필기체 / 라틴 장식 텍스트
nanum-square.woff   큰 제목과 주요 UI 제목
nanum-beomsom.ttf   소개 페이지와 후기 요약의 손글씨 무드
```

폰트 깜빡임을 줄이기 위해 주요 로컬 폰트는 `font-display: block`을 사용한다.

### 9.2 Type Scale

Mobile 기준:

| Token | Size | Usage |
|---|---:|---|
| display | 34px / 1.15 | Home Hero title |
| h1 | 28px / 1.25 | Page title |
| h2 | 22px / 1.3 | Section title |
| h3 | 18px / 1.4 | Card title |
| body | 16px / 1.65 | Main text |
| small | 14px / 1.5 | Metadata |
| caption | 12px / 1.4 | Helper text |

Desktop에서는 display/h1/h2만 조금 키울 수 있다.

### 9.3 Text Rules

```text
본문은 16px 이상을 기본으로 한다.
문장은 짧고 단단하게 쓴다.
한 줄 설명은 2줄 이상 넘어가지 않게 truncate 가능하다.
50살의 나는 프로필 상단에서 문장형으로 여유 있게 보여준다.
관리자 화면을 제외하고 테이블형 UI를 과하게 쓰지 않는다.
```

---

## 10. Component System

Codex 구현 시 컴포넌트는 재사용 가능하게 분리한다.

권장 컴포넌트 네이밍:

```text
AppHeader
MobileBottomNav
MobileHamburgerMenu
PhotoHero
SectionHeader
EditorialSection
BookCard
RecommendedBookCard
ReadingRecordCard
MemberProfileCard
MemberShowcaseGrid
GrowthProfileHero
GrowthStatCard
MonthlyParticipationCard
MeetingCard
AttendeeAvatarStack
MeetingReviewCard
ImageGallery
EmptyState
LoadingSkeleton
```

---

## 11. Core Components

## 11.1 Button

### Types

```text
Primary Button
Secondary Button
Ghost Button
Danger Button
Link Button
```

### Usage

Primary Button은 화면당 1개를 원칙으로 한다.

예:

```text
독서기록 작성
참석하기
실행계획 작성
프로필 저장
```

### States

```text
Default
Hover
Pressed
Disabled
Loading
```

Loading 상태에서는 중복 제출을 막는다.

---

## 11.2 Card

Growth Archive의 핵심 UI 단위는 Card다.

기본 카드 스타일:

```text
흰색 또는 아이보리 계열 배경
절제된 radius
얇은 border
낮은 shadow 또는 shadow 없음
충분한 내부 여백
```

권장:

```text
border-radius: 8px 이하
border: 1px solid var(--border)
padding: 화면 밀도에 맞춰 14px ~ 24px
```

카드는 정보를 담되, 너무 많은 액션을 넣지 않는다.

Admin 목록 카드처럼 편집 대상이 되는 카드는 텍스트 일부가 아니라 카드 전체 클릭으로 선택/접기를 제공한다. 모바일과 긴 목록에서는 선택한 카드 바로 아래 또는 가까운 위치에 편집 패널을 배치해 현재 맥락을 잃지 않게 한다.

탈퇴 같은 파괴적 기능은 큰 빨간 버튼으로 강조하지 않고, 설정 하단에 작은 붉은 텍스트 액션으로 제공한다. 실행 전에는 앱 디자인에 맞춘 확인 팝업 또는 확인 문구 입력을 요구한다.

---

## 11.3 PhotoHero

공개 화면의 첫인상을 만드는 컴포넌트.

사용 위치:

```text
Home
소개
성장하는 사람들
모임 후기
```

구조:

```text
브랜드 문구
짧은 설명
Primary CTA
Secondary CTA(optional)
실제 모임/사람/책 사진
```

Desktop:

```text
텍스트 + 이미지 split layout 또는 large editorial hero
```

Mobile:

```text
문구 우선
아래에 16:9 이미지
또는 이미지 위에 약한 overlay
```

주의:

```text
사진이 텍스트 가독성을 방해하면 안 된다.
과한 dark overlay 금지.
실제 사진이 없을 경우 고급스러운 추상 배경보다 모임/책 사진을 우선 확보한다.
```

---

## 11.4 Book Card

책 카드에는 책 표지가 핵심이다.

노출 정보:

```text
책 표지
책 제목
저자
평균 평점(optional)
독서기록 수
```

이미지 비율:

```text
Book cover ratio: 2:3
```

빈 표지 fallback:

```text
고급스러운 단색 책 커버 형태
책 제목 첫 글자 또는 기본 책 아이콘
```

---

## 11.5 Recommended Book Card

추천책 카드.

노출 정보:

```text
책 표지
책 제목
저자
추천 이유
추천자(optional)
```

디자인 톤:

```text
운영진 큐레이션 느낌
Bronze accent 사용 가능
슬라이드 또는 가로 스크롤 가능
```

모바일에서는 가로 스크롤 카드형을 우선한다.

---

## 11.6 Reading Record Card

독서기록 카드.

노출 정보:

```text
작성자 표시명
프로필 이미지
책 제목
평점(optional)
한줄평
대표 이미지(optional)
블로그 원문 보기 링크
작성일
```

Guest도 블로그 링크를 볼 수 있다.

평점이 없을 경우:

```text
별점 영역 자체를 숨긴다.
```

대표 이미지가 있는 경우 카드의 생동감을 높이기 위해 적절히 크게 노출한다.

---

## 11.7 Member Profile Card

`성장하는 사람들` 목록에서 사용하는 회원 카드.

이 카드는 회원 목록이 아니라 **성장하는 사람들의 프리미엄 쇼케이스**처럼 보여야 한다.

Guest 공개 정보:

```text
프로필 사진
표시명
한 줄 소개
관심 분야 태그
50살의 나 1~2줄
성장 통계 요약
최근 공개 활동
```

Member 추가 정보는 카드가 아니라 상세 프로필에서 보여준다.

### Visual Direction

```text
트렌디한 명함형 카드
고급스러운 포트레이트 카드
과한 장식보다 사진, 여백, 타이포그래피 중심
프로필 사진은 충분히 크게
관심 분야 태그는 부드러운 pill 형태
성장 통계는 작고 정돈된 숫자 블록
```

### Recommended Layout

```text
상단: 큰 프로필 사진 또는 4:5 포트레이트 영역
중단: 표시명, 한 줄 소개, 관심 분야 태그
하단: 50살의 나 요약, 성장 통계, 최근 공개 활동
```

### Interaction

카드 클릭 시:

```text
/people/{memberId}
```

### Avoid

```text
단순 테이블형 회원 목록
작은 프로필 이미지 + 이름만 있는 리스트
과한 배지와 점수 표시
귀여운 캐릭터 스타일
```

---

## 11.8 Growth Profile Hero

성장 프로필 상세 상단.

가장 중요한 정보는 `50살의 나`다.

구조:

```text
프로필 사진
표시명
한 줄 소개
관심 분야

50살의 나
큰 문장형 블록
```

Member에게만 추가 노출:

```text
가입 이유
현재 고민
3년 뒤 목표
```

디자인 톤:

```text
개인 성장 명함
프리미엄 프로필 페이지
LinkedIn보다 따뜻하고, SNS보다 진지한 느낌
```

---

## 11.9 Growth Stat Card

성장 통계 카드.

노출 가능한 지표:

```text
독서기록 수
실행계획 수
월간회고 수
모임후기 수
소소모임 개설 수
모임 참석 수
```

금지:

```text
랭킹
순위
전체 회원 대비 상위 n%
```

통계는 비교가 아니라 개인의 누적 기록으로 표현한다.

---

## 11.10 Monthly Participation Card

마이페이지 최상단 핵심 카드.

목적:

```text
이번 달 최소 참여 여부를 기분 좋게 보여준다.
```

규칙:

```text
독서기록 1건 또는 실행계획 1건 작성 시 참여 완료
```

완료 상태 예시:

```text
이번 달 참여 완료
독서기록 1건
실행계획 0건
```

미완료 상태 예시:

```text
이번 달 참여가 아직 필요해요
독서기록 0건
실행계획 0건

독서기록 작성 또는 실행계획 작성 중 하나만 해도 완료돼요.
```

주의:

```text
미완료 상태도 부끄럽게 만들지 않는다.
빨간 경고 UI 사용 금지.
커피 후원 문구는 Admin 화면에서 더 명확하게 관리한다.
```

---

## 11.11 Meeting Card

모임 목록에서 사용하는 카드.

노출 정보:

```text
대표 이미지
모임명
모임 유형
일시
지역 수준 장소
참석자 수
작은 참석자 프로필 이미지 스택
```

Guest:

```text
지역 수준 장소만 표시
정확한 장소 숨김
참석자 이름 숨김
프로필 클릭 불가
작은 프로필 이미지 일부 공개
```

Member:

```text
정확한 장소 표시
참석자 이미지/이름 확인 가능
참석하기 버튼 표시
```

---

## 11.12 Attendee Avatar Stack

소모임 앱에서 본 것처럼 작은 원형 프로필들이 가로로 나열되는 UI.

### Guest

```text
아주 작은 원형 프로필 이미지 일부 공개
이름 비공개
클릭 불가
표시 개수 제한: 최대 8개 권장
```

### Member

```text
작은 원형 프로필 이미지 공개
클릭 시 참석자 목록 또는 프로필 이동 가능
표시 개수 제한 후 +N 표시
```

권장 크기:

```text
Guest avatar: 28px ~ 32px
Member avatar: 32px ~ 36px
```

---

## 11.13 Meeting Review Card

모임 후기 목록 카드.

노출 정보:

```text
대표 이미지
제목
작성자 표시명
작성일
연결된 모임
사진 수
내용 요약
```

Guest도 볼 수 있다.

모임 후기는 리얼한 사진 중심으로 신뢰를 만든다.
사진이 공개될 수 있으므로 후기 작성 화면에는 안내 문구를 표시한다.

```text
업로드한 사진은 공개 모임 후기에 노출될 수 있어요.
함께 나온 사람들에게 공개 가능 여부를 확인해주세요.
```

---

## 11.14 Image Gallery

모임 후기 상세에서 사용하는 이미지 갤러리.

정책:

```text
최대 10장
브라우저 우선 WebP 변환/리사이징/압축
서버 방어 검증
대표 이미지 자동 지정
```

모바일 UX:

```text
썸네일 그리드
이미지 탭 시 확대 보기
좌우 스와이프 가능하면 좋음
```

MVP에서는 고급 갤러리보다 안정적인 업로드/조회가 우선이다.

---

## 11.15 Empty State

빈 상태는 따뜻하지만 가볍지 않게 행동을 유도한다.

예:

```text
아직 독서기록이 없습니다.
첫 번째 기록을 남겨보세요.

아직 이번 달 실행계획이 없습니다.
이번 달 해보고 싶은 일을 가볍게 적어보세요.

아직 모임 후기가 없습니다.
다녀온 모임의 온도를 남겨보세요.
```

---

## 11.16 Loading Skeleton

책 검색, 최근 기록, 카드 목록에는 skeleton loading을 사용한다.

단순 spinner만 사용하는 것을 피한다.

---

## 12. Page UX Guidelines

## 12.1 Home

섹션 순서:

```text
1. Photo Hero
2. 추천책
3. 최근 성장 기록
4. 인기 도서 TOP5
5. 최근 모임
6. 최근 모임 후기
7. 성장하는 사람들
```

Home의 목적은 전체 기능을 나열하는 것이 아니라, 커뮤니티가 살아있고 진지한 사람들이 모여 있다는 인상을 주는 것이다.

Hero에는 가능하면 실제 모임 또는 책/대화 장면 사진을 사용한다.

---

## 12.2 Reading Library

라이브러리는 책방 진열대처럼 보여야 한다.

핵심 섹션:

```text
추천책
인기 도서 TOP5
최근 독서기록
책 검색
```

책 표지가 시각적 중심이 되도록 한다.

---

## 12.3 Book Detail

책 상세는 책 하나에 쌓인 여러 사람의 기록을 보여준다.

구조:

```text
책 정보
평균 평점
독서기록 수
이 책을 읽은 사람
작성자별 독서기록 카드 목록
```

작성자별 독서기록은 책 표지와 지표 아래에 카드형 목록으로 제공한다. 각 카드에는 작성자 표시명, 평점, 작성일, 한줄평, 대표 사진, 블로그 원문 링크를 함께 보여준다.

---

## 12.4 Growth People

`성장하는 사람들` 페이지는 트렌디한 회원카드 쇼케이스다.

중요:

```text
회원 목록처럼 보이지 않아야 한다.
진지하고 멋있게 성장하는 사람들이 모여 있다는 인상을 줘야 한다.
사진, 여백, 타이포그래피로 사람의 방향성을 보여줘야 한다.
```

카드는 모바일에서 1열, 태블릿/데스크톱에서 2~3열 그리드를 사용할 수 있다.

---

## 12.5 Growth Profile Detail

프로필 상단에 `50살의 나`를 크게 보여준다.

최근 기록은 전체를 길게 보여주지 않는다.

```text
최근 독서기록 3개
최근 실행계획 3개
최근 회고 3개
```

나머지는 더보기 링크로 이동한다.

---

## 12.6 My Page

마이페이지는 Member의 시작 화면이다.

상단 우선순위:

```text
1. 이번 달 참여 현황
2. 빠른 작성 버튼
3. 내 최근 기록
4. 프로필 관리
```

빠른 작성 버튼:

```text
독서기록 작성
실행계획 작성
월간회고 작성
소소모임 만들기
```

---

## 12.7 Meetings

모임 페이지는 정기모임과 소소모임을 함께 보여준다.

정기모임은 기본 생성되며, 소소모임은 멤버가 직접 만든다.

모임 카드는 실제 모임 사진과 참석자 프로필 스택이 살아있는 느낌을 준다.

---

## 12.8 Meeting Detail

Guest와 Member의 정보 노출을 명확히 나눈다.

Guest:

```text
모임명
설명
일시
지역 수준 장소
참석자 수
작은 프로필 이미지 일부
후기 미리보기
```

Member:

```text
정확한 장소
참석하기
참석 취소
참석자 목록
후기 작성
```

---

## 12.9 Reviews

모임 후기는 신규 방문자에게 신뢰를 주는 화면이다.

사진을 너무 작게 숨기지 말고, 모임 분위기를 느낄 수 있게 보여준다.

다만 사진 공개 안내와 Admin 숨김/삭제 정책이 필요하다.

---

## 12.10 Admin

Admin 화면은 일반 사용자 화면과 디자인 톤을 맞추되, 실용성을 우선한다.

Admin은 카드보다 테이블/필터/검색이 더 중요하다.

Admin 화면에서는 다음을 명확히 보여준다.

```text
회원 상태
초대코드
추천책
참여 현황
미참여자 목록
모임 상태
숨김/삭제 대상 콘텐츠
```

---

## 13. Form UX

### 13.1 General Rules

기록 작성은 가벼워야 한다.

```text
필수 입력 최소화
긴 설명 대신 placeholder 사용
저장 버튼은 화면 하단에서도 접근 가능하게 고려
모바일 키보드에 가려지지 않게 처리
```

### 13.2 Reading Record Form

순서:

```text
1. 책 검색
2. 책 선택 또는 임시 등록
3. 한줄평
4. 평점(optional)
5. 대표 사진(optional)
6. 블로그 URL
7. 저장
```

책 검색 실패가 기록 작성을 막으면 안 된다.

### 13.3 Action Plan Form

실행계획은 자유 입력이다.

```text
제목(optional)
내용(required)
```

체크리스트, 진행률, 완료율은 MVP 제외.

### 13.4 Monthly Reflection Form

회고는 선택적 기록이다.

```text
이번 달 잘한 것
아쉬운 점
다음 달 집중할 것
```

작성하지 않아도 문제 없다는 느낌이 있어야 한다.

---

## 14. Accessibility

### 14.1 Color Contrast

텍스트와 배경은 충분한 대비를 가져야 한다.

Muted text는 본문 중요 정보에는 사용하지 않는다.

### 14.2 Touch Target

모바일 터치 영역은 최소 44px 이상을 권장한다.

### 14.3 Keyboard / Screen Reader

Accordion, Dialog, Menu는 접근 가능한 컴포넌트를 사용한다.

shadcn/ui 또는 Radix UI 기반 컴포넌트 사용을 권장한다.

### 14.4 Images

모든 의미 있는 이미지는 alt text를 제공한다.

```text
책 표지 alt: {책 제목} 표지
프로필 이미지 alt: {표시명} 프로필 이미지
모임 이미지 alt: {모임명} 대표 이미지
모임 후기 이미지 alt: {모임명} 후기 사진 {index}
```

---

## 15. Motion

MVP에서는 과한 애니메이션을 피한다.

허용:

```text
카드 hover
카드 focus/hover
모바일 메뉴 slide
toast fade
사진 카드의 아주 약한 hover zoom on desktop
```

금지:

```text
과한 scroll animation
자동 재생되는 복잡한 motion
읽기 방해하는 animation
사진을 과도하게 움직이는 효과
```

---

## 16. Responsive Rules

### 16.1 Mobile

```text
1 column
bottom navigation
horizontal card scroll allowed
large touch target
photo-first sections should not exceed readable height
```

### 16.2 Tablet

```text
2 column card grid 가능
side margin 증가
```

### 16.3 Desktop

```text
max-width content
3 column card grid 가능
header navigation fixed or sticky 가능
editorial split layout 가능
```

---

## 17. Design Tokens for Implementation

Codex는 Tailwind config 또는 CSS variables로 디자인 토큰을 분리해야 한다.

Example:

```css
:root {
  --background: #F8F5EF;
  --foreground: #141414;
  --card: #FFFFFF;
  --muted: #F3EFE7;
  --muted-foreground: #6F6A62;
  --border: #E6E0D6;
  --primary: #111827;
  --accent: #B08D57;
  --success: #2F855A;
  --warning: #B7791F;
  --danger: #C53030;
}
```

Tokens must be used through semantic names, not hardcoded repeatedly.

---

## 18. Image Implementation Guidance

Codex는 이미지 구현 시 다음 원칙을 따른다.

```text
1. Next.js Image 컴포넌트 또는 이에 준하는 최적화 방식을 사용한다.
2. 모든 카드 이미지는 aspect-ratio를 명시한다.
3. object-fit: cover를 기본으로 한다.
4. 이미지가 없을 때 고급스러운 fallback UI를 제공한다.
5. 외부 스톡 이미지 URL을 코드에 하드코딩하지 않는다.
6. 업로드 이미지는 브라우저에서 우선 WebP 변환/리사이징/압축하고, 최소한 리사이징된 표시용 이미지로 보여준다.
7. 프로필/모임/후기 이미지는 public/private 공개 정책을 PRD 기준으로 따른다.
8. 얼굴이 포함된 모임 사진은 공개 안내 문구를 UX에 포함한다.
9. 여러 장 업로드는 파일별 상태를 보여주되, 화면을 복잡하게 만들지 않는다.
10. 일부 업로드 실패 시 전체 저장을 막지 않고 성공한 사진만 저장되었음을 담백하게 안내한다.
11. 업로드 중에는 버튼/상태 문구가 모바일에서 줄바꿈되어도 레이아웃이 깨지지 않아야 한다.
```

---

## 19. MVP Exclusions

아래는 MVP 디자인 범위에서 제외한다.

```text
Dark mode
복잡한 애니메이션
고급 gamification UI
랭킹 UI
포인트/레벨 UI
실시간 채팅 UI
푸시 알림 UI
완전한 네이티브 앱 UI
전용 로고 제작
전문 사진 촬영 프로세스 자동화
```

---

## 20. Acceptance Criteria

DESIGN-001은 아래 조건을 만족해야 한다.

```text
1. 모바일에서 핵심 기능이 하단 탭으로 접근 가능해야 한다.
2. 일반 화면은 게시판처럼 보이면 안 된다.
3. 서비스 전체는 고급스럽고 깔끔한 성장 아카이브처럼 보여야 한다.
4. 실제 사진이 브랜드 신뢰를 만드는 핵심 시각 요소로 사용되어야 한다.
5. 성장하는 사람들 페이지는 회원 목록이 아니라 쇼케이스처럼 보여야 한다.
6. 회원카드는 사진, 여백, 타이포그래피 중심의 트렌디한 명함형 카드여야 한다.
7. 마이페이지는 이번 달 참여 현황을 가장 먼저 보여야 한다.
8. 독서기록 라이브러리는 책 표지가 중심이 되어야 한다.
9. 모임 상세는 참석자 프로필 스택을 작은 원형 이미지로 보여야 한다.
10. Guest와 Member의 정보 노출 차이가 시각적으로도 분명해야 한다.
11. 작성 폼은 모바일에서 부담 없이 작성 가능해야 한다.
12. Admin 화면은 실용성과 필터/검색을 우선해야 한다.
13. 디자인 토큰은 코드에서 재사용 가능해야 한다.
14. 사진이 없더라도 전체 품질이 급격히 떨어지지 않는 fallback UI가 있어야 한다.
```

---

## 21. Codex Implementation Guidance

Codex는 UI 구현 시 다음 원칙을 따라야 한다.

```text
1. Tailwind CSS utility class를 사용하되, 반복되는 UI는 컴포넌트로 분리한다.
2. shadcn/ui 또는 Radix UI 기반 접근 가능한 컴포넌트를 우선 사용한다.
3. 색상은 semantic token을 우선 사용한다.
4. 모바일 레이아웃을 먼저 구현하고 desktop을 확장한다.
5. 하드코딩된 디자인 값이 반복되면 token 또는 component prop으로 분리한다.
6. MVP에서 제외된 dark mode, ranking, point UI를 임의로 추가하지 않는다.
7. 카드 UI는 정보 과밀을 피하고 충분한 여백을 유지한다.
8. Guest/Member/Admin 상태에 따른 노출 차이는 PRD-002, PRD-003을 우선한다.
9. 이미지는 real-photo-first 원칙을 따르며, 임시 placeholder도 고급스럽게 처리한다.
10. 회원카드와 모임카드는 이미지가 있는 경우 사진이 주인공이 되도록 설계한다.
```

---

## 22. Open Questions

아래 항목은 디자인 시안 또는 MVP 구현 중 추가 확정한다.

### OQ-D001. 최종 로고

MVP에서는 텍스트 로고를 사용한다.

```text
부자습관 만들기
Growth Archive
```

전용 로고는 MVP 이후 제작 가능.

### OQ-D002. 컬러 팔레트 조정 범위

MVP 기본 팔레트는 `Quiet Luxury Archive`로 채택한다.
다만 실제 모임 사진, 회원 프로필 사진, 책 표지의 톤을 적용한 뒤 다음 범위 내에서만 소폭 조정 가능하다.

```text
Background / Surface 밝기
Border 명도
Bronze 채도
Deep Green 명도
```

브랜드 방향을 바꾸는 수준의 컬러 변경은 MVP 이후 별도 디자인 리뷰에서 결정한다.

### OQ-D003. 아이콘 세트

MVP에서는 lucide-react 같은 일관된 아이콘 세트 사용 권장.

### OQ-D004. 회원카드 최종 비주얼 스타일

DESIGN-001에서는 정보 구조와 사진 중심 방향을 확정한다.
실제 카드 비주얼은 구현 후 개선 가능.

### OQ-D005. 다크모드

MVP 제외.
Phase 2 이후 검토.

### OQ-D006. 사진 수급 전략

MVP 초기에는 기존 소모임/모임 후기 사진을 우선 사용한다.

추후 브랜딩 강화를 위해 다음을 검토한다.

```text
모임 사진 촬영 가이드
프로필 사진 가이드
운영진 추천 대표 사진 세트
홈 Hero용 실제 모임 사진 큐레이션
```

---

## 23. Summary

Growth Archive의 디자인은 기능을 화려하게 포장하는 것이 아니다.

이 서비스의 디자인은 사람들이 남긴 작은 기록이 시간이 지나도 흐려지지 않도록, 진지하고 고급스러운 그릇이 되어야 한다.

```text
책은 표지로 기억되고,
사람은 방향으로 기억되고,
성장은 기록으로 남는다.

그리고 그 모든 순간은
실제 사람들의 사진과 기록 속에서
더 오래 믿어진다.
```
