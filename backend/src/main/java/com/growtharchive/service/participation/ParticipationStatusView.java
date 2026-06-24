package com.growtharchive.service.participation;

public record ParticipationStatusView(
    String month,
    Long readingRecordCount,
    Long actionPlanCount,
    boolean calculationTarget,
    boolean completed,
    boolean coffeeSupportTarget,
    String coffeeSupportItem
) {
}
