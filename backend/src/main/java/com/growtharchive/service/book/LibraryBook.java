package com.growtharchive.service.book;

public record LibraryBook(
    Long id,
    String title,
    String authorsText,
    String publisher,
    String thumbnailUrl,
    Long readingRecordCount,
    Double averageRating
) {
}
