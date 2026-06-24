package com.growtharchive.service.profile;

import java.util.List;

public record MyProfile(
    Long memberId,
    String nickname,
    String realName,
    String displayNameType,
    String displayName,
    String profileImageUrl,
    Long profileImageId,
    String oneLineIntro,
    String job,
    List<Long> interestTagIds,
    String futureMeAt50,
    String joinReason,
    String currentConcern,
    String threeYearGoal
) {
}
