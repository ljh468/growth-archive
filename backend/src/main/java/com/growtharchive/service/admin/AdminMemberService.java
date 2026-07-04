package com.growtharchive.service.admin;

import com.growtharchive.repository.InviteCodeRepository;
import com.growtharchive.repository.MemberRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import com.growtharchive.service.auth.CodeHashService;
import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.repository.AdminAuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminMemberService {

    private final CurrentMemberResolver currentMemberResolver;
    private final MemberRepository memberRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final CodeHashService codeHashService;
    private final AdminAuditLogRepository adminAuditLogRepository;

    public AdminMemberService(
        CurrentMemberResolver currentMemberResolver,
        MemberRepository memberRepository,
        InviteCodeRepository inviteCodeRepository,
        CodeHashService codeHashService,
        AdminAuditLogRepository adminAuditLogRepository
    ) {
        this.currentMemberResolver = currentMemberResolver;
        this.memberRepository = memberRepository;
        this.inviteCodeRepository = inviteCodeRepository;
        this.codeHashService = codeHashService;
        this.adminAuditLogRepository = adminAuditLogRepository;
    }

    public List<MemberSummary> list(HttpServletRequest request, String keyword, int page, int size) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        return memberRepository.findAdminMembers(keyword, safeSize(size), Math.max(page, 0) * safeSize(size))
            .stream()
            .map(this::toSummary)
            .toList();
    }

    public MemberSummary detail(HttpServletRequest request, Long memberId) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        return memberRepository.findAdminMemberById(memberId)
            .map(this::toSummary)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public void deactivate(HttpServletRequest request, Long memberId) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        if (admin.memberId().equals(memberId)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "자기 자신은 비활성화할 수 없습니다.");
        }
        MemberSummary before = memberRepository.findAdminMemberById(memberId)
            .map(this::toSummary)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
        if ("ADMIN".equals(before.role()) && memberRepository.countOtherActiveAdmins(memberId) == 0) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "마지막 운영진은 비활성화할 수 없습니다.");
        }
        memberRepository.deactivate(memberId);
        adminAuditLogRepository.record(
            admin.memberId(),
            "DEACTIVATE_MEMBER",
            "MEMBER",
            memberId,
            "{\"deactivatedAt\":\"" + nullSafe(before.deactivatedAt()) + "\"}",
            "{\"deactivated\":true}"
        );
    }

    @Transactional
    public void reactivate(HttpServletRequest request, Long memberId) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        MemberSummary before = memberRepository.findAdminMemberById(memberId)
            .map(this::toSummary)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
        memberRepository.reactivate(memberId);
        adminAuditLogRepository.record(
            admin.memberId(),
            "REACTIVATE_MEMBER",
            "MEMBER",
            memberId,
            "{\"deactivatedAt\":\"" + nullSafe(before.deactivatedAt()) + "\"}",
            "{\"deactivated\":false}"
        );
    }

    @Transactional
    public MemberSummary updateParticipationStartMonth(HttpServletRequest request, Long memberId, LocalDate participationStartMonth) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        LocalDate normalized = participationStartMonth.withDayOfMonth(1);
        MemberSummary before = memberRepository.findAdminMemberById(memberId)
            .map(this::toSummary)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
        memberRepository.updateParticipationStartMonth(memberId, normalized);
        adminAuditLogRepository.record(
            admin.memberId(),
            "UPDATE_PARTICIPATION_START_MONTH",
            "MEMBER",
            memberId,
            "{\"participationStartMonth\":\"" + before.participationStartMonth() + "\"}",
            "{\"participationStartMonth\":\"" + normalized + "\"}"
        );
        return detail(request, memberId);
    }

    @Transactional
    public MemberSummary updateRole(HttpServletRequest request, Long memberId, String role) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        String normalizedRole = normalizeInviteRole(role);
        MemberSummary before = memberRepository.findAdminMemberById(memberId)
            .map(this::toSummary)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
        if (admin.memberId().equals(memberId)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "자기 자신의 권한은 변경할 수 없습니다.");
        }
        if (before.deactivatedAt() != null || before.onboardingCompletedAt() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "활성 회원만 권한을 변경할 수 있습니다.");
        }
        if ("ADMIN".equals(before.role()) && "MEMBER".equals(normalizedRole) && memberRepository.countOtherActiveAdmins(memberId) == 0) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "마지막 운영진은 일반 회원으로 변경할 수 없습니다.");
        }
        memberRepository.updateRole(memberId, normalizedRole);
        adminAuditLogRepository.record(
            admin.memberId(),
            "UPDATE_MEMBER_ROLE",
            "MEMBER",
            memberId,
            "{\"role\":\"" + before.role() + "\"}",
            "{\"role\":\"" + normalizedRole + "\"}"
        );
        return detail(request, memberId);
    }

    public InviteCodeResponse activeInviteCode(HttpServletRequest request) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        InviteCodeRepository.ActiveInviteCodePreviews previews = inviteCodeRepository.findActivePreviews();
        return new InviteCodeResponse(previews.memberCodePreview(), previews.adminCodePreview());
    }

    public void updateInviteCode(HttpServletRequest request, String role, String code) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        String normalizedRole = normalizeInviteRole(role);
        String previousPreview = inviteCodeRepository.findActivePreview(normalizedRole).orElse(null);
        inviteCodeRepository.replaceActiveCode(
            normalizedRole,
            codeHashService.hashInviteCode(code),
            codeHashService.preview(code),
            admin.memberId()
        );
        adminAuditLogRepository.record(
            admin.memberId(),
            "UPDATE_INVITE_CODE",
            "INVITE_CODE",
            null,
            "{\"role\":\"" + normalizedRole + "\",\"codePreview\":\"" + nullSafe(previousPreview) + "\"}",
            "{\"role\":\"" + normalizedRole + "\",\"codePreview\":\"" + codeHashService.preview(code) + "\"}"
        );
    }

    private String normalizeInviteRole(String role) {
        if ("ADMIN".equals(role) || "MEMBER".equals(role)) {
            return role;
        }
        throw new ApiException(ErrorCode.VALIDATION_ERROR, "초대코드 권한을 선택해 주세요.");
    }

    private MemberSummary toSummary(MemberRepository.AdminMemberRow row) {
        return new MemberSummary(
            row.memberId(),
            row.role(),
            row.displayName(),
            row.realName(),
            row.nickname(),
            row.oneLineIntro(),
            row.job(),
            row.profileImageUrl(),
            row.participationStartMonth(),
            row.interestTags(),
            row.inviteVerifiedAt(),
            row.termsAgreedAt(),
            row.privacyAgreedAt(),
            row.onboardingCompletedAt(),
            row.deactivatedAt(),
            row.withdrawnAt(),
            row.createdAt()
        );
    }

    private int safeSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, 100);
    }

    private String nullSafe(Object value) {
        return value == null ? "" : value.toString();
    }

    public record InviteCodeResponse(String memberCodePreview, String adminCodePreview) {
    }

    public record MemberSummary(
        Long memberId,
        String role,
        String displayName,
        String realName,
        String nickname,
        String oneLineIntro,
        String job,
        String profileImageUrl,
        LocalDate participationStartMonth,
        List<String> interestTags,
        OffsetDateTime inviteVerifiedAt,
        OffsetDateTime termsAgreedAt,
        OffsetDateTime privacyAgreedAt,
        OffsetDateTime onboardingCompletedAt,
        OffsetDateTime deactivatedAt,
        OffsetDateTime withdrawnAt,
        OffsetDateTime createdAt
    ) {
    }
}
