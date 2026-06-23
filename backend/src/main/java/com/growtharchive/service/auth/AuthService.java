package com.growtharchive.service.auth;

import com.growtharchive.config.properties.AppProperties;
import com.growtharchive.repository.MemberRepository;
import com.growtharchive.repository.OauthAccountRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.AccessLevelCalculator;
import com.growtharchive.security.AuthCookieService;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.JwtService;
import com.growtharchive.security.MemberPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String PROVIDER_KAKAO = "KAKAO";

    private final AppProperties properties;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final MemberRepository memberRepository;
    private final OauthAccountRepository oauthAccountRepository;
    private final JwtService jwtService;
    private final AuthCookieService authCookieService;
    private final CurrentMemberResolver currentMemberResolver;
    private final AccessLevelCalculator accessLevelCalculator;

    public AuthService(
        AppProperties properties,
        KakaoOAuthClient kakaoOAuthClient,
        MemberRepository memberRepository,
        OauthAccountRepository oauthAccountRepository,
        JwtService jwtService,
        AuthCookieService authCookieService,
        CurrentMemberResolver currentMemberResolver,
        AccessLevelCalculator accessLevelCalculator
    ) {
        this.properties = properties;
        this.kakaoOAuthClient = kakaoOAuthClient;
        this.memberRepository = memberRepository;
        this.oauthAccountRepository = oauthAccountRepository;
        this.jwtService = jwtService;
        this.authCookieService = authCookieService;
        this.currentMemberResolver = currentMemberResolver;
        this.accessLevelCalculator = accessLevelCalculator;
    }

    public URI kakaoLoginUri(String state) {
        return URI.create(kakaoOAuthClient.authorizationUrl(state));
    }

    @Transactional
    public URI handleKakaoCallback(String code, HttpServletResponse response) {
        KakaoUserProfile profile = kakaoOAuthClient.fetchProfile(code);
        Long memberId = oauthAccountRepository.findMemberId(PROVIDER_KAKAO, profile.providerUserId())
            .orElseGet(() -> memberRepository.createPreOnboardingMember(
                profile.providerUserId(),
                profile.nickname(),
                profile.profileImageUrl(),
                properties.getKakao().getInitialAdminProviderIds().contains(profile.providerUserId())
            ));
        oauthAccountRepository.upsert(
            PROVIDER_KAKAO,
            profile.providerUserId(),
            memberId,
            profile.email(),
            profile.nickname(),
            profile.profileImageUrl()
        );
        issueCookies(memberId, response);
        MemberPrincipal principal = memberRepository.findPrincipalById(memberId).orElseThrow();
        return URI.create(properties.getFrontend().getBaseUrl()
            + (principal.onboardingCompleted() && !principal.deactivated() ? "/" : "/onboarding"));
    }

    public MeResponse me(HttpServletRequest request) {
        MemberPrincipal principal = currentMemberResolver.resolveOptional(request);
        AccessLevel accessLevel = accessLevelCalculator.calculate(principal);
        if (principal == null) {
            return MeResponse.publicUser();
        }
        return new MeResponse(
            true,
            accessLevel.name(),
            principal.memberId(),
            principal.role(),
            principal.displayName(),
            principal.kakaoProfileImageUrl(),
            principal.inviteVerified(),
            principal.termsAgreed(),
            principal.privacyAgreed(),
            principal.onboardingCompleted(),
            principal.deactivated()
        );
    }

    public void refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = authCookieService.readCookie(request, AuthCookieService.REFRESH_TOKEN_COOKIE);
        Long memberId = jwtService.verify(refreshToken, JwtService.TokenType.REFRESH);
        issueCookies(memberId, response);
    }

    public void logout(HttpServletResponse response) {
        authCookieService.clearAuthCookies(response);
    }

    private void issueCookies(Long memberId, HttpServletResponse response) {
        String accessToken = jwtService.issue(memberId, JwtService.TokenType.ACCESS);
        String refreshToken = jwtService.issue(memberId, JwtService.TokenType.REFRESH);
        authCookieService.addAuthCookies(response, accessToken, refreshToken);
    }

    public record MeResponse(
        boolean authenticated,
        String accessLevel,
        Long memberId,
        String role,
        String displayName,
        String profileImageUrl,
        boolean inviteVerified,
        boolean termsAgreed,
        boolean privacyAgreed,
        boolean onboardingCompleted,
        boolean deactivated
    ) {
        public static MeResponse publicUser() {
            return new MeResponse(false, AccessLevel.PUBLIC.name(), null, null, null, null, false, false, false, false, false);
        }
    }
}
