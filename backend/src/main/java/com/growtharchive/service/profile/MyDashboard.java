package com.growtharchive.service.profile;

import java.util.List;

public record MyDashboard(
    DashboardProfile profile,
    DashboardParticipation participation,
    GrowthStats quickStats,
    List<ActivitySummary> recentActivities
) {
    public record DashboardProfile(
        Long memberId,
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
}
