package com.growtharchive.service.review;

import java.time.OffsetDateTime;
import java.util.List;

public record MeetingReviewDetail(
    Long id,
    Long meetingId,
    String meetingTitle,
    Long memberId,
    String memberDisplayName,
    String memberProfileImageUrl,
    String title,
    String content,
    String status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    boolean canEdit,
    List<ReviewImageView> images
) {
}
