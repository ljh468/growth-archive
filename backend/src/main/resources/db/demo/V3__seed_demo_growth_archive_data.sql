INSERT INTO image_assets (id, owner_member_id, bucket, object_key, public_url, image_type, mime_type, width, height, size_bytes, created_at)
VALUES
    (1, null, 'local-demo', 'profiles/member-minjun.png', '/images/profiles/member-minjun.png', 'PROFILE', 'image/png', 512, 512, 120000, '2026-06-01 09:00:00+09'),
    (2, null, 'local-demo', 'profiles/member-seoyeon.png', '/images/profiles/member-seoyeon.png', 'PROFILE', 'image/png', 512, 512, 120000, '2026-06-01 09:00:00+09'),
    (3, null, 'local-demo', 'profiles/member-jihoon.png', '/images/profiles/member-jihoon.png', 'PROFILE', 'image/png', 512, 512, 120000, '2026-06-01 09:00:00+09'),
    (4, null, 'local-demo', 'profiles/member-yujin.png', '/images/profiles/member-yujin.png', 'PROFILE', 'image/png', 512, 512, 120000, '2026-06-01 09:00:00+09'),
    (5, null, 'local-demo', 'profiles/member-doyun.png', '/images/profiles/member-doyun.png', 'PROFILE', 'image/png', 512, 512, 120000, '2026-06-01 09:00:00+09'),
    (6, null, 'local-demo', 'profiles/member-harin.png', '/images/profiles/member-harin.png', 'PROFILE', 'image/png', 512, 512, 120000, '2026-06-01 09:00:00+09'),
    (7, null, 'local-demo', 'profiles/member-minjun-2.png', '/images/profiles/member-minjun.png', 'PROFILE', 'image/png', 512, 512, 120000, '2026-06-01 09:00:00+09'),
    (8, null, 'local-demo', 'profiles/member-seoyeon-2.png', '/images/profiles/member-seoyeon.png', 'PROFILE', 'image/png', 512, 512, 120000, '2026-06-01 09:00:00+09'),
    (9, null, 'local-demo', 'profiles/member-jihoon-2.png', '/images/profiles/member-jihoon.png', 'PROFILE', 'image/png', 512, 512, 120000, '2026-06-01 09:00:00+09'),
    (10, null, 'local-demo', 'profiles/member-yujin-2.png', '/images/profiles/member-yujin.png', 'PROFILE', 'image/png', 512, 512, 120000, '2026-06-01 09:00:00+09'),
    (11, null, 'local-demo', 'reading/korean-reading-table.jpg', '/images/korean-reading-table.jpg', 'READING_RECORD', 'image/jpeg', 1200, 900, 280000, '2026-06-01 09:00:00+09'),
    (12, null, 'local-demo', 'reading/korean-reading-table-alt.jpg', '/images/korean-reading-table.jpg', 'READING_RECORD', 'image/jpeg', 1200, 900, 260000, '2026-06-01 09:00:00+09'),
    (13, null, 'local-demo', 'reading/reading-books.jpg', '/images/reading-books.jpg', 'READING_RECORD', 'image/jpeg', 1200, 900, 240000, '2026-06-01 09:00:00+09'),
    (14, null, 'local-demo', 'reading/member-books.jpg', '/images/member-books.jpg', 'READING_RECORD', 'image/jpeg', 1200, 900, 250000, '2026-06-01 09:00:00+09'),
    (15, null, 'local-demo', 'reading/book-shelf.jpg', '/images/book-shelf.jpg', 'READING_RECORD', 'image/jpeg', 1200, 900, 270000, '2026-06-01 09:00:00+09'),
    (16, null, 'local-demo', 'reading/meeting-table.jpg', '/images/meeting-table.jpg', 'READING_RECORD', 'image/jpeg', 1200, 900, 290000, '2026-06-01 09:00:00+09'),
    (17, null, 'local-demo', 'reading/korean-bookclub-discussion.jpg', '/images/korean-bookclub-discussion.jpg', 'READING_RECORD', 'image/jpeg', 1200, 900, 300000, '2026-06-01 09:00:00+09'),
    (18, null, 'local-demo', 'reading/gathering-people.jpg', '/images/gathering-people.jpg', 'READING_RECORD', 'image/jpeg', 1200, 900, 285000, '2026-06-01 09:00:00+09'),
    (19, null, 'local-demo', 'reading/hero-bookclub-study.png', '/images/hero-bookclub-study.png', 'READING_RECORD', 'image/png', 1200, 900, 310000, '2026-06-01 09:00:00+09'),
    (20, null, 'local-demo', 'reading/hero-bookclub-discussion.png', '/images/hero-bookclub-discussion.png', 'READING_RECORD', 'image/png', 1200, 900, 315000, '2026-06-01 09:00:00+09');

INSERT INTO members (
    id, role, display_type, real_name, birth_date, nickname, one_line_intro, profile_image_id,
    kakao_profile_image_url, fifty_year_old_me, join_reason, current_concern, three_year_goal,
    participation_start_month, invite_verified_at, terms_agreed_at, privacy_agreed_at,
    onboarding_completed_at, created_at, updated_at
)
VALUES
    (1, 'ADMIN', 'REAL_NAME', '김민준', '1987-03-14', '책읽는민준', '작게 읽고 바로 실행합니다.', 1, null, '50살에는 가족과 동료에게 시간과 지식을 나누는 투자자가 되고 싶습니다.', '혼자 읽고 끝내지 않고 실행까지 남기고 싶어서 참여했습니다.', '꾸준함을 일상에 고정하는 것이 가장 큰 과제입니다.', '매달 한 권의 책에서 하나의 실행 습관을 만들겠습니다.', '2026-06-01', now(), now(), now(), now(), '2026-06-01 09:10:00+09', now()),
    (2, 'MEMBER', 'NICKNAME', '박서연', '1991-08-22', '새벽독서가', '사업과 건강 루틴을 함께 쌓습니다.', 2, null, '50살에는 작은 브랜드를 오래 운영하며 건강한 생활 리듬을 유지하고 싶습니다.', '책에서 얻은 문장을 실제 업무에 옮기는 연습을 하고 싶었습니다.', '할 일을 벌리는 습관을 줄이고 중요한 일에 집중하고 싶습니다.', '사이드 프로젝트를 안정적인 수익 구조로 키우겠습니다.', '2026-06-01', now(), now(), now(), now(), '2026-06-02 09:10:00+09', now()),
    (3, 'MEMBER', 'REAL_NAME', '이지훈', '1989-11-03', '데이터리더', '숫자보다 행동을 먼저 봅니다.', 3, null, '50살에는 경제적으로 자유롭지만 호기심을 잃지 않는 사람이 되고 싶습니다.', '투자와 독서를 기록으로 연결하고 싶어 참여했습니다.', '시장 흐름에 흔들리지 않는 기준을 세우는 중입니다.', '장기 투자 원칙을 글과 데이터로 정리하겠습니다.', '2026-06-01', now(), now(), now(), now(), '2026-06-03 09:10:00+09', now()),
    (4, 'MEMBER', 'NICKNAME', '최유진', '1993-05-19', '회고수집가', '매일의 작은 선택을 기록합니다.', 4, null, '50살에는 좋은 질문을 던지는 코치이자 독서 모임 호스트가 되고 싶습니다.', '책을 통해 삶의 방향을 자주 점검하고 싶었습니다.', '일과 회복의 균형을 더 잘 잡고 싶습니다.', '나만의 회고 시스템을 만들어 꾸준히 운영하겠습니다.', '2026-06-01', now(), now(), now(), now(), '2026-06-04 09:10:00+09', now()),
    (5, 'MEMBER', 'REAL_NAME', '정도윤', '1985-12-27', '현금흐름노트', '부동산과 현금흐름을 공부합니다.', 5, null, '50살에는 안정적인 현금흐름을 만들고 지역 커뮤니티에 기여하고 싶습니다.', '부동산 공부를 실행 기록으로 남기고 싶었습니다.', '정보는 많은데 실제 의사결정 기준이 부족합니다.', '임장 기록과 재무 원칙을 함께 정리하겠습니다.', '2026-06-01', now(), now(), now(), now(), '2026-06-05 09:10:00+09', now()),
    (6, 'MEMBER', 'NICKNAME', '윤하린', '1995-02-11', '러닝북클럽', '러닝과 독서로 리듬을 만듭니다.', 6, null, '50살에도 매일 달리고 읽는 사람으로 남고 싶습니다.', '좋은 습관을 오래 가져가는 사람들과 함께하고 싶었습니다.', '꾸준히 하다가도 일정이 무너지면 회복이 느립니다.', '러닝, 독서, 저축을 월간 루틴으로 고정하겠습니다.', '2026-06-01', now(), now(), now(), now(), '2026-06-06 09:10:00+09', now()),
    (7, 'MEMBER', 'REAL_NAME', '강태오', '1990-07-07', '커리어실험실', '커리어 전환을 차분히 준비합니다.', 7, null, '50살에는 배운 것을 제품과 교육으로 연결하는 사람이 되고 싶습니다.', '이직과 성장을 감정이 아니라 기록으로 관리하고 싶었습니다.', '배움은 많은데 포트폴리오로 정리하는 속도가 느립니다.', '매달 하나씩 공개 가능한 결과물을 만들겠습니다.', '2026-06-01', now(), now(), now(), now(), '2026-06-07 09:10:00+09', now()),
    (8, 'MEMBER', 'NICKNAME', '한지민', '1988-09-16', '가계부정원', '가족과 재무 목표를 함께 봅니다.', 8, null, '50살에는 가족에게 선택지를 넓혀주는 사람이 되고 싶습니다.', '재테크를 조급함 없이 배우고 싶어 참여했습니다.', '소비와 저축의 기준을 가족과 함께 맞추는 중입니다.', '가계부와 독서기록을 연결해 재무 루틴을 만들겠습니다.', '2026-06-01', now(), now(), now(), now(), '2026-06-08 09:10:00+09', now()),
    (9, 'MEMBER', 'REAL_NAME', '오서준', '1992-01-30', 'AI문장가', 'AI와 글쓰기를 함께 실험합니다.', 9, null, '50살에는 기술을 쉽게 설명하는 작가형 창업가가 되고 싶습니다.', '읽은 책을 글과 실험으로 남기고 싶었습니다.', '새로운 도구를 많이 써보지만 정리와 공유가 부족합니다.', 'AI 활용 사례를 매달 하나씩 글로 남기겠습니다.', '2026-06-01', now(), now(), now(), now(), '2026-06-09 09:10:00+09', now()),
    (10, 'MEMBER', 'NICKNAME', '문채원', '1994-04-25', '차분한루틴', '마음챙김과 생산성을 연결합니다.', 10, null, '50살에는 차분한 일상을 지키며 필요한 사람에게 도움을 주고 싶습니다.', '책과 회고로 일상을 덜 흩어지게 만들고 싶었습니다.', '바쁜 시기에 나를 돌보는 시간을 자주 놓칩니다.', '명상과 독서 회고를 꾸준히 기록하겠습니다.', '2026-06-01', now(), now(), now(), now(), '2026-06-10 09:10:00+09', now());

UPDATE image_assets
SET owner_member_id = id
WHERE id BETWEEN 1 AND 10;

INSERT INTO oauth_accounts (id, provider, provider_user_id, member_id, email, profile_nickname, profile_image_url, last_login_at, created_at)
VALUES
    (1, 'KAKAO', '00000000-0000-0000-0000-000000000001', 1, 'minjun@example.com', '김민준', '/images/profiles/member-minjun.png', now(), now()),
    (2, 'KAKAO', '00000000-0000-0000-0000-000000000002', 2, 'seoyeon@example.com', '박서연', '/images/profiles/member-seoyeon.png', now(), now()),
    (3, 'KAKAO', '00000000-0000-0000-0000-000000000003', 3, 'jihoon@example.com', '이지훈', '/images/profiles/member-jihoon.png', now(), now()),
    (4, 'KAKAO', '00000000-0000-0000-0000-000000000004', 4, 'yujin@example.com', '최유진', '/images/profiles/member-yujin.png', now(), now()),
    (5, 'KAKAO', '00000000-0000-0000-0000-000000000005', 5, 'doyun@example.com', '정도윤', '/images/profiles/member-doyun.png', now(), now()),
    (6, 'KAKAO', '00000000-0000-0000-0000-000000000006', 6, 'harin@example.com', '윤하린', '/images/profiles/member-harin.png', now(), now()),
    (7, 'KAKAO', '00000000-0000-0000-0000-000000000007', 7, 'taeo@example.com', '강태오', '/images/profiles/member-minjun.png', now(), now()),
    (8, 'KAKAO', '00000000-0000-0000-0000-000000000008', 8, 'jimin@example.com', '한지민', '/images/profiles/member-seoyeon.png', now(), now()),
    (9, 'KAKAO', '00000000-0000-0000-0000-000000000009', 9, 'seojoon@example.com', '오서준', '/images/profiles/member-jihoon.png', now(), now()),
    (10, 'KAKAO', '00000000-0000-0000-0000-000000000010', 10, 'chaewon@example.com', '문채원', '/images/profiles/member-yujin.png', now(), now());

INSERT INTO invite_codes (id, code_hash, code_preview, role, is_active, created_by_member_id, created_at)
VALUES
    (1, '94ee059335e587e501cc4bf90613e0814f00a7b08bc7c648fd865a2af6a22cc2', '****', 'MEMBER', true, 1, now()),
    (2, '835d6dc88b708bc646d6db82c853ef4182fabbd4a8de59c213f2b5ab3ae7d9be', 'ADM****IN', 'ADMIN', true, 1, now());

INSERT INTO member_interest_tags (member_id, interest_tag_id)
SELECT member_id, it.id
FROM (
    VALUES
        (1, 'reading'), (1, 'investment'), (1, 'habit'),
        (2, 'business'), (2, 'branding'), (2, 'health'),
        (3, 'stocks'), (3, 'financial-freedom'), (3, 'ai'),
        (4, 'writing'), (4, 'mindfulness'), (4, 'leadership'),
        (5, 'real-estate'), (5, 'personal-finance'), (5, 'investment'),
        (6, 'running'), (6, 'reading'), (6, 'habit'),
        (7, 'career'), (7, 'ai'), (7, 'productivity'),
        (8, 'health'), (8, 'personal-finance'), (8, 'investment'),
        (9, 'ai'), (9, 'writing'), (9, 'startup'),
        (10, 'mindfulness'), (10, 'health'), (10, 'time-management')
) AS seed(member_id, slug)
JOIN interest_tags it ON it.slug = seed.slug;

INSERT INTO books (
    id, source, source_book_id, isbn13, title, authors_text, publisher, published_date,
    thumbnail_url, status, source_payload, created_at, updated_at
)
VALUES
    (1, 'KAKAO', 'seed-book-001', '9791188331796', '돈의 속성', '김승호', '스노우폭스북스', '2020-06-15', 'https://contents.kyobobook.co.kr/sih/fit-in/458x0/pdt/9791188331796.jpg', 'VERIFIED', '{}', now(), now()),
    (2, 'KAKAO', 'seed-book-002', '9791162540640', '아주 작은 습관의 힘', '제임스 클리어', '비즈니스북스', '2019-02-26', 'https://contents.kyobobook.co.kr/sih/fit-in/458x0/pdt/9791162540640.jpg', 'VERIFIED', '{}', now(), now()),
    (3, 'KAKAO', 'seed-book-003', '9791191056372', '돈의 심리학', '모건 하우절', '인플루엔셜', '2021-01-13', 'https://contents.kyobobook.co.kr/sih/fit-in/458x0/pdt/9791191056372.jpg', 'VERIFIED', '{}', now(), now()),
    (4, 'KAKAO', 'seed-book-004', '9791158883591', '부자 아빠 가난한 아빠', '로버트 기요사키', '민음인', '2018-02-22', 'https://contents.kyobobook.co.kr/sih/fit-in/458x0/pdt/9791158883591.jpg', 'VERIFIED', '{}', now(), now()),
    (5, 'KAKAO', 'seed-book-005', '9788901260716', '역행자', '자청', '웅진지식하우스', '2022-05-30', 'https://contents.kyobobook.co.kr/sih/fit-in/458x0/pdt/9788901260716.jpg', 'VERIFIED', '{}', now(), now()),
    (6, 'KAKAO', 'seed-book-006', '9788997575169', '원씽', '게리 켈러, 제이 파파산', '비즈니스북스', '2013-08-30', 'https://contents.kyobobook.co.kr/sih/fit-in/458x0/pdt/9788997575169.jpg', 'VERIFIED', '{}', now(), now()),
    (7, 'KAKAO', 'seed-book-007', '9788994702346', '부의 추월차선', '엠제이 드마코', '토트', '2013-08-20', 'https://contents.kyobobook.co.kr/sih/fit-in/458x0/pdt/9788994702346.jpg', 'VERIFIED', '{}', now(), now()),
    (8, 'KAKAO', 'seed-book-008', '9791124280195', '생각하라 그리고 부자가 되어라', '나폴레온 힐', '반니', null, 'https://contents.kyobobook.co.kr/sih/fit-in/458x0/pdt/9791124280195.jpg', 'VERIFIED', '{}', now(), now()),
    (9, 'KAKAO', 'seed-book-009', '9791187142560', '데일 카네기 인간관계론', '데일 카네기', '현대지성', '2019-10-07', 'https://contents.kyobobook.co.kr/sih/fit-in/458x0/pdt/9791187142560.jpg', 'VERIFIED', '{}', now(), now()),
    (10, 'KAKAO', 'seed-book-010', '9791162540633', '그릿', '앤절라 더크워스', '비즈니스북스', '2016-10-25', 'https://contents.kyobobook.co.kr/sih/fit-in/458x0/pdt/9791162540633.jpg', 'VERIFIED', '{}', now(), now()),
    (11, 'KAKAO', 'seed-book-011', '9791158510619', '타이탄의 도구들', '팀 페리스', '토네이도', '2017-04-03', 'https://contents.kyobobook.co.kr/sih/fit-in/458x0/pdt/9791158510619.jpg', 'VERIFIED', '{}', now(), now()),
    (12, 'KAKAO', 'seed-book-012', '9791168473690', '세이노의 가르침', '세이노', '데이원', '2023-03-02', 'https://contents.kyobobook.co.kr/sih/fit-in/458x0/pdt/9791168473690.jpg', 'VERIFIED', '{}', now(), now());

INSERT INTO recommended_books (id, book_id, target_month, reason, recommended_by_member_id, display_order, status, created_at, updated_at)
VALUES
    (1, 8, '2026-06-01', '부를 대하는 오래된 원칙을 지금의 실행 언어로 다시 읽기 좋습니다.', 1, 1, 'ACTIVE', now(), now()),
    (2, 9, '2026-06-01', '좋은 관계와 대화 습관을 차분히 점검하게 해주는 기본서입니다.', 1, 2, 'ACTIVE', now(), now()),
    (3, 10, '2026-06-01', '오래 해내는 힘을 만들고 싶은 성장하는 사람들에게 잘 맞는 책입니다.', 1, 3, 'ACTIVE', now(), now()),
    (4, 11, '2026-06-01', '일과 삶의 도구를 골라 자기 방식으로 실험해 보기 좋습니다.', 1, 4, 'ACTIVE', now(), now()),
    (5, 12, '2026-06-01', '돈과 일을 대하는 태도를 현실적인 언어로 다시 점검하게 합니다.', 1, 5, 'ACTIVE', now(), now()),
    (6, 1, '2026-07-01', '돈을 대하는 태도와 생활 습관을 다시 정리하기 좋은 책입니다.', 1, 1, 'ACTIVE', now(), now()),
    (7, 2, '2026-07-01', '작은 실행을 부담 없이 시작하고 싶은 7월에 잘 맞습니다.', 1, 2, 'ACTIVE', now(), now()),
    (8, 6, '2026-07-01', '한 달 동안 정말 중요한 일 하나를 고르는 연습에 어울립니다.', 1, 3, 'ACTIVE', now(), now()),
    (9, 7, '2026-07-01', '현금흐름과 선택의 속도를 차분히 점검하게 합니다.', 1, 4, 'ACTIVE', now(), now()),
    (10, 3, '2026-07-01', '투자와 성장을 오래 이어가기 위한 마음의 기준을 세우기 좋습니다.', 1, 5, 'ACTIVE', now(), now());

INSERT INTO reading_records (
    id, member_id, book_id, rating, one_line_review, blog_url, representative_image_id,
    status, recorded_at, created_at, updated_at
)
VALUES
    (1, 1, 1, 5, '돈을 버는 기술보다 돈을 대하는 태도를 먼저 보게 됐습니다.', 'https://example.com/growth/reading-001', 11, 'ACTIVE', '2026-06-26 08:30:00+09', '2026-06-26 08:40:00+09', now()),
    (2, 2, 2, 5, '작은 습관을 설계하는 문장이 바로 실행으로 이어졌습니다.', 'https://example.com/growth/reading-002', 12, 'ACTIVE', '2026-06-26 07:50:00+09', '2026-06-26 08:05:00+09', now()),
    (3, 3, 3, 4, '투자는 숫자보다 마음 관리라는 말이 오래 남았습니다.', 'https://example.com/growth/reading-003', 13, 'ACTIVE', '2026-06-25 22:20:00+09', '2026-06-25 22:30:00+09', now()),
    (4, 4, 4, 4, '자산과 부채를 일상 언어로 다시 구분하게 만든 책입니다.', 'https://example.com/growth/reading-004', 14, 'ACTIVE', '2026-06-25 21:10:00+09', '2026-06-25 21:20:00+09', now()),
    (5, 5, 5, 4, '익숙한 선택을 거꾸로 보는 연습이 필요하다는 걸 느꼈습니다.', 'https://example.com/growth/reading-005', 15, 'ACTIVE', '2026-06-24 20:15:00+09', '2026-06-24 20:20:00+09', now()),
    (6, 6, 2, 5, '러닝 루틴에도 바로 적용할 수 있는 구체적인 힌트가 많았습니다.', 'https://example.com/growth/reading-006', 16, 'ACTIVE', '2026-06-24 07:30:00+09', '2026-06-24 07:35:00+09', now()),
    (7, 7, 6, 4, '해야 할 일을 줄이는 것이 집중의 시작이라는 점이 좋았습니다.', 'https://example.com/growth/reading-007', 17, 'ACTIVE', '2026-06-23 23:00:00+09', '2026-06-23 23:10:00+09', now()),
    (8, 8, 3, 5, '가족 재무 대화를 감정적으로 하지 않게 도와준 책입니다.', 'https://example.com/growth/reading-008', 18, 'ACTIVE', '2026-06-23 21:00:00+09', '2026-06-23 21:10:00+09', now()),
    (9, 9, 11, 4, '도구보다 시스템을 먼저 만들어야 한다는 점을 배웠습니다.', 'https://example.com/growth/reading-009', 19, 'ACTIVE', '2026-06-22 19:40:00+09', '2026-06-22 19:45:00+09', now()),
    (10, 10, 10, 4, '꾸준함은 재능보다 환경에 가깝다는 문장이 오래 남았습니다.', 'https://example.com/growth/reading-010', 20, 'ACTIVE', '2026-06-22 08:20:00+09', '2026-06-22 08:25:00+09', now()),
    (11, 1, 7, 4, '현금흐름을 만드는 속도보다 방향을 먼저 점검하게 됐습니다.', 'https://example.com/growth/reading-011', 11, 'ACTIVE', '2026-06-21 10:00:00+09', '2026-06-21 10:10:00+09', now()),
    (12, 2, 1, 5, '수입보다 태도와 루틴이 먼저라는 점이 인상 깊었습니다.', 'https://example.com/growth/reading-012', 12, 'ACTIVE', '2026-06-20 14:30:00+09', '2026-06-20 14:40:00+09', now()),
    (13, 3, 1, 4, '투자 원칙을 일상 언어로 다시 써보게 만든 책입니다.', 'https://example.com/growth/reading-013', 13, 'ACTIVE', '2026-06-19 22:10:00+09', '2026-06-19 22:20:00+09', now()),
    (14, 4, 9, 5, '관계도 연습과 기록이 필요하다는 걸 느꼈습니다.', 'https://example.com/growth/reading-014', 14, 'ACTIVE', '2026-06-18 20:00:00+09', '2026-06-18 20:10:00+09', now()),
    (15, 5, 4, 4, '부동산 공부 전에 재무제표처럼 삶을 봐야겠다고 생각했습니다.', 'https://example.com/growth/reading-015', 15, 'ACTIVE', '2026-06-17 18:30:00+09', '2026-06-17 18:35:00+09', now()),
    (16, 6, 12, 4, '평범한 습관이 오래 쌓이면 큰 차이가 된다는 확신을 줬습니다.', 'https://example.com/growth/reading-016', 16, 'ACTIVE', '2026-06-16 06:50:00+09', '2026-06-16 07:00:00+09', now()),
    (17, 7, 2, 5, '커리어 전환도 아주 작은 반복으로 시작해야겠다고 느꼈습니다.', 'https://example.com/growth/reading-017', 17, 'ACTIVE', '2026-06-15 22:10:00+09', '2026-06-15 22:20:00+09', now()),
    (18, 8, 8, 3, '조급한 마음을 다스리고 목표를 다시 쓰게 만든 책입니다.', 'https://example.com/growth/reading-018', 18, 'ACTIVE', '2026-06-14 15:20:00+09', '2026-06-14 15:30:00+09', now()),
    (19, 9, 5, 4, '생각만 하던 실험을 작게 공개해 보기로 했습니다.', 'https://example.com/growth/reading-019', 19, 'ACTIVE', '2026-06-13 16:00:00+09', '2026-06-13 16:05:00+09', now()),
    (20, 10, 1, 4, '돈과 마음챙김이 멀지 않다는 걸 알게 됐습니다.', 'https://example.com/growth/reading-020', 20, 'ACTIVE', '2026-06-12 09:10:00+09', '2026-06-12 09:15:00+09', now()),
    (21, 1, 2, 5, '습관을 작게 만드는 일이 결국 오래 가는 힘이라는 걸 다시 확인했습니다.', 'https://example.com/growth/reading-021', 11, 'ACTIVE', '2026-07-01 08:20:00+09', '2026-07-01 08:25:00+09', now()),
    (22, 2, 6, 4, '이번 달에는 할 일을 더하는 대신 덜어내는 기준을 세워보려 합니다.', 'https://example.com/growth/reading-022', 12, 'ACTIVE', '2026-07-01 09:10:00+09', '2026-07-01 09:15:00+09', now()),
    (23, 3, 3, 5, '시장보다 내 감정의 속도를 먼저 봐야 한다는 문장이 남았습니다.', 'https://example.com/growth/reading-023', 13, 'ACTIVE', '2026-07-01 10:00:00+09', '2026-07-01 10:05:00+09', now()),
    (24, 4, 9, 4, '좋은 관계도 결국 매일 쓰는 말과 태도에서 시작된다고 느꼈습니다.', 'https://example.com/growth/reading-024', 14, 'ACTIVE', '2026-07-01 11:10:00+09', '2026-07-01 11:15:00+09', now()),
    (25, 5, 7, 4, '빠른 길을 찾기 전에 내 수입 구조를 더 정확히 봐야겠습니다.', 'https://example.com/growth/reading-025', 15, 'ACTIVE', '2026-07-01 12:30:00+09', '2026-07-01 12:35:00+09', now()),
    (26, 6, 1, 5, '돈을 다루는 기준을 생활 루틴 안에 넣어야겠다는 생각이 들었습니다.', 'https://example.com/growth/reading-026', 16, 'ACTIVE', '2026-07-01 13:20:00+09', '2026-07-01 13:25:00+09', now()),
    (27, 7, 11, 4, '도구를 모으기보다 내 일에 맞는 루틴 하나를 고르는 게 먼저였습니다.', 'https://example.com/growth/reading-027', 17, 'ACTIVE', '2026-07-01 14:10:00+09', '2026-07-01 14:15:00+09', now()),
    (28, 8, 12, 4, '가족과 돈 이야기를 더 담백하게 나눌 수 있는 문장이 많았습니다.', 'https://example.com/growth/reading-028', 18, 'ACTIVE', '2026-07-01 15:40:00+09', '2026-07-01 15:45:00+09', now()),
    (29, 9, 5, 4, '생각을 멈추지 않되 작게라도 밖으로 꺼내야 변화가 시작됐습니다.', 'https://example.com/growth/reading-029', 19, 'ACTIVE', '2026-07-01 16:20:00+09', '2026-07-01 16:25:00+09', now()),
    (30, 10, 10, 5, '꾸준함을 의지 문제가 아니라 환경 설계로 보게 됐습니다.', 'https://example.com/growth/reading-030', 20, 'ACTIVE', '2026-07-01 17:00:00+09', '2026-07-01 17:05:00+09', now());

UPDATE image_assets
SET owner_member_id = ((id - 11) % 10) + 1
WHERE id BETWEEN 11 AND 20;

INSERT INTO monthly_action_plans (id, member_id, target_month, title, content, status, created_at, updated_at)
VALUES
    (1, 1, '2026-06-01', '6월 실행 선언', '매일 아침 20분 독서 후 한 문장 실행 메모를 남긴다.', 'ACTIVE', '2026-06-02 08:00:00+09', now()),
    (2, 2, '2026-06-01', '브랜드 실험', '고객 인터뷰 5개를 정리하고 다음 제품 가설을 좁힌다.', 'ACTIVE', '2026-06-02 08:10:00+09', now()),
    (3, 3, '2026-06-01', '투자 기준 정리', '매수 전 체크리스트를 10개 항목으로 줄인다.', 'ACTIVE', '2026-06-02 08:20:00+09', now()),
    (4, 4, '2026-06-01', '회고 루틴', '매주 일요일 밤 30분 회고를 캘린더에 고정한다.', 'ACTIVE', '2026-06-02 08:30:00+09', now()),
    (5, 5, '2026-06-01', '임장 기록', '관심 지역 2곳을 방문하고 숫자와 느낌을 분리해 기록한다.', 'ACTIVE', '2026-06-02 08:40:00+09', now()),
    (6, 6, '2026-06-01', '러닝 독서 루틴', '주 3회 러닝 후 10분 독서 메모를 남긴다.', 'ACTIVE', '2026-06-02 08:50:00+09', now()),
    (7, 7, '2026-06-01', '포트폴리오', '작은 프로젝트 하나를 공개 가능한 문서로 정리한다.', 'ACTIVE', '2026-06-02 09:00:00+09', now()),
    (8, 8, '2026-06-01', '가계 루틴', '주 1회 가족과 지출 리뷰 시간을 가진다.', 'ACTIVE', '2026-06-02 09:10:00+09', now()),
    (9, 9, '2026-06-01', 'AI 실험 기록', '업무 자동화 실험 하나를 글로 정리한다.', 'ACTIVE', '2026-06-02 09:20:00+09', now()),
    (10, 10, '2026-06-01', '명상과 회고', '평일 10분 명상 후 감정 키워드 하나를 적는다.', 'ACTIVE', '2026-06-02 09:30:00+09', now()),
    (11, 1, '2026-07-01', '7월 독서 실행', '매일 아침 책에서 고른 한 문장을 업무 시작 전에 실행 메모로 옮긴다.', 'ACTIVE', '2026-07-01 08:00:00+09', now()),
    (12, 2, '2026-07-01', '고객 대화 정리', '이번 달에는 고객 인터뷰를 3개만 진행하고, 반복해서 나온 표현을 제품 문장으로 정리한다.', 'ACTIVE', '2026-07-01 08:10:00+09', now()),
    (13, 3, '2026-07-01', '투자 기록 단순화', '매수와 매도 이유를 각각 세 문장 안으로 적고, 감정이 들어간 표현은 따로 표시한다.', 'ACTIVE', '2026-07-01 08:20:00+09', now()),
    (14, 4, '2026-07-01', '회고 질문 고정', '매주 일요일 밤에 잘한 일 하나와 줄일 일 하나만 적어 다음 주 계획에 반영한다.', 'ACTIVE', '2026-07-01 08:30:00+09', now()),
    (15, 5, '2026-07-01', '현금흐름 점검', '고정비를 한 번 더 확인하고, 이번 달에는 새 지출을 만들기 전에 하루를 두고 결정한다.', 'ACTIVE', '2026-07-01 08:40:00+09', now()),
    (16, 6, '2026-07-01', '러닝과 독서', '주 3회 러닝 후 15분 독서를 이어가고, 몸 상태가 좋지 않은 날에는 산책과 한 페이지 읽기로 낮춘다.', 'ACTIVE', '2026-07-01 08:50:00+09', now()),
    (17, 7, '2026-07-01', '포트폴리오 공개', '작은 자동화 프로젝트 하나를 정리해 링크로 공유하고 피드백 받을 질문 세 개를 준비한다.', 'ACTIVE', '2026-07-01 09:00:00+09', now()),
    (18, 8, '2026-07-01', '가족 재무 대화', '주말마다 20분씩 가족 지출을 같이 보고, 줄일 항목보다 유지하고 싶은 기준을 먼저 이야기한다.', 'ACTIVE', '2026-07-01 09:10:00+09', now()),
    (19, 9, '2026-07-01', 'AI 메모 실험', '독서 메모를 AI로 요약한 뒤 내가 직접 다시 고친 문장만 아카이브에 남긴다.', 'ACTIVE', '2026-07-01 09:20:00+09', now()),
    (20, 10, '2026-07-01', '마음챙김 루틴', '평일 점심 전에 5분 호흡을 하고, 감정 키워드를 독서 문장 옆에 함께 적는다.', 'ACTIVE', '2026-07-01 09:30:00+09', now());

INSERT INTO image_assets (id, owner_member_id, bucket, object_key, public_url, image_type, mime_type, width, height, size_bytes, created_at)
VALUES
    (21, null, 'local-demo', 'meetings/2026-01-reading.jpg', '/images/korean-bookclub-discussion.jpg', 'MEETING_COVER', 'image/jpeg', 1200, 900, 300000, '2026-01-05 09:00:00+09'),
    (22, null, 'local-demo', 'meetings/2026-02-action.jpg', '/images/meeting-table.jpg', 'MEETING_COVER', 'image/jpeg', 1200, 900, 290000, '2026-02-03 09:00:00+09'),
    (23, null, 'local-demo', 'meetings/2026-03-small.jpg', '/images/gathering-people.jpg', 'MEETING_COVER', 'image/jpeg', 1200, 900, 285000, '2026-03-03 09:00:00+09'),
    (24, null, 'local-demo', 'meetings/2026-04-reading.jpg', '/images/korean-reading-table.jpg', 'MEETING_COVER', 'image/jpeg', 1200, 900, 280000, '2026-04-03 09:00:00+09'),
    (25, null, 'local-demo', 'meetings/2026-05-action.jpg', '/images/hero-bookclub-study.png', 'MEETING_COVER', 'image/png', 1200, 900, 310000, '2026-05-03 09:00:00+09'),
    (26, null, 'local-demo', 'meetings/2026-06-small.jpg', '/images/hero-bookclub-discussion.png', 'MEETING_COVER', 'image/png', 1200, 900, 315000, '2026-06-03 09:00:00+09'),
    (31, 2, 'local-demo', 'reviews/2026-01-reading-01.jpg', '/images/korean-bookclub-discussion.jpg', 'MEETING_REVIEW', 'image/jpeg', 1200, 900, 300000, '2026-01-11 13:30:00+09'),
    (32, 4, 'local-demo', 'reviews/2026-01-action-01.jpg', '/images/meeting-table.jpg', 'MEETING_REVIEW', 'image/jpeg', 1200, 900, 290000, '2026-01-25 13:20:00+09'),
    (33, 6, 'local-demo', 'reviews/2026-02-reading-01.jpg', '/images/member-books.jpg', 'MEETING_REVIEW', 'image/jpeg', 1200, 900, 250000, '2026-02-08 13:20:00+09'),
    (34, 8, 'local-demo', 'reviews/2026-02-action-01.jpg', '/images/gathering-people.jpg', 'MEETING_REVIEW', 'image/jpeg', 1200, 900, 285000, '2026-02-22 13:30:00+09'),
    (35, 3, 'local-demo', 'reviews/2026-03-reading-01.jpg', '/images/korean-reading-table.jpg', 'MEETING_REVIEW', 'image/jpeg', 1200, 900, 280000, '2026-03-08 13:10:00+09'),
    (36, 5, 'local-demo', 'reviews/2026-03-action-01.jpg', '/images/hero-bookclub-study.png', 'MEETING_REVIEW', 'image/png', 1200, 900, 310000, '2026-03-22 13:20:00+09'),
    (37, 7, 'local-demo', 'reviews/2026-04-reading-01.jpg', '/images/hero-bookclub-discussion.png', 'MEETING_REVIEW', 'image/png', 1200, 900, 315000, '2026-04-12 13:10:00+09'),
    (38, 9, 'local-demo', 'reviews/2026-04-action-01.jpg', '/images/reading-books.jpg', 'MEETING_REVIEW', 'image/jpeg', 1200, 900, 240000, '2026-04-26 13:20:00+09'),
    (39, 1, 'local-demo', 'reviews/2026-05-reading-01.jpg', '/images/korean-reading-table.jpg', 'MEETING_REVIEW', 'image/jpeg', 1200, 900, 260000, '2026-05-10 13:10:00+09'),
    (40, 10, 'local-demo', 'reviews/2026-05-action-01.jpg', '/images/book-shelf.jpg', 'MEETING_REVIEW', 'image/jpeg', 1200, 900, 270000, '2026-05-24 13:20:00+09');

INSERT INTO meetings (
    id, meeting_type, title, description, meeting_at, region_text, detail_address, capacity,
    cost_amount, cover_image_id, host_member_id, status, target_month, is_auto_generated, created_at, updated_at
)
VALUES
    (1, 'REGULAR_READING', '1월 독서기록모임', '1월에 남긴 독서기록을 함께 읽고 다음 실행 문장을 고릅니다.', '2026-01-11 10:00:00+09', '서울 성수', '성수 북라운지 2층', 16, 0, 21, 1, 'HELD', '2026-01-01', true, '2026-01-01 00:10:00+09', now()),
    (2, 'REGULAR_ACTION', '1월 실행수다모임', '월초에 정한 실행목표를 함께 이야기하고 편하게 수다 나누는 자리입니다.', '2026-01-25 10:00:00+09', '서울 성수', '성수 북라운지 2층', 16, 0, 22, 1, 'HELD', '2026-01-01', true, '2026-01-01 00:10:00+09', now()),
    (3, 'SMALL', '1월 현금흐름 노트 모임', '가계부와 현금흐름을 조용히 점검하는 소소모임입니다.', '2026-01-18 14:00:00+09', '서울 합정', '합정 조용한 카페', 8, 8000, 23, 5, 'HELD', '2026-01-01', false, '2026-01-07 21:00:00+09', now()),
    (4, 'REGULAR_READING', '2월 독서기록모임', '2월 독서기록에서 각자 실행한 문장을 나눕니다.', '2026-02-08 10:00:00+09', '서울 성수', '성수 북라운지 2층', 16, 0, 24, 1, 'HELD', '2026-02-01', true, '2026-02-01 00:10:00+09', now()),
    (5, 'REGULAR_ACTION', '2월 실행수다모임', '월초에 적어둔 실행목표를 꺼내 놓고 서로의 한 달 이야기를 나눕니다.', '2026-02-22 10:00:00+09', '서울 성수', '성수 북라운지 2층', 16, 0, 25, 1, 'HELD', '2026-02-01', true, '2026-02-01 00:10:00+09', now()),
    (6, 'SMALL', '2월 러닝과 독서 루틴 모임', '러닝과 독서 루틴을 함께 설계하는 소소모임입니다.', '2026-02-15 08:30:00+09', '서울 잠실', '석촌호수 동쪽 입구', 7, 0, 26, 6, 'HELD', '2026-02-01', false, '2026-02-04 20:20:00+09', now()),
    (7, 'REGULAR_READING', '3월 독서기록모임', '3월 독서기록을 바탕으로 투자와 습관의 기준을 정리합니다.', '2026-03-08 10:00:00+09', '서울 성수', '성수 북라운지 2층', 16, 0, 21, 1, 'HELD', '2026-03-01', true, '2026-03-01 00:10:00+09', now()),
    (8, 'REGULAR_ACTION', '3월 실행수다모임', '각자 정한 실행목표가 한 달 동안 어떻게 흘러갔는지 편하게 나눕니다.', '2026-03-22 10:00:00+09', '서울 성수', '성수 북라운지 2층', 16, 0, 22, 1, 'HELD', '2026-03-01', true, '2026-03-01 00:10:00+09', now()),
    (9, 'SMALL', '3월 투자 원칙 정리 모임', '흔들리지 않는 투자 원칙을 각자 한 장으로 정리합니다.', '2026-03-15 15:00:00+09', '서울 여의도', '여의도 스터디룸 A', 8, 10000, 23, 3, 'HELD', '2026-03-01', false, '2026-03-05 20:10:00+09', now()),
    (10, 'REGULAR_READING', '4월 독서기록모임', '4월 독서기록에서 관계와 일의 태도를 돌아봅니다.', '2026-04-12 10:00:00+09', '서울 성수', '성수 북라운지 2층', 16, 0, 24, 1, 'HELD', '2026-04-01', true, '2026-04-01 00:10:00+09', now()),
    (11, 'REGULAR_ACTION', '4월 실행수다모임', '월초의 실행목표를 돌아보고 잘된 일과 막힌 일을 가볍게 이야기합니다.', '2026-04-26 10:00:00+09', '서울 성수', '성수 북라운지 2층', 16, 0, 25, 1, 'HELD', '2026-04-01', true, '2026-04-01 00:10:00+09', now()),
    (12, 'SMALL', '4월 회고 글쓰기 모임', '독서 후 떠오른 문장을 짧은 회고 글로 남깁니다.', '2026-04-19 14:30:00+09', '서울 연남', '연남 워크룸', 6, 7000, 26, 4, 'HELD', '2026-04-01', false, '2026-04-06 22:00:00+09', now()),
    (13, 'REGULAR_READING', '5월 독서기록모임', '5월 독서기록을 함께 읽고 오래 남을 실행을 고릅니다.', '2026-05-10 10:00:00+09', '서울 성수', '성수 북라운지 2층', 16, 0, 21, 1, 'HELD', '2026-05-01', true, '2026-05-01 00:10:00+09', now()),
    (14, 'REGULAR_ACTION', '5월 실행수다모임', '정해둔 실행목표를 소재로 서로의 일상과 실행 이야기를 나눕니다.', '2026-05-24 10:00:00+09', '서울 성수', '성수 북라운지 2층', 16, 0, 22, 1, 'HELD', '2026-05-01', true, '2026-05-01 00:10:00+09', now()),
    (15, 'SMALL', '5월 부동산 임장 기록 모임', '관심 지역 임장 기록을 숫자와 느낌으로 나눠 정리합니다.', '2026-05-17 13:30:00+09', '서울 마포', '공덕 스터디룸 B', 7, 12000, 23, 5, 'HELD', '2026-05-01', false, '2026-05-07 21:20:00+09', now()),
    (16, 'REGULAR_READING', '6월 독서기록모임', '6월 독서기록에서 바로 실행한 내용을 공유합니다.', '2026-06-14 10:00:00+09', '서울 성수', '성수 북라운지 2층', 16, 0, 24, 1, 'HELD', '2026-06-01', true, '2026-06-01 00:10:00+09', now()),
    (17, 'REGULAR_ACTION', '6월 실행수다모임', '월초에 설정한 실행목표를 놓고 이야기 나누고 편하게 수다 떠는 자리입니다.', '2026-06-28 10:00:00+09', '서울 성수', '성수 북라운지 2층', 16, 0, 25, 1, 'HELD', '2026-06-01', true, '2026-06-01 00:10:00+09', now()),
    (18, 'SMALL', '6월 AI 독서 기록 모임', 'AI 도구로 독서 메모를 정리하는 방식을 함께 실험합니다.', '2026-06-21 15:00:00+09', '서울 강남', '강남 스터디룸 C', 8, 10000, 26, 9, 'HELD', '2026-06-01', false, '2026-06-06 20:30:00+09', now()),
    (19, 'REGULAR_READING', '7월 독서기록모임', '7월에 남긴 독서기록을 함께 읽고 다음 실행 문장을 고릅니다.', '2026-07-12 10:00:00+09', '서울 성수', '성수 북라운지 2층', 16, 0, 24, 1, 'SCHEDULED', '2026-07-01', true, '2026-07-01 00:10:00+09', now()),
    (20, 'REGULAR_ACTION', '7월 실행수다모임', '월초에 설정한 실행목표를 놓고 이야기 나누고 편하게 수다 떠는 자리입니다.', '2026-07-26 10:00:00+09', '서울 성수', '성수 북라운지 2층', 16, 0, 25, 1, 'SCHEDULED', '2026-07-01', true, '2026-07-01 00:10:00+09', now()),
    (21, 'SMALL', '7월 아침 독서 루틴 모임', '아침 시간을 조용히 정리하고 짧은 독서 루틴을 함께 설계합니다.', '2026-07-05 09:30:00+09', '서울 미사', '미사역 근처 조용한 카페', 8, 8000, 26, 6, 'SCHEDULED', '2026-07-01', false, '2026-07-01 10:30:00+09', now()),
    (22, 'SMALL', '7월 현금흐름 점검 모임', '이번 달 지출과 저축 기준을 각자의 속도에 맞게 점검합니다.', '2026-07-19 14:00:00+09', '서울 합정', '합정 스터디룸 3층', 8, 10000, 23, 5, 'SCHEDULED', '2026-07-01', false, '2026-07-01 11:00:00+09', now());

INSERT INTO meeting_attendances (meeting_id, member_id, status, created_at, updated_at)
SELECT id AS meeting_id,
       ((id + attendee_offset - 1) % 10) + 1 AS member_id,
       'JOINED',
       meeting_at - interval '5 days' + attendee_offset * interval '1 hour',
       now()
FROM meetings
CROSS JOIN generate_series(0, 4) AS attendee_offset;

INSERT INTO meeting_reviews (
    id, meeting_id, member_id, title, content, representative_image_id, status, created_at, updated_at
)
VALUES
    (1, 1, 2, '새해 첫 독서기록을 함께 열어본 시간', '1월 독서기록모임에서는 각자 새해에 붙잡고 싶은 문장을 나눴습니다. 혼자 읽을 때는 지나쳤던 부분이 다른 사람의 실행 사례를 들으며 더 선명해졌고, 저도 이번 달에는 책 한 권에서 한 가지 행동만 고르기로 했습니다.', 31, 'ACTIVE', '2026-01-11 13:30:00+09', now()),
    (2, 2, 4, '계획을 줄이니 오히려 실행이 보였습니다', '하고 싶은 일을 많이 적는 대신 이번 달에 반드시 지킬 행동 하나를 정했습니다. 모임에서 서로 질문을 주고받으니 계획이 더 현실적인 크기로 내려왔고, 월말에 확인할 기준도 분명해졌습니다.', 32, 'ACTIVE', '2026-01-25 13:20:00+09', now()),
    (3, 3, 5, '현금흐름을 숫자와 감정으로 나눠 본 날', '가계부를 함께 보며 지출을 무조건 줄이는 이야기가 아니라 어떤 선택이 나를 편하게 만드는지까지 이야기했습니다. 다음 달에는 고정비와 변동비를 따로 적어 보기로 했습니다.', 33, 'ACTIVE', '2026-01-18 17:00:00+09', now()),
    (4, 4, 6, '습관을 작게 만드는 대화가 좋았습니다', '아주 작은 습관의 힘을 읽은 성장하는 사람들이 많아서 실천 사례가 풍성했습니다. 아침 루틴, 출근길 독서, 저녁 회고처럼 각자 다른 방식이었지만 공통점은 부담을 줄였다는 점이었습니다.', 33, 'ACTIVE', '2026-02-08 13:20:00+09', now()),
    (5, 5, 8, '실행목표 이야기가 자연스럽게 이어졌습니다', '월초에 적어둔 실행목표를 꺼내 놓고 각자 어떻게 지내고 있는지 편하게 이야기했습니다. 목표를 평가하는 느낌보다 서로의 생활을 듣는 시간이어서 부담이 적었습니다.', 34, 'ACTIVE', '2026-02-22 13:30:00+09', now()),
    (6, 6, 6, '뛰고 읽는 리듬을 같이 만든 아침', '러닝 후 짧게 모여 각자의 독서 루틴을 나눴습니다. 운동과 독서를 경쟁적으로 하는 분위기가 아니라 하루를 덜 흩어지게 만드는 방법으로 이야기해서 편안했습니다.', 35, 'ACTIVE', '2026-02-15 11:00:00+09', now()),
    (7, 7, 3, '투자 이야기도 결국 습관 이야기였습니다', '돈의 심리학을 이야기하며 수익률보다 오래 버티는 태도에 대해 많이 나눴습니다. 각자의 투자 원칙을 한 문장으로 적어보니 막연했던 기준이 조금 정리됐습니다.', 35, 'ACTIVE', '2026-03-08 13:10:00+09', now()),
    (8, 8, 5, '수다 속에서 목표가 더 선명해졌습니다', '실행목표를 딱딱하게 점검하기보다 한 달 동안 있었던 일을 이야기했습니다. 서로의 시행착오를 들으면서 내 목표를 조금 더 현실적인 방향으로 바라보게 됐습니다.', 36, 'ACTIVE', '2026-03-22 13:20:00+09', now()),
    (9, 9, 3, '투자 원칙을 한 장으로 정리했습니다', '각자 흔들렸던 순간과 지켜낸 기준을 나눴습니다. 좋은 정보보다 먼저 나에게 맞는 원칙을 만들어야 한다는 이야기가 오래 남았습니다.', 36, 'ACTIVE', '2026-03-15 18:00:00+09', now()),
    (10, 10, 7, '관계와 일의 태도를 돌아본 독서 모임', '인간관계론을 읽고 대화 습관을 돌아보는 시간이었습니다. 책의 문장을 그대로 외우는 것보다 이번 주에 바꿀 말투 하나를 고르는 방식이 좋았습니다.', 37, 'ACTIVE', '2026-04-12 13:10:00+09', now()),
    (11, 11, 9, '4월 실행계획은 회복까지 포함했습니다', '계획을 세울 때 실패했을 때의 회복 방법까지 같이 정했습니다. 완벽하게 지키는 것보다 다시 돌아오는 장치를 만드는 게 더 중요하다는 걸 배웠습니다.', 38, 'ACTIVE', '2026-04-26 13:20:00+09', now()),
    (12, 12, 4, '짧은 회고 글이 생각보다 깊었습니다', '긴 글을 쓰려고 하지 않고 책에서 남은 한 문장으로 시작했습니다. 서로의 글을 읽으며 같은 책도 전혀 다른 삶의 장면으로 이어진다는 점이 좋았습니다.', 37, 'ACTIVE', '2026-04-19 17:20:00+09', now()),
    (13, 13, 1, '5월 독서기록에서 실행의 흔적을 봤습니다', '이번 달 기록에는 책을 읽은 감상보다 실제로 해본 일들이 많이 담겨 있었습니다. 작은 실행을 공유하는 분위기가 자연스러워져서 모임이 더 단단해진 느낌이었습니다.', 39, 'ACTIVE', '2026-05-10 13:10:00+09', now()),
    (14, 14, 10, '실행하지 못한 이유까지 말할 수 있었습니다', '잘한 일만 이야기하지 않고 막힌 이유도 편하게 나눴습니다. 덕분에 계획을 더 현실적으로 고치게 됐고, 다음 달에는 기준을 낮춰 꾸준히 가보기로 했습니다.', 40, 'ACTIVE', '2026-05-24 13:20:00+09', now()),
    (15, 15, 5, '임장 기록을 감정과 숫자로 분리했습니다', '동네를 보고 온 느낌과 실제 숫자를 따로 적어보니 판단이 덜 흔들렸습니다. 혼자서는 놓쳤을 질문을 많이 받아서 다음 임장 때 볼 기준이 생겼습니다.', 39, 'ACTIVE', '2026-05-17 17:00:00+09', now()),
    (16, 16, 2, '6월 독서기록은 더 담백했습니다', '최근 기록을 함께 읽으며 멋진 문장보다 실제로 남은 변화에 집중했습니다. 한 사람씩 이번 달에 바꾼 행동을 말하는 시간이 특히 좋았습니다.', 31, 'ACTIVE', '2026-06-14 13:10:00+09', now()),
    (17, 18, 9, 'AI로 독서 메모를 정리해 본 소소모임', '각자 쓰는 독서 메모 방식을 보여주고 AI로 요약하거나 질문을 뽑는 실험을 했습니다. 도구가 기록을 대신하는 것이 아니라 다시 생각하게 돕는 쪽으로 쓰는 게 좋겠다고 느꼈습니다.', 38, 'ACTIVE', '2026-06-21 18:00:00+09', now()),
    (18, 17, 4, '실행계획을 편하게 꺼내본 시간', '월초에 쓴 목표를 평가하듯 보지 않고 실제 한 달이 어떻게 흘렀는지 이야기했습니다. 완벽하게 지킨 계획보다 다시 이어갈 수 있는 기준을 찾은 시간이었습니다.', 40, 'ACTIVE', '2026-06-28 13:10:00+09', now());

INSERT INTO meeting_review_images (meeting_review_id, image_asset_id, display_order, created_at)
SELECT id, representative_image_id, 1, created_at
FROM meeting_reviews
WHERE representative_image_id IS NOT NULL;

INSERT INTO activity_events (id, member_id, event_type, reference_type, reference_id, visibility, summary, happened_at, created_at)
SELECT id, member_id, 'READING_RECORD_CREATED', 'READING_RECORD', id, 'PUBLIC', one_line_review, recorded_at, created_at
FROM reading_records;

INSERT INTO activity_events (id, member_id, event_type, reference_type, reference_id, visibility, summary, happened_at, created_at)
SELECT 100 + id, member_id, 'MEETING_REVIEW_CREATED', 'MEETING_REVIEW', id, 'PUBLIC', title, created_at, created_at
FROM meeting_reviews;

SELECT setval(pg_get_serial_sequence('image_assets', 'id'), (SELECT max(id) FROM image_assets));
SELECT setval(pg_get_serial_sequence('members', 'id'), (SELECT max(id) FROM members));
SELECT setval(pg_get_serial_sequence('oauth_accounts', 'id'), (SELECT max(id) FROM oauth_accounts));
SELECT setval(pg_get_serial_sequence('invite_codes', 'id'), (SELECT max(id) FROM invite_codes));
SELECT setval(pg_get_serial_sequence('books', 'id'), (SELECT max(id) FROM books));
SELECT setval(pg_get_serial_sequence('recommended_books', 'id'), (SELECT max(id) FROM recommended_books));
SELECT setval(pg_get_serial_sequence('reading_records', 'id'), (SELECT max(id) FROM reading_records));
SELECT setval(pg_get_serial_sequence('monthly_action_plans', 'id'), (SELECT max(id) FROM monthly_action_plans));
SELECT setval(pg_get_serial_sequence('meetings', 'id'), (SELECT max(id) FROM meetings));
SELECT setval(pg_get_serial_sequence('meeting_reviews', 'id'), (SELECT max(id) FROM meeting_reviews));
SELECT setval(pg_get_serial_sequence('meeting_review_images', 'id'), (SELECT max(id) FROM meeting_review_images));
SELECT setval(pg_get_serial_sequence('activity_events', 'id'), (SELECT max(id) FROM activity_events));
