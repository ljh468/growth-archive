package com.growtharchive.service.participation;

public record AdminParticipationMemberView(
    Long memberId,
    String displayName,
    String nickname,
    String profileImageUrl,
    Long readingRecordCount,
    boolean hasActionPlan,
    boolean calculationTarget,
    boolean completed,
    boolean coffeeSupportTarget,
    String coffeeSupportItem,
    String adminMemo
) {
}
