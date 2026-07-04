package com.growtharchive.service.book;

import java.time.LocalDate;

public record BookSummary(
    Long id,
    String title,
    String authorsText,
    String publisher,
    LocalDate publishedDate,
    String thumbnailUrl,
    String status
) {
}
