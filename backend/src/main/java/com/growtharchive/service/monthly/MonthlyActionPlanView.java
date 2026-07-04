package com.growtharchive.service.monthly;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MonthlyActionPlanView(
    Long id,
    Long memberId,
    LocalDate targetMonth,
    String title,
    String content,
    String status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
