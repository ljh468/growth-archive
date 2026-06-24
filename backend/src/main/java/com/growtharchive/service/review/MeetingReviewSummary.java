package com.growtharchive.service.review;

import java.time.OffsetDateTime;

public record MeetingReviewSummary(
    Long id,
    Long meetingId,
    String meetingTitle,
    Long memberId,
    String memberDisplayName,
    String memberProfileImageUrl,
    String title,
    String contentSummary,
    String representativeImageUrl,
    String status,
    OffsetDateTime createdAt
) {
}
