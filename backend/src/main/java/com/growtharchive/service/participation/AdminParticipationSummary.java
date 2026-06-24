package com.growtharchive.service.participation;

import java.util.List;

public record AdminParticipationSummary(
    String month,
    Long totalTargetMemberCount,
    Long completedCount,
    Long incompleteCount,
    Double completionRate,
    List<AdminParticipationMemberView> members
) {
}
