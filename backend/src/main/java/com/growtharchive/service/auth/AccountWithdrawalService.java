package com.growtharchive.service.auth;

import com.growtharchive.repository.MemberRepository;
import com.growtharchive.repository.OauthAccountRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.AuthCookieService;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountWithdrawalService {

    private static final String PROVIDER_KAKAO = "KAKAO";

    private final CurrentMemberResolver currentMemberResolver;
    private final MemberRepository memberRepository;
    private final OauthAccountRepository oauthAccountRepository;
    private final AuthCookieService authCookieService;
    private final KakaoOAuthClient kakaoOAuthClient;

    public AccountWithdrawalService(
        CurrentMemberResolver currentMemberResolver,
        MemberRepository memberRepository,
        OauthAccountRepository oauthAccountRepository,
        AuthCookieService authCookieService,
        KakaoOAuthClient kakaoOAuthClient
    ) {
        this.currentMemberResolver = currentMemberResolver;
        this.memberRepository = memberRepository;
        this.oauthAccountRepository = oauthAccountRepository;
        this.authCookieService = authCookieService;
        this.kakaoOAuthClient = kakaoOAuthClient;
    }

    @Transactional
    public void withdrawSelf(HttpServletRequest request, HttpServletResponse response) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        String providerUserId = oauthAccountRepository.findProviderUserId(PROVIDER_KAKAO, member.memberId()).orElse(null);
        withdrawMember(member.memberId());
        if (providerUserId != null) {
            kakaoOAuthClient.unlinkProviderUser(providerUserId);
        }
        authCookieService.clearAuthCookies(response);
        authCookieService.clearSignupCookie(response);
    }

    @Transactional
    public void withdrawByKakaoProviderUserId(String providerUserId) {
        if (providerUserId == null || providerUserId.isBlank()) {
            return;
        }
        oauthAccountRepository.findMemberId(PROVIDER_KAKAO, providerUserId)
            .ifPresent(this::withdrawMember);
    }

    private void withdrawMember(Long memberId) {
        memberRepository.withdrawAndRedact(memberId);
        oauthAccountRepository.redactProfile(PROVIDER_KAKAO, memberId);
    }
}
