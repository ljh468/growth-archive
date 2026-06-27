package com.growtharchive.service.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.growtharchive.config.properties.AppProperties;
import com.growtharchive.repository.MemberRepository;
import com.growtharchive.repository.OauthAccountRepository;
import com.growtharchive.security.AccessLevelCalculator;
import com.growtharchive.security.AuthCookieService;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.JwtService;
import com.growtharchive.security.MemberPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AuthServiceTest {

    private final AppProperties properties = new AppProperties();
    private final KakaoOAuthClient kakaoOAuthClient = Mockito.mock(KakaoOAuthClient.class);
    private final MemberRepository memberRepository = Mockito.mock(MemberRepository.class);
    private final OauthAccountRepository oauthAccountRepository = Mockito.mock(OauthAccountRepository.class);
    private final JwtService jwtService = Mockito.mock(JwtService.class);
    private final AuthCookieService authCookieService = Mockito.mock(AuthCookieService.class);
    private final CurrentMemberResolver currentMemberResolver = Mockito.mock(CurrentMemberResolver.class);
    private final AccessLevelCalculator accessLevelCalculator = new AccessLevelCalculator();
    private final AuthService service = new AuthService(
        properties,
        kakaoOAuthClient,
        memberRepository,
        oauthAccountRepository,
        jwtService,
        authCookieService,
        currentMemberResolver,
        accessLevelCalculator
    );

    @Test
    void kakaoCallbackReusesMemberByProviderUserId() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        KakaoUserProfile profile = new KakaoUserProfile("kakao-123", "user@example.com", "카카오 사용자", "https://profile.example/image.png");
        OffsetDateTime completedAt = OffsetDateTime.now();
        Mockito.when(kakaoOAuthClient.fetchProfile("code")).thenReturn(profile);
        Mockito.when(oauthAccountRepository.findMemberId("KAKAO", "kakao-123")).thenReturn(Optional.of(7L));
        Mockito.when(jwtService.issue(Mockito.eq(7L), Mockito.any())).thenReturn("token");
        Mockito.when(memberRepository.findPrincipalById(7L)).thenReturn(Optional.of(
            new MemberPrincipal(7L, "MEMBER", "NICKNAME", null, "민준", null, completedAt, completedAt, completedAt, completedAt, null)
        ));

        URI redirect = service.handleKakaoCallback("code", response);

        assertThat(redirect.toString()).isEqualTo("http://localhost:3000/");
        Mockito.verify(oauthAccountRepository).upsert(
            "KAKAO",
            "kakao-123",
            7L,
            "user@example.com",
            "카카오 사용자",
            "https://profile.example/image.png"
        );
    }

    @Test
    void kakaoCallbackIssuesSignupTokenForNewProviderUserId() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        KakaoUserProfile profile = new KakaoUserProfile("kakao-456", null, "신규 사용자", null);
        Mockito.when(kakaoOAuthClient.fetchProfile("code")).thenReturn(profile);
        Mockito.when(oauthAccountRepository.findMemberId("KAKAO", "kakao-456")).thenReturn(Optional.empty());
        Mockito.when(jwtService.issueSignup(Mockito.any())).thenReturn("signup-token");

        URI redirect = service.handleKakaoCallback("code", response);

        assertThat(redirect.toString()).isEqualTo("http://localhost:3000/onboarding");
        Mockito.verify(oauthAccountRepository, Mockito.never())
            .upsert(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(authCookieService).clearAuthCookies(response);
        Mockito.verify(authCookieService).addSignupCookie(response, "signup-token");
    }
}
