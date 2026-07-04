package com.growtharchive.service.book;

import java.time.LocalDate;

public record BookSearchResult(
    String provider,
    String title,
    String authorsText,
    String publisher,
    LocalDate publishedDate,
    String thumbnailUrl,
    String isbn10,
    String isbn13,
    String sourcePayload
) {
}
