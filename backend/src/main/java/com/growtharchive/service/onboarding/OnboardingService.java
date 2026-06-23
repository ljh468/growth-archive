package com.growtharchive.service.onboarding;

import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.repository.InterestTagRepository;
import com.growtharchive.repository.InviteCodeRepository;
import com.growtharchive.repository.MemberRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import com.growtharchive.service.auth.CodeHashService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingService {

    private final CurrentMemberResolver currentMemberResolver;
    private final InviteCodeRepository inviteCodeRepository;
    private final MemberRepository memberRepository;
    private final InterestTagRepository interestTagRepository;
    private final CodeHashService codeHashService;

    public OnboardingService(
        CurrentMemberResolver currentMemberResolver,
        InviteCodeRepository inviteCodeRepository,
        MemberRepository memberRepository,
        InterestTagRepository interestTagRepository,
        CodeHashService codeHashService
    ) {
        this.currentMemberResolver = currentMemberResolver;
        this.inviteCodeRepository = inviteCodeRepository;
        this.memberRepository = memberRepository;
        this.interestTagRepository = interestTagRepository;
        this.codeHashService = codeHashService;
    }

    public void verifyInviteCode(HttpServletRequest request, String code) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.AUTHENTICATED);
        String activeHash = inviteCodeRepository.findActiveHash()
            .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INVITE_CODE, "현재 사용할 수 없는 초대코드입니다. 운영진에게 문의해 주세요."));
        if (!activeHash.equals(codeHashService.hashInviteCode(code))) {
            throw new ApiException(ErrorCode.INVALID_INVITE_CODE);
        }
        memberRepository.markInviteVerified(member.memberId());
    }

    public void agreeTerms(HttpServletRequest request, boolean termsAgreed, boolean privacyAgreed) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.INVITE_VERIFIED);
        if (!termsAgreed || !privacyAgreed) {
            throw new ApiException(ErrorCode.TERMS_NOT_AGREED);
        }
        memberRepository.agreeTerms(member.memberId());
    }

    @Transactional
    public void completeProfile(HttpServletRequest request, CompleteProfileCommand command) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.INVITE_VERIFIED);
        if (member.onboardingCompleted()) {
            throw new ApiException(ErrorCode.ALREADY_ONBOARDED);
        }
        if (!member.termsAgreed() || !member.privacyAgreed()) {
            throw new ApiException(ErrorCode.TERMS_NOT_AGREED);
        }
        String nickname = command.nickname().trim();
        if (memberRepository.existsNicknameForOtherMember(nickname, member.memberId())) {
            throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
        }
        if ("REAL_NAME".equals(command.displayNameType()) && isBlank(command.realName())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "실명 공개 선택 시 실명을 입력해 주세요.");
        }
        if (command.interestTagIds().size() > 5) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "관심 분야는 최대 5개까지 선택해 주세요.");
        }
        if (interestTagRepository.countActiveIds(command.interestTagIds()) != command.interestTagIds().size()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "선택할 수 없는 관심 분야가 포함되어 있습니다.");
        }
        memberRepository.completeOnboarding(
            member.memberId(),
            nickname,
            command.oneLineIntro().trim(),
            command.displayNameType(),
            trimToNull(command.realName()),
            trimToNull(command.job()),
            command.futureMeAt50().trim(),
            trimToNull(command.joinReason()),
            trimToNull(command.currentConcern()),
            trimToNull(command.threeYearGoal())
        );
        interestTagRepository.replaceMemberTags(member.memberId(), command.interestTagIds());
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

    public record CompleteProfileCommand(
        String nickname,
        String oneLineIntro,
        String displayNameType,
        String realName,
        String job,
        List<Long> interestTagIds,
        String futureMeAt50,
        String joinReason,
        String currentConcern,
        String threeYearGoal
    ) {
    }
}
