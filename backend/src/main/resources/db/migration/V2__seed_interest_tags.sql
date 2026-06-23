INSERT INTO interest_tags (name, slug, display_order, is_active)
VALUES
    ('사업', 'business', 1, true),
    ('창업', 'startup', 2, true),
    ('투자', 'investment', 3, true),
    ('부동산', 'real-estate', 4, true),
    ('독서', 'reading', 5, true),
    ('커리어', 'career', 6, true),
    ('AI', 'ai', 7, true),
    ('개발', 'development', 8, true),
    ('마케팅', 'marketing', 9, true),
    ('운동', 'exercise', 10, true),
    ('건강', 'health', 11, true),
    ('인간관계', 'relationships', 12, true),
    ('경제적 자유', 'financial-freedom', 13, true)
ON CONFLICT (slug) DO NOTHING;
