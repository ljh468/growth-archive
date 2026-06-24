package com.growtharchive.service.meeting;

import java.time.OffsetDateTime;

public record MeetingCommand(
    String title,
    String description,
    OffsetDateTime meetingAt,
    String locationRegion,
    String exactLocation,
    Integer capacity,
    Long thumbnailImageId,
    Integer feeAmount,
    String status
) {
}
