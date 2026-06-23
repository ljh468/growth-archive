package com.growtharchive.service.book;

public record RecommendedBookView(
    Long id,
    Long bookId,
    String title,
    String authorsText,
    String publisher,
    String thumbnailUrl,
    String reason,
    Integer displayOrder
) {
}
