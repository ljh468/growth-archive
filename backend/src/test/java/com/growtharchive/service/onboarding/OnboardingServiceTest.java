package com.growtharchive.service.onboarding;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.growtharchive.exception.ApiException;
import com.growtharchive.repository.InterestTagRepository;
import com.growtharchive.repository.InviteCodeRepository;
import com.growtharchive.repository.MemberRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import com.growtharchive.service.auth.CodeHashService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OnboardingServiceTest {

    private final CurrentMemberResolver resolver = Mockito.mock(CurrentMemberResolver.class);
    private final InviteCodeRepository inviteCodeRepository = Mockito.mock(InviteCodeRepository.class);
    private final MemberRepository memberRepository = Mockito.mock(MemberRepository.class);
    private final InterestTagRepository interestTagRepository = Mockito.mock(InterestTagRepository.class);
    private final CodeHashService codeHashService = new CodeHashService();
    private final OnboardingService service = new OnboardingService(
        resolver,
        inviteCodeRepository,
        memberRepository,
        interestTagRepository,
        codeHashService
    );

    @Test
    void rejectsInvalidInviteCode() {
        Mockito.when(resolver.require(Mockito.isNull(), Mockito.eq(AccessLevel.AUTHENTICATED))).thenReturn(authenticatedMember());
        Mockito.when(inviteCodeRepository.findActiveHash()).thenReturn(Optional.of(codeHashService.hashInviteCode("right-code")));

        assertThatThrownBy(() -> service.verifyInviteCode(null, "wrong-code"))
            .isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsProfileWithMoreThanFiveInterestTags() {
        Mockito.when(resolver.require(Mockito.isNull(), Mockito.eq(AccessLevel.INVITE_VERIFIED))).thenReturn(inviteVerifiedTermsAgreedMember());

        OnboardingService.CompleteProfileCommand command = new OnboardingService.CompleteProfileCommand(
            "phase11",
            "한 줄 소개",
            "NICKNAME",
            null,
            "Developer",
            List.of(1L, 2L, 3L, 4L, 5L, 6L),
            "오래 지속하는 사람",
            "함께 성장",
            "꾸준함",
            "좋은 제품"
        );

        assertThatThrownBy(() -> service.completeProfile(null, command))
            .isInstanceOf(ApiException.class);
    }

    private MemberPrincipal authenticatedMember() {
        return new MemberPrincipal(1L, "MEMBER", "NICKNAME", null, "member", null, null, null, null, null, null);
    }

    private MemberPrincipal inviteVerifiedTermsAgreedMember() {
        OffsetDateTime now = OffsetDateTime.now();
        return new MemberPrincipal(1L, "MEMBER", "NICKNAME", null, "member", null, now, now, now, null, null);
    }
}
