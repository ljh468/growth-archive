package com.growtharchive.service.onboarding;

import com.growtharchive.config.properties.AppProperties;
import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.repository.ImageAssetRepository;
import com.growtharchive.repository.InterestTagRepository;
import com.growtharchive.repository.InviteCodeRepository;
import com.growtharchive.repository.MemberRepository;
import com.growtharchive.repository.OauthAccountRepository;
import com.growtharchive.security.AuthCookieService;
import com.growtharchive.security.JwtService;
import com.growtharchive.service.auth.CodeHashService;
import com.growtharchive.support.KstDateTimes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingService {

    private static final String PROVIDER_KAKAO = "KAKAO";

    private final AppProperties properties;
    private final InviteCodeRepository inviteCodeRepository;
    private final MemberRepository memberRepository;
    private final OauthAccountRepository oauthAccountRepository;
    private final InterestTagRepository interestTagRepository;
    private final ImageAssetRepository imageAssetRepository;
    private final AuthCookieService authCookieService;
    private final JwtService jwtService;
    private final CodeHashService codeHashService;

    public OnboardingService(
        AppProperties properties,
        InviteCodeRepository inviteCodeRepository,
        MemberRepository memberRepository,
        OauthAccountRepository oauthAccountRepository,
        InterestTagRepository interestTagRepository,
        ImageAssetRepository imageAssetRepository,
        AuthCookieService authCookieService,
        JwtService jwtService,
        CodeHashService codeHashService
    ) {
        this.properties = properties;
        this.inviteCodeRepository = inviteCodeRepository;
        this.memberRepository = memberRepository;
        this.oauthAccountRepository = oauthAccountRepository;
        this.interestTagRepository = interestTagRepository;
        this.imageAssetRepository = imageAssetRepository;
        this.authCookieService = authCookieService;
        this.jwtService = jwtService;
        this.codeHashService = codeHashService;
    }

    public void verifyInviteCode(HttpServletRequest request, HttpServletResponse response, String code) {
        JwtService.SignupToken signupToken = requireSignup(request);
        String inviteRole = inviteCodeRepository.findActiveRoleByHash(codeHashService.hashInviteCode(code))
            .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INVITE_CODE, "현재 사용할 수 없는 초대코드입니다. 운영진에게 문의해 주세요."));
        rewriteSignupToken(response, new JwtService.SignupToken(
            signupToken.provider(),
            signupToken.providerUserId(),
            signupToken.email(),
            signupToken.nickname(),
            signupToken.profileImageUrl(),
            true,
            inviteRole,
            signupToken.termsAgreed(),
            signupToken.privacyAgreed()
        ));
    }

    public void agreeTerms(HttpServletRequest request, HttpServletResponse response, boolean termsAgreed, boolean privacyAgreed) {
        JwtService.SignupToken signupToken = requireSignup(request);
        if (!signupToken.inviteVerified()) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (!termsAgreed || !privacyAgreed) {
            throw new ApiException(ErrorCode.TERMS_NOT_AGREED);
        }
        rewriteSignupToken(response, new JwtService.SignupToken(
            signupToken.provider(),
            signupToken.providerUserId(),
            signupToken.email(),
            signupToken.nickname(),
            signupToken.profileImageUrl(),
            true,
            signupToken.inviteRole(),
            true,
            true
        ));
    }

    public ProfileDraft getProfileDraft(HttpServletRequest request) {
        JwtService.SignupToken signupToken = requireSignup(request);
        if (!signupToken.inviteVerified() || !signupToken.termsAgreed() || !signupToken.privacyAgreed()) {
            throw new ApiException(ErrorCode.TERMS_NOT_AGREED);
        }
        Optional<Long> existingMemberId = oauthAccountRepository.findMemberId(PROVIDER_KAKAO, signupToken.providerUserId());
        if (existingMemberId.isPresent() && memberRepository.isWithdrawn(existingMemberId.get())) {
            return memberRepository.findWithdrawnOnboardingDraft(existingMemberId.get())
                .map(row -> new ProfileDraft(
                    row.nickname(),
                    row.oneLineIntro(),
                    normalizeDisplayNameType(row.displayNameType()),
                    null,
                    null,
                    null,
                    row.futureMeAt50(),
                    row.joinReason(),
                    row.currentConcern(),
                    row.threeYearGoal(),
                    row.interestTagIds()
                ))
                .orElseGet(() -> emptyProfileDraft(signupToken));
        }
        return emptyProfileDraft(signupToken);
    }

    @Transactional
    public void completeProfile(HttpServletRequest request, HttpServletResponse response, CompleteProfileCommand command) {
        JwtService.SignupToken signupToken = requireSignup(request);
        if (!signupToken.inviteVerified() || !signupToken.termsAgreed() || !signupToken.privacyAgreed()) {
            throw new ApiException(ErrorCode.TERMS_NOT_AGREED);
        }
        Optional<Long> existingMemberId = oauthAccountRepository.findMemberId(PROVIDER_KAKAO, signupToken.providerUserId());
        if (existingMemberId.isPresent() && !memberRepository.isWithdrawn(existingMemberId.get())) {
            issueAuthCookies(existingMemberId.get(), response);
            authCookieService.clearSignupCookie(response);
            return;
        }
        String nickname = command.nickname().trim();
        boolean nicknameTaken = existingMemberId
            .map(memberId -> memberRepository.existsNicknameForOtherMember(nickname, memberId))
            .orElseGet(() -> memberRepository.existsNickname(nickname));
        if (nicknameTaken) {
            throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
        }
        if (isBlank(command.realName())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "실명을 입력해 주세요.");
        }
        if (command.birthDate() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "생년월일을 입력해 주세요.");
        }
        String displayNameType = normalizeDisplayNameType(command.displayNameType());
        if (command.interestTagIds() == null || command.interestTagIds().isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "관심 분야를 1개 이상 선택해 주세요.");
        }
        if (command.interestTagIds().size() > 5) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "관심 분야는 최대 5개까지 선택해 주세요.");
        }
        if (interestTagRepository.countActiveIds(command.interestTagIds()) != command.interestTagIds().size()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "선택할 수 없는 관심 분야가 포함되어 있습니다.");
        }
        if (command.birthDate().isAfter(KstDateTimes.today())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "생년월일을 다시 확인해 주세요.");
        }
        if (!imageAssetRepository.isUnownedImage(command.profileImageId(), "PROFILE")) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "사용할 수 없는 프로필 이미지입니다.");
        }
        String role = normalizeInviteRole(signupToken.inviteRole());
        Long memberId;
        if (existingMemberId.isPresent()) {
            memberId = existingMemberId.get();
            memberRepository.reactivateWithdrawnMember(
                memberId,
                role,
                nickname,
                command.oneLineIntro().trim(),
                displayNameType,
                command.realName().trim(),
                command.birthDate(),
                command.profileImageId(),
                signupToken.profileImageUrl(),
                command.futureMeAt50().trim(),
                trimToNull(command.joinReason()),
                trimToNull(command.currentConcern()),
                trimToNull(command.threeYearGoal())
            );
        } else {
            memberId = memberRepository.createOnboardedMember(
                role,
                nickname,
                command.oneLineIntro().trim(),
                displayNameType,
                command.realName().trim(),
                command.birthDate(),
                command.profileImageId(),
                signupToken.profileImageUrl(),
                command.futureMeAt50().trim(),
                trimToNull(command.joinReason()),
                trimToNull(command.currentConcern()),
                trimToNull(command.threeYearGoal())
            );
        }
        imageAssetRepository.assignOwner(command.profileImageId(), memberId);
        interestTagRepository.replaceMemberTags(memberId, command.interestTagIds());
        oauthAccountRepository.upsert(
            PROVIDER_KAKAO,
            signupToken.providerUserId(),
            memberId,
            signupToken.email(),
            signupToken.nickname(),
            signupToken.profileImageUrl()
        );
        issueAuthCookies(memberId, response);
        authCookieService.clearSignupCookie(response);
    }

    private JwtService.SignupToken requireSignup(HttpServletRequest request) {
        String token = authCookieService.readCookie(request, AuthCookieService.SIGNUP_TOKEN_COOKIE);
        if (token == null || token.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        JwtService.SignupToken signupToken = jwtService.verifySignup(token);
        if (!PROVIDER_KAKAO.equals(signupToken.provider()) || isBlank(signupToken.providerUserId())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return signupToken;
    }

    private void rewriteSignupToken(HttpServletResponse response, JwtService.SignupToken signupToken) {
        authCookieService.addSignupCookie(response, jwtService.issueSignup(signupToken));
    }

    private void issueAuthCookies(Long memberId, HttpServletResponse response) {
        authCookieService.addAuthCookies(
            response,
            jwtService.issue(memberId, JwtService.TokenType.ACCESS),
            jwtService.issue(memberId, JwtService.TokenType.REFRESH)
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeInviteRole(String role) {
        return "ADMIN".equals(role) ? "ADMIN" : "MEMBER";
    }

    private String normalizeDisplayNameType(String displayNameType) {
        if (displayNameType == null || displayNameType.isBlank()) {
            return "REAL_NAME";
        }
        String normalized = displayNameType.trim();
        if (!"REAL_NAME".equals(normalized) && !"NICKNAME".equals(normalized)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "공개 표시 방식을 선택해 주세요.");
        }
        return normalized;
    }

    private ProfileDraft emptyProfileDraft(JwtService.SignupToken signupToken) {
        return new ProfileDraft(
            signupToken.nickname(),
            "",
            "REAL_NAME",
            null,
            null,
            null,
            "",
            null,
            null,
            null,
            List.of()
        );
    }

    public record CompleteProfileCommand(
        String nickname,
        String oneLineIntro,
        String displayNameType,
        String realName,
        LocalDate birthDate,
        Long profileImageId,
        List<Long> interestTagIds,
        String futureMeAt50,
        String joinReason,
        String currentConcern,
        String threeYearGoal
    ) {
    }

    public record ProfileDraft(
        String nickname,
        String oneLineIntro,
        String displayNameType,
        String realName,
        LocalDate birthDate,
        Long profileImageId,
        String futureMeAt50,
        String joinReason,
        String currentConcern,
        String threeYearGoal,
        List<Long> interestTagIds
    ) {
    }
}
