package com.growtharchive.service.profile;

import java.time.OffsetDateTime;

public record ActivitySummary(
    String type,
    String title,
    String href,
    OffsetDateTime occurredAt
) {
}
