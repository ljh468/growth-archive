package com.growtharchive.service.meeting;

import java.time.OffsetDateTime;
import java.util.List;

public record MeetingSummary(
    Long id,
    String meetingType,
    String title,
    String description,
    OffsetDateTime meetingAt,
    String locationRegion,
    Integer capacity,
    Integer feeAmount,
    String coverImageUrl,
    String status,
    int attendeeCount,
    List<String> attendeePreviewImageUrls
) {
}
