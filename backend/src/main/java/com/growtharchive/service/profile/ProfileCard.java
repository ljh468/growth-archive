package com.growtharchive.service.profile;

import java.util.List;

public record ProfileCard(
    Long memberId,
    String displayName,
    String profileImageUrl,
    String oneLineIntro,
    List<String> interestTags,
    String futureMeAt50Summary,
    GrowthStats growthStats,
    ActivitySummary recentPublicActivity
) {
}
