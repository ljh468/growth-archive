package com.growtharchive.service.admin;

import com.growtharchive.repository.InviteCodeRepository;
import com.growtharchive.repository.MemberRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import com.growtharchive.service.auth.CodeHashService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AdminMemberService {

    private final CurrentMemberResolver currentMemberResolver;
    private final MemberRepository memberRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final CodeHashService codeHashService;

    public AdminMemberService(
        CurrentMemberResolver currentMemberResolver,
        MemberRepository memberRepository,
        InviteCodeRepository inviteCodeRepository,
        CodeHashService codeHashService
    ) {
        this.currentMemberResolver = currentMemberResolver;
        this.memberRepository = memberRepository;
        this.inviteCodeRepository = inviteCodeRepository;
        this.codeHashService = codeHashService;
    }

    public void deactivate(HttpServletRequest request, Long memberId) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        memberRepository.deactivate(memberId);
    }

    public void reactivate(HttpServletRequest request, Long memberId) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        memberRepository.reactivate(memberId);
    }

    public InviteCodeResponse activeInviteCode(HttpServletRequest request) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        return new InviteCodeResponse(inviteCodeRepository.findActivePreview().orElse(null));
    }

    public void updateInviteCode(HttpServletRequest request, String code) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        inviteCodeRepository.replaceActiveCode(
            codeHashService.hashInviteCode(code),
            codeHashService.preview(code),
            admin.memberId()
        );
    }

    public record InviteCodeResponse(String codePreview) {
    }
}
