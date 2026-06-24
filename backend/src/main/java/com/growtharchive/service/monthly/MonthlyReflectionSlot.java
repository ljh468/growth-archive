package com.growtharchive.service.monthly;

import java.time.LocalDate;

public record MonthlyReflectionSlot(
    LocalDate targetMonth,
    boolean writable,
    MonthlyReflectionView reflection
) {
}
