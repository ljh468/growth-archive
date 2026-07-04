package com.growtharchive.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class AccessLevelCalculatorTest {

    private final AccessLevelCalculator calculator = new AccessLevelCalculator();

    @Test
    void publicWhenNoAuthenticatedMember() {
        assertThat(calculator.calculate(null)).isEqualTo(AccessLevel.PUBLIC);
    }

    @Test
    void authenticatedBeforeInviteVerification() {
        MemberPrincipal member = principal("MEMBER", null, null, null, null, null);

        assertThat(calculator.calculate(member)).isEqualTo(AccessLevel.AUTHENTICATED);
    }

    @Test
    void inviteVerifiedBeforeOnboarding() {
        OffsetDateTime now = OffsetDateTime.now();
        MemberPrincipal member = principal("MEMBER", now, null, null, null, null);

        assertThat(calculator.calculate(member)).isEqualTo(AccessLevel.INVITE_VERIFIED);
    }

    @Test
    void memberRequiresCompletedOnboardingAndNotDeactivated() {
        OffsetDateTime now = OffsetDateTime.now();
        MemberPrincipal member = principal("MEMBER", now, now, now, now, null);

        assertThat(calculator.calculate(member)).isEqualTo(AccessLevel.MEMBER);
    }

    @Test
    void adminRequiresActiveMemberAndAdminRole() {
        OffsetDateTime now = OffsetDateTime.now();
        MemberPrincipal member = principal("ADMIN", now, now, now, now, null);

        assertThat(calculator.calculate(member)).isEqualTo(AccessLevel.ADMIN);
    }

    @Test
    void deactivatedMemberIsNotMemberOrAdmin() {
        OffsetDateTime now = OffsetDateTime.now();
        MemberPrincipal member = principal("ADMIN", now, now, now, now, now);

        assertThat(calculator.calculate(member)).isEqualTo(AccessLevel.AUTHENTICATED);
    }

    private MemberPrincipal principal(
        String role,
        OffsetDateTime inviteVerifiedAt,
        OffsetDateTime termsAgreedAt,
        OffsetDateTime privacyAgreedAt,
        OffsetDateTime onboardingCompletedAt,
        OffsetDateTime deactivatedAt
    ) {
        return new MemberPrincipal(
            1L,
            role,
            "NICKNAME",
            null,
            "노아",
            null,
            inviteVerifiedAt,
            termsAgreedAt,
            privacyAgreedAt,
            onboardingCompletedAt,
            deactivatedAt
        );
    }
}
