package com.growtharchive.controller.profile;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.profile.MyDashboard;
import com.growtharchive.service.profile.MyProfile;
import com.growtharchive.service.profile.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final ProfileService profileService;

    public MeController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<MyDashboard> dashboard(
        HttpServletRequest request,
        @RequestParam(required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate month
    ) {
        return ApiResponse.success(profileService.getDashboard(request, month));
    }

    @GetMapping("/profile")
    public ApiResponse<MyProfile> profile(HttpServletRequest request) {
        return ApiResponse.success(profileService.getMyProfile(request));
    }

    @PutMapping("/profile")
    public ApiResponse<MyProfile> updateProfile(
        HttpServletRequest request,
        @Valid @RequestBody UpdateProfileRequest body
    ) {
        return ApiResponse.success(profileService.updateMyProfile(request, body.toCommand()));
    }

    public record UpdateProfileRequest(
        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
        String nickname,
        @NotBlank(message = "한 줄 소개를 입력해 주세요.")
        @Size(max = 80, message = "한 줄 소개는 80자 이하입니다.")
        String oneLineIntro,
        @NotBlank(message = "실명을 입력해 주세요.")
        @Size(min = 2, max = 50, message = "실명은 2~50자여야 합니다.")
        String realName,
        @Pattern(regexp = "REAL_NAME|NICKNAME", message = "공개 표시 방식을 선택해 주세요.")
        String displayNameType,
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,
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
        String threeYearGoal,
        Long profileImageId
    ) {
        ProfileService.UpdateProfileCommand toCommand() {
            return new ProfileService.UpdateProfileCommand(
                nickname,
                oneLineIntro,
                realName,
                displayNameType,
                birthDate,
                interestTagIds,
                futureMeAt50,
                joinReason,
                currentConcern,
                threeYearGoal,
                profileImageId
            );
        }
    }
}
