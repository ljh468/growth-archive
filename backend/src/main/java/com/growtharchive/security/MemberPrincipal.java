package com.growtharchive.security;

import java.time.OffsetDateTime;

public record MemberPrincipal(
    Long memberId,
    String role,
    String displayType,
    String realName,
    String nickname,
    String kakaoProfileImageUrl,
    OffsetDateTime inviteVerifiedAt,
    OffsetDateTime termsAgreedAt,
    OffsetDateTime privacyAgreedAt,
    OffsetDateTime onboardingCompletedAt,
    OffsetDateTime deactivatedAt
) {

    public boolean inviteVerified() {
        return inviteVerifiedAt != null;
    }

    public boolean termsAgreed() {
        return termsAgreedAt != null;
    }

    public boolean privacyAgreed() {
        return privacyAgreedAt != null;
    }

    public boolean onboardingCompleted() {
        return onboardingCompletedAt != null;
    }

    public boolean deactivated() {
        return deactivatedAt != null;
    }

    public String displayName() {
        if ("REAL_NAME".equals(displayType) && realName != null && !realName.isBlank()) {
            return realName;
        }
        return nickname;
    }
}
