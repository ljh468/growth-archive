package com.growtharchive.service.monthly;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MonthlyReflectionView(
    Long id,
    Long memberId,
    LocalDate targetMonth,
    String wellDone,
    String regret,
    String nextFocus,
    String status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
