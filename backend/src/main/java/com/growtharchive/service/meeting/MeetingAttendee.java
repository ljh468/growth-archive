package com.growtharchive.service.meeting;

public record MeetingAttendee(
    Long memberId,
    String displayName,
    String profileImageUrl,
    String profileHref
) {
}
