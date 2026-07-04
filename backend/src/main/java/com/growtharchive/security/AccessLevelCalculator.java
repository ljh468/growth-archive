package com.growtharchive.security;

import org.springframework.stereotype.Component;

@Component
public class AccessLevelCalculator {

    public AccessLevel calculate(MemberPrincipal member) {
        if (member == null) {
            return AccessLevel.PUBLIC;
        }
        if (member.deactivated()) {
            return AccessLevel.AUTHENTICATED;
        }
        if (member.onboardingCompleted()) {
            if ("ADMIN".equals(member.role())) {
                return AccessLevel.ADMIN;
            }
            return AccessLevel.MEMBER;
        }
        if (member.inviteVerified()) {
            return AccessLevel.INVITE_VERIFIED;
        }
        return AccessLevel.AUTHENTICATED;
    }

    public boolean hasAtLeast(AccessLevel actual, AccessLevel required) {
        return actual.ordinal() >= required.ordinal();
    }
}
