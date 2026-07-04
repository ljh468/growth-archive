package com.growtharchive.service.profile;

import java.time.OffsetDateTime;
import java.util.List;

public record MyDashboard(
    DashboardProfile profile,
    DashboardParticipation participation,
    DashboardReadingRecord currentReadingRecord,
    List<DashboardReadingRecord> currentReadingRecords,
    DashboardActionPlan currentActionPlan,
    GrowthStats quickStats,
    List<ActivitySummary> recentActivities
) {
    public record DashboardProfile(
        Long memberId,
        String role,
        String displayName,
        String profileImageUrl,
        String oneLineIntro
    ) {
    }

    public record DashboardParticipation(
        String month,
        Long readingRecordCount,
        Long actionPlanCount,
        boolean completed,
        boolean coffeeSupportTarget
    ) {
    }

    public record DashboardReadingRecord(
        Long id,
        Long bookId,
        String bookTitle,
        String oneLineReview,
        String blogUrl,
        OffsetDateTime recordedAt
    ) {
    }

    public record DashboardActionPlan(
        Long id,
        String targetMonth,
        String title,
        String content,
        OffsetDateTime updatedAt
    ) {
    }
}
