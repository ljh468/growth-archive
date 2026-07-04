package com.growtharchive.controller.onboarding;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.onboarding.OnboardingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/invite-code")
    public ApiResponse<InviteCodeResponse> verifyInviteCode(
        HttpServletRequest request,
        HttpServletResponse response,
        @Valid @RequestBody InviteCodeRequest body
    ) {
        onboardingService.verifyInviteCode(request, response, body.code());
        return new ApiResponse<>(true, new InviteCodeResponse(true), "초대코드가 확인되었습니다.", null);
    }

    @PostMapping("/terms")
    public ApiResponse<TermsResponse> agreeTerms(
        HttpServletRequest request,
        HttpServletResponse response,
        @Valid @RequestBody TermsRequest body
    ) {
        onboardingService.agreeTerms(request, response, body.termsAgreed(), body.privacyAgreed());
        return ApiResponse.success(new TermsResponse(true, true));
    }

    @GetMapping("/profile-draft")
    public ApiResponse<OnboardingService.ProfileDraft> profileDraft(HttpServletRequest request) {
        return ApiResponse.success(onboardingService.getProfileDraft(request));
    }

    @PostMapping("/profile")
    public ApiResponse<OnboardingResponse> completeProfile(
        HttpServletRequest request,
        HttpServletResponse response,
        @Valid @RequestBody CompleteProfileRequest body
    ) {
        onboardingService.completeProfile(request, response, body.toCommand());
        return new ApiResponse<>(true, new OnboardingResponse(true), "성장 프로필이 만들어졌습니다.", null);
    }

    public record InviteCodeRequest(
        @NotBlank(message = "초대코드를 입력해 주세요.")
        String code
    ) {
    }

    public record InviteCodeResponse(boolean verified) {
    }

    public record TermsRequest(
        @AssertTrue(message = "이용약관에 동의해 주세요.")
        boolean termsAgreed,
        @AssertTrue(message = "개인정보처리방침에 동의해 주세요.")
        boolean privacyAgreed
    ) {
    }

    public record TermsResponse(boolean termsAgreed, boolean privacyAgreed) {
    }

    public record CompleteProfileRequest(
        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
        String nickname,
        @NotBlank(message = "한 줄 소개를 입력해 주세요.")
        @Size(max = 80, message = "한 줄 소개는 80자 이하입니다.")
        String oneLineIntro,
        @Pattern(regexp = "REAL_NAME|NICKNAME", message = "공개 표시 방식을 선택해 주세요.")
        String displayNameType,
        @NotBlank(message = "실명을 입력해 주세요.")
        @Size(min = 2, max = 50, message = "실명은 2~50자여야 합니다.")
        String realName,
        @NotNull(message = "생년월일을 입력해 주세요.")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,
        Long profileImageId,
        @NotEmpty(message = "관심 분야를 1개 이상 선택해 주세요.")
        @Size(max = 5, message = "관심 분야는 최대 5개까지 선택해 주세요.")
        List<Long> interestTagIds,
        @NotBlank(message = "50살의 나를 입력해 주세요.")
        @Size(max = 1000, message = "50살의 나는 1000자 이하입니다.")
        String futureMeAt50,
        @Size(max = 1000, message = "가입 이유는 1000자 이하입니다.")
        String joinReason,
        @Size(max = 1000, message = "현재 고민은 1000자 이하입니다.")
        String currentConcern,
        @Size(max = 1000, message = "3년 뒤 목표는 1000자 이하입니다.")
        String threeYearGoal
    ) {
        OnboardingService.CompleteProfileCommand toCommand() {
            return new OnboardingService.CompleteProfileCommand(
                nickname,
                oneLineIntro,
                displayNameType,
                realName,
                birthDate,
                profileImageId,
                interestTagIds,
                futureMeAt50,
                joinReason,
                currentConcern,
                threeYearGoal
            );
        }
    }

    public record OnboardingResponse(boolean completed) {
    }
}
