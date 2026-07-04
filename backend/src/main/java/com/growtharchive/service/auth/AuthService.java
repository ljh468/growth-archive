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
        return oauthAccountRepository.findMemberId(PROVIDER_KAKAO, profile.providerUserId())
            .map(memberId -> handleExistingMemberLogin(memberId, profile, response))
            .orElseGet(() -> handleSignupStart(profile, response));
    }

    private URI handleExistingMemberLogin(Long memberId, KakaoUserProfile profile, HttpServletResponse response) {
        oauthAccountRepository.upsert(
            PROVIDER_KAKAO,
            profile.providerUserId(),
            memberId,
            profile.email(),
            profile.nickname(),
            profile.profileImageUrl()
        );
        memberRepository.updateKakaoProfileImageUrl(memberId, profile.profileImageUrl());
        if (memberRepository.isWithdrawn(memberId)) {
            return handleSignupStart(profile, response);
        }
        MemberPrincipal principal = memberRepository.findPrincipalById(memberId).orElseThrow();
        if (principal.deactivated()) {
            authCookieService.clearAuthCookies(response);
            authCookieService.clearSignupCookie(response);
            return URI.create(properties.getFrontend().getBaseUrl() + "/login?deactivated=true");
        }
        issueCookies(memberId, response);
        authCookieService.clearSignupCookie(response);
        return URI.create(properties.getFrontend().getBaseUrl() + (principal.onboardingCompleted() ? "/" : "/onboarding"));
    }

    private URI handleSignupStart(KakaoUserProfile profile, HttpServletResponse response) {
        JwtService.SignupToken signupToken = new JwtService.SignupToken(
            PROVIDER_KAKAO,
            profile.providerUserId(),
            profile.email(),
            profile.nickname(),
            profile.profileImageUrl(),
            false,
            null,
            false,
            false
        );
        authCookieService.clearAuthCookies(response);
        authCookieService.addSignupCookie(response, jwtService.issueSignup(signupToken));
        return URI.create(properties.getFrontend().getBaseUrl() + "/onboarding");
    }

    public MeResponse me(HttpServletRequest request) {
        MemberPrincipal principal = currentMemberResolver.resolveOptional(request);
        AccessLevel accessLevel = accessLevelCalculator.calculate(principal);
        if (principal == null) {
            JwtService.SignupToken signupToken = resolveSignupToken(request);
            if (signupToken == null) {
                return MeResponse.publicUser();
            }
            return new MeResponse(
                true,
                signupToken.inviteVerified() ? AccessLevel.INVITE_VERIFIED.name() : AccessLevel.AUTHENTICATED.name(),
                null,
                null,
                signupToken.nickname(),
                signupToken.profileImageUrl(),
                signupToken.inviteVerified(),
                signupToken.termsAgreed(),
                signupToken.privacyAgreed(),
                false,
                false
            );
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
        authCookieService.clearSignupCookie(response);
    }

    private JwtService.SignupToken resolveSignupToken(HttpServletRequest request) {
        String token = authCookieService.readCookie(request, AuthCookieService.SIGNUP_TOKEN_COOKIE);
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return jwtService.verifySignup(token);
        } catch (Exception exception) {
            return null;
        }
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
