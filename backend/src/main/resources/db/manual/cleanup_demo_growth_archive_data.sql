-- DEV ONLY.
-- This removes all demo and user-created business data while preserving schema,
-- Flyway history, and required default seed data.

BEGIN;

TRUNCATE TABLE
    admin_audit_logs,
    participation_admin_notes,
    activity_events,
    recommended_books,
    meeting_review_images,
    meeting_reviews,
    meeting_attendances,
    meetings,
    monthly_reflections,
    monthly_action_plans,
    reading_records,
    books,
    member_interest_tags,
    invite_codes,
    oauth_accounts,
    member_join_intro_sources,
    members,
    image_assets
RESTART IDENTITY CASCADE;

INSERT INTO invite_codes (code_hash, code_preview, role, is_active, created_by_member_id, created_at)
VALUES
    ('94ee059335e587e501cc4bf90613e0814f00a7b08bc7c648fd865a2af6a22cc2', 'test', 'MEMBER', true, null, now()),
    ('835d6dc88b708bc646d6db82c853ef4182fabbd4a8de59c213f2b5ab3ae7d9be', 'admin', 'ADMIN', true, null, now());

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

INSERT INTO recommended_books (book_id, target_month, reason, recommended_by_member_id, display_order, status, created_at, updated_at)
VALUES
    (1, date_trunc('month', now())::date, '돈을 대하는 태도와 생활 습관을 다시 정리하기 좋은 책입니다.', null, 1, 'ACTIVE', now(), now()),
    (2, date_trunc('month', now())::date, '작은 실행을 부담 없이 시작하고 싶은 달에 잘 맞습니다.', null, 2, 'ACTIVE', now(), now()),
    (6, date_trunc('month', now())::date, '한 달 동안 정말 중요한 일 하나를 고르는 연습에 어울립니다.', null, 3, 'ACTIVE', now(), now()),
    (7, date_trunc('month', now())::date, '현금흐름과 선택의 속도를 차분히 점검하게 합니다.', null, 4, 'ACTIVE', now(), now()),
    (3, date_trunc('month', now())::date, '투자와 성장을 오래 이어가기 위한 마음의 기준을 세우기 좋습니다.', null, 5, 'ACTIVE', now(), now());

COMMIT;
