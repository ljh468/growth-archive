INSERT INTO interest_tags (name, slug, display_order, is_active)
VALUES
    ('독서', 'reading', 1, true),
    ('실행습관', 'habit', 2, true),
    ('재테크', 'personal-finance', 3, true),
    ('투자', 'investment', 4, true),
    ('주식', 'stocks', 5, true),
    ('부동산', 'real-estate', 6, true),
    ('사업', 'business', 7, true),
    ('창업', 'startup', 8, true),
    ('경제적 자유', 'financial-freedom', 9, true),
    ('커리어', 'career', 10, true),
    ('생산성', 'productivity', 11, true),
    ('시간관리', 'time-management', 12, true),
    ('리더십', 'leadership', 13, true),
    ('글쓰기', 'writing', 14, true),
    ('AI', 'ai', 15, true),
    ('브랜딩', 'branding', 16, true),
    ('운동', 'exercise', 17, true),
    ('러닝', 'running', 18, true),
    ('건강', 'health', 19, true),
    ('마음챙김', 'mindfulness', 20, true)
ON CONFLICT (slug) DO UPDATE
SET name = excluded.name,
    display_order = excluded.display_order,
    is_active = excluded.is_active;

UPDATE interest_tags
SET is_active = false
WHERE slug NOT IN (
    'reading',
    'habit',
    'personal-finance',
    'investment',
    'stocks',
    'real-estate',
    'business',
    'startup',
    'financial-freedom',
    'career',
    'productivity',
    'time-management',
    'leadership',
    'writing',
    'ai',
    'branding',
    'exercise',
    'running',
    'health',
    'mindfulness'
);
