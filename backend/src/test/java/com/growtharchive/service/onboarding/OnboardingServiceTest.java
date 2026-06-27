package com.growtharchive.service.onboarding;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.growtharchive.config.properties.AppProperties;
import com.growtharchive.exception.ApiException;
import com.growtharchive.repository.OauthAccountRepository;
import com.growtharchive.repository.ImageAssetRepository;
import com.growtharchive.repository.InterestTagRepository;
import com.growtharchive.repository.InviteCodeRepository;
import com.growtharchive.repository.MemberRepository;
import com.growtharchive.security.AuthCookieService;
import com.growtharchive.security.JwtService;
import com.growtharchive.service.auth.CodeHashService;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OnboardingServiceTest {

    private static final JwtService.SignupToken SIGNUP_TOKEN = new JwtService.SignupToken(
        "KAKAO",
        "kakao-123",
        "member@example.com",
        "카카오 사용자",
        "https://profile.example/image.png",
        false,
        false,
        false
    );
    private static final JwtService.SignupToken READY_SIGNUP_TOKEN = new JwtService.SignupToken(
        "KAKAO",
        "kakao-123",
        "member@example.com",
        "카카오 사용자",
        "https://profile.example/image.png",
        true,
        true,
        true
    );

    private final AppProperties properties = new AppProperties();
    private final InviteCodeRepository inviteCodeRepository = Mockito.mock(InviteCodeRepository.class);
    private final MemberRepository memberRepository = Mockito.mock(MemberRepository.class);
    private final OauthAccountRepository oauthAccountRepository = Mockito.mock(OauthAccountRepository.class);
    private final InterestTagRepository interestTagRepository = Mockito.mock(InterestTagRepository.class);
    private final ImageAssetRepository imageAssetRepository = Mockito.mock(ImageAssetRepository.class);
    private final AuthCookieService authCookieService = Mockito.mock(AuthCookieService.class);
    private final JwtService jwtService = Mockito.mock(JwtService.class);
    private final CodeHashService codeHashService = new CodeHashService();
    private final OnboardingService service = new OnboardingService(
        properties,
        inviteCodeRepository,
        memberRepository,
        oauthAccountRepository,
        interestTagRepository,
        imageAssetRepository,
        authCookieService,
        jwtService,
        codeHashService
    );

    @Test
    void rejectsInvalidInviteCode() {
        mockSignup(SIGNUP_TOKEN);
        Mockito.when(inviteCodeRepository.findActiveHash()).thenReturn(Optional.of(codeHashService.hashInviteCode("right-code")));

        assertThatThrownBy(() -> service.verifyInviteCode(null, Mockito.mock(HttpServletResponse.class), "wrong-code"))
            .isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsProfileWithMoreThanFiveInterestTags() {
        mockSignup(READY_SIGNUP_TOKEN);

        OnboardingService.CompleteProfileCommand command = new OnboardingService.CompleteProfileCommand(
            "phase11",
            "한 줄 소개",
            "NICKNAME",
            "김민준",
            null,
            null,
            List.of(1L, 2L, 3L, 4L, 5L, 6L),
            "오래 지속하는 사람",
            "함께 성장",
            "꾸준함",
            "좋은 제품"
        );

        assertThatThrownBy(() -> service.completeProfile(null, Mockito.mock(HttpServletResponse.class), command))
            .isInstanceOf(ApiException.class);
    }

    @Test
    void createsMemberOnlyWhenProfileIsCompleted() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        mockSignup(READY_SIGNUP_TOKEN);
        Mockito.when(oauthAccountRepository.findMemberId("KAKAO", "kakao-123")).thenReturn(Optional.empty());
        Mockito.when(interestTagRepository.countActiveIds(List.of(1L))).thenReturn(1);
        Mockito.when(imageAssetRepository.isUnownedImage(10L, "PROFILE")).thenReturn(true);
        Mockito.when(memberRepository.createOnboardedMember(
            Mockito.eq("MEMBER"),
            Mockito.eq("민준"),
            Mockito.eq("꾸준히 읽고 실행합니다."),
            Mockito.eq("NICKNAME"),
            Mockito.eq("김민준"),
            Mockito.eq(LocalDate.of(1990, 1, 1)),
            Mockito.eq(10L),
            Mockito.eq("https://profile.example/image.png"),
            Mockito.eq("오래 지속하는 사람"),
            Mockito.eq("함께 성장"),
            Mockito.eq("꾸준함"),
            Mockito.eq("좋은 제품")
        )).thenReturn(42L);
        Mockito.when(jwtService.issue(Mockito.eq(42L), Mockito.any())).thenReturn("auth-token");

        service.completeProfile(null, response, new OnboardingService.CompleteProfileCommand(
            "민준",
            "꾸준히 읽고 실행합니다.",
            "NICKNAME",
            "김민준",
            LocalDate.of(1990, 1, 1),
            10L,
            List.of(1L),
            "오래 지속하는 사람",
            "함께 성장",
            "꾸준함",
            "좋은 제품"
        ));

        Mockito.verify(oauthAccountRepository).upsert(
            "KAKAO",
            "kakao-123",
            42L,
            "member@example.com",
            "카카오 사용자",
            "https://profile.example/image.png"
        );
        Mockito.verify(authCookieService).addAuthCookies(response, "auth-token", "auth-token");
        Mockito.verify(authCookieService).clearSignupCookie(response);
    }

    @Test
    void defaultsDisplayNameTypeToRealNameWhenProfileIsCompleted() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        mockSignup(READY_SIGNUP_TOKEN);
        Mockito.when(oauthAccountRepository.findMemberId("KAKAO", "kakao-123")).thenReturn(Optional.empty());
        Mockito.when(interestTagRepository.countActiveIds(List.of(1L))).thenReturn(1);
        Mockito.when(imageAssetRepository.isUnownedImage(null, "PROFILE")).thenReturn(true);
        Mockito.when(memberRepository.createOnboardedMember(
            Mockito.eq("MEMBER"),
            Mockito.eq("민준"),
            Mockito.eq("꾸준히 읽고 실행합니다."),
            Mockito.eq("REAL_NAME"),
            Mockito.eq("김민준"),
            Mockito.isNull(),
            Mockito.isNull(),
            Mockito.eq("https://profile.example/image.png"),
            Mockito.eq("오래 지속하는 사람"),
            Mockito.isNull(),
            Mockito.isNull(),
            Mockito.isNull()
        )).thenReturn(42L);
        Mockito.when(jwtService.issue(Mockito.eq(42L), Mockito.any())).thenReturn("auth-token");

        service.completeProfile(null, response, new OnboardingService.CompleteProfileCommand(
            "민준",
            "꾸준히 읽고 실행합니다.",
            null,
            "김민준",
            null,
            null,
            List.of(1L),
            "오래 지속하는 사람",
            null,
            null,
            null
        ));

        Mockito.verify(authCookieService).addAuthCookies(response, "auth-token", "auth-token");
    }

    private void mockSignup(JwtService.SignupToken signupToken) {
        Mockito.when(authCookieService.readCookie(null, AuthCookieService.SIGNUP_TOKEN_COOKIE)).thenReturn("signup-token");
        Mockito.when(jwtService.verifySignup("signup-token")).thenReturn(signupToken);
    }
}
