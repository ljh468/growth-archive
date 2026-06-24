package com.growtharchive.service.profile;

public record GrowthStats(
    Long readingRecordCount,
    Long actionPlanCount,
    Long monthlyReflectionCount,
    Long meetingReviewCount,
    Long smallMeetingCreatedCount
) {
}
