package com.growtharchive.service.reading;

import java.time.OffsetDateTime;

public record ReadingRecordDetail(
    Long id,
    Long memberId,
    String memberDisplayName,
    String memberProfileImageUrl,
    Long bookId,
    String bookTitle,
    String authorsText,
    String bookThumbnailUrl,
    Integer rating,
    String oneLineReview,
    String blogUrl,
    Long imageId,
    String recordImageUrl,
    String status,
    OffsetDateTime recordedAt,
    OffsetDateTime createdAt
) {
}
