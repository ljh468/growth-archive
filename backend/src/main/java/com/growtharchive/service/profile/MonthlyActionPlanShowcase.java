package com.growtharchive.service.profile;

import java.time.OffsetDateTime;

public record MonthlyActionPlanShowcase(
    Long id,
    Long memberId,
    String displayName,
    String profileImageUrl,
    String targetMonth,
    String title,
    String content,
    OffsetDateTime updatedAt
) {
}
