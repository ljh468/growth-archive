package com.growtharchive.service.book;

import java.time.LocalDate;

public record RecommendedBookView(
    Long id,
    Long bookId,
    String title,
    String authorsText,
    String publisher,
    String thumbnailUrl,
    String reason,
    Integer displayOrder,
    LocalDate targetMonth,
    String status
) {
}
