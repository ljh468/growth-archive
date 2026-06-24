package com.growtharchive.service.profile;

import com.growtharchive.service.reading.ReadingRecordDetail;
import java.util.List;

public record ProfileDetail(
    Long memberId,
    String displayName,
    String profileImageUrl,
    String oneLineIntro,
    List<String> interestTags,
    String futureMeAt50,
    GrowthStats growthStats,
    List<ReadingRecordDetail> recentReadingRecords,
    List<ActivitySummary> recentMeetingReviews,
    List<ActivitySummary> recentPublicActivities,
    MemberOnlyProfile memberOnly
) {
    public record MemberOnlyProfile(
        String job,
        String joinReason,
        String currentConcern,
        String threeYearGoal,
        List<ActivitySummary> recentActionPlans,
        List<ActivitySummary> recentReflections
    ) {
    }
}
