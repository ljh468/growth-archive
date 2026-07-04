package com.growtharchive.service.profile;

import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.repository.ImageAssetRepository;
import com.growtharchive.repository.InterestTagRepository;
import com.growtharchive.repository.MemberRepository;
import com.growtharchive.repository.ProfileRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.AccessLevelCalculator;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import com.growtharchive.support.KstDateTimes;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final CurrentMemberResolver currentMemberResolver;
    private final AccessLevelCalculator accessLevelCalculator;
    private final ProfileRepository profileRepository;
    private final MemberRepository memberRepository;
    private final InterestTagRepository interestTagRepository;
    private final ImageAssetRepository imageAssetRepository;

    public ProfileService(
        CurrentMemberResolver currentMemberResolver,
        AccessLevelCalculator accessLevelCalculator,
        ProfileRepository profileRepository,
        MemberRepository memberRepository,
        InterestTagRepository interestTagRepository,
        ImageAssetRepository imageAssetRepository
    ) {
        this.currentMemberResolver = currentMemberResolver;
        this.accessLevelCalculator = accessLevelCalculator;
        this.profileRepository = profileRepository;
        this.memberRepository = memberRepository;
        this.interestTagRepository = interestTagRepository;
        this.imageAssetRepository = imageAssetRepository;
    }

    public List<ProfileCard> getPeople(HttpServletRequest request, Long interestTagId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        MemberPrincipal current = currentMemberResolver.resolveOptional(request);
        AccessLevel actual = accessLevelCalculator.calculate(current);
        boolean revealPrivateProfile = accessLevelCalculator.hasAtLeast(actual, AccessLevel.MEMBER);
        return profileRepository.findPeople(interestTagId, safeSize, Math.max(page, 0) * safeSize, revealPrivateProfile);
    }

    public ProfileDetail getProfile(HttpServletRequest request, Long memberId) {
        MemberPrincipal current = currentMemberResolver.resolveOptional(request);
        AccessLevel actual = accessLevelCalculator.calculate(current);
        boolean includeMemberOnly = accessLevelCalculator.hasAtLeast(actual, AccessLevel.MEMBER);
        return profileRepository.findProfileDetail(memberId, includeMemberOnly)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public List<MonthlyActionPlanShowcase> getMonthlyActionPlans(HttpServletRequest request, LocalDate month) {
        currentMemberResolver.require(request, AccessLevel.MEMBER);
        LocalDate targetMonth = month == null ? KstDateTimes.currentMonth() : month.withDayOfMonth(1);
        return profileRepository.findMonthlyActionPlans(targetMonth, 50);
    }

    public MyDashboard getDashboard(HttpServletRequest request, LocalDate month) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        LocalDate targetMonth = month == null ? KstDateTimes.currentMonth() : month.withDayOfMonth(1);
        return profileRepository.findDashboard(member.memberId(), targetMonth);
    }

    public MyProfile getMyProfile(HttpServletRequest request) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        return profileRepository.findMyProfile(member.memberId())
            .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public MyProfile updateMyProfile(HttpServletRequest request, UpdateProfileCommand command) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        String nickname = required(command.nickname(), "닉네임을 입력해 주세요.");
        if (memberRepository.existsNicknameForOtherMember(nickname, member.memberId())) {
            throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
        }
        if (!"REAL_NAME".equals(command.displayNameType()) && !"NICKNAME".equals(command.displayNameType())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "공개 표시 방식을 선택해 주세요.");
        }
        if (isBlank(command.realName())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "실명을 입력해 주세요.");
        }
        if (command.interestTagIds() == null || command.interestTagIds().isEmpty() || command.interestTagIds().size() > 5) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "관심 분야는 1~5개 선택해 주세요.");
        }
        if (interestTagRepository.countActiveIds(command.interestTagIds()) != command.interestTagIds().size()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "선택할 수 없는 관심 분야가 포함되어 있습니다.");
        }
        if (command.birthDate() != null && command.birthDate().isAfter(KstDateTimes.today())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "생년월일을 다시 확인해 주세요.");
        }
        if (!imageAssetRepository.isOwnedImage(member.memberId(), command.profileImageId(), "PROFILE")) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "사용할 수 없는 프로필 이미지입니다.");
        }
        profileRepository.updateMyProfile(
            member.memberId(),
            nickname,
            required(command.oneLineIntro(), "한 줄 소개를 입력해 주세요."),
            command.displayNameType(),
            command.realName().trim(),
            command.birthDate(),
            required(command.futureMeAt50(), "50살의 나를 입력해 주세요."),
            trimToNull(command.joinReason()),
            trimToNull(command.currentConcern()),
            trimToNull(command.threeYearGoal()),
            command.profileImageId()
        );
        interestTagRepository.replaceMemberTags(member.memberId(), command.interestTagIds());
        return getMyProfile(request);
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, message);
        }
        return value.trim();
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

    public record UpdateProfileCommand(
        String nickname,
        String oneLineIntro,
        String realName,
        String displayNameType,
        LocalDate birthDate,
        List<Long> interestTagIds,
        String futureMeAt50,
        String joinReason,
        String currentConcern,
        String threeYearGoal,
        Long profileImageId
    ) {
    }
}
