package com.growtharchive.service.review;

import java.util.List;

public record MeetingReviewCommand(
    Long meetingId,
    String title,
    String content,
    List<Long> imageIds
) {
}
