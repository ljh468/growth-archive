package com.growtharchive.service.monthly;

import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.repository.MonthlyRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonthlyService {

    private final CurrentMemberResolver currentMemberResolver;
    private final MonthlyRepository monthlyRepository;

    public MonthlyService(CurrentMemberResolver currentMemberResolver, MonthlyRepository monthlyRepository) {
        this.currentMemberResolver = currentMemberResolver;
        this.monthlyRepository = monthlyRepository;
    }

    public MonthlyActionPlanView getActionPlan(HttpServletRequest request, LocalDate targetMonth) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        return monthlyRepository.findActionPlan(member.memberId(), targetMonth.withDayOfMonth(1)).orElse(null);
    }

    @Transactional
    public MonthlyActionPlanView createActionPlan(HttpServletRequest request, ActionPlanCommand command) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        LocalDate targetMonth = command.targetMonth().withDayOfMonth(1);
        Long id = monthlyRepository.upsertActionPlan(
            member.memberId(),
            targetMonth,
            trimToNull(command.title()),
            required(command.content(), "실행계획 내용을 입력해 주세요.")
        );
        return monthlyRepository.findActionPlanById(id).orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_ERROR));
    }

    @Transactional
    public MonthlyActionPlanView updateActionPlan(HttpServletRequest request, Long actionPlanId, ActionPlanUpdateCommand command) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        MonthlyActionPlanView existing = monthlyRepository.findActionPlanById(actionPlanId)
            .orElseThrow(() -> new ApiException(ErrorCode.VALIDATION_ERROR, "실행계획을 찾을 수 없습니다."));
        if (!existing.memberId().equals(member.memberId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "작성자만 실행계획을 수정할 수 있습니다.");
        }
        monthlyRepository.updateActionPlan(actionPlanId, trimToNull(command.title()), required(command.content(), "실행계획 내용을 입력해 주세요."));
        return monthlyRepository.findActionPlanById(actionPlanId).orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_ERROR));
    }

    @Transactional
    public void deleteActionPlan(HttpServletRequest request, Long actionPlanId) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        MonthlyActionPlanView existing = monthlyRepository.findActionPlanById(actionPlanId)
            .orElseThrow(() -> new ApiException(ErrorCode.VALIDATION_ERROR, "실행계획을 찾을 수 없습니다."));
        if (!existing.memberId().equals(member.memberId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "작성자만 실행계획을 삭제할 수 있습니다.");
        }
        monthlyRepository.deleteActionPlan(actionPlanId);
    }

    public MonthlyReflectionSlot getReflectionSlot(HttpServletRequest request, LocalDate targetMonth) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        LocalDate normalizedMonth = targetMonth.withDayOfMonth(1);
        MonthlyReflectionView reflection = monthlyRepository.findReflection(member.memberId(), normalizedMonth).orElse(null);
        return new MonthlyReflectionSlot(normalizedMonth, true, reflection);
    }

    @Transactional
    public MonthlyReflectionView saveReflection(HttpServletRequest request, LocalDate targetMonth, ReflectionCommand command) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        String wellDone = trimToNull(command.didWell());
        String regret = trimToNull(command.couldImprove());
        String nextFocus = trimToNull(command.nextFocus());
        if (wellDone == null && regret == null && nextFocus == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "회고는 최소 1개 항목을 입력해 주세요.");
        }
        Long id = monthlyRepository.upsertReflection(member.memberId(), targetMonth.withDayOfMonth(1), wellDone, regret, nextFocus);
        return monthlyRepository.findReflectionById(id).orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_ERROR));
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public record ActionPlanCommand(LocalDate targetMonth, String title, String content) {
    }

    public record ActionPlanUpdateCommand(String title, String content) {
    }

    public record ReflectionCommand(String didWell, String couldImprove, String nextFocus) {
    }
}
