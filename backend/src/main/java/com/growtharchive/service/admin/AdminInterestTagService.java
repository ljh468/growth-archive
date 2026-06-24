package com.growtharchive.service.admin;

import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.repository.AdminAuditLogRepository;
import com.growtharchive.repository.InterestTagRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminInterestTagService {

    private final CurrentMemberResolver currentMemberResolver;
    private final InterestTagRepository interestTagRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

    public AdminInterestTagService(
        CurrentMemberResolver currentMemberResolver,
        InterestTagRepository interestTagRepository,
        AdminAuditLogRepository adminAuditLogRepository
    ) {
        this.currentMemberResolver = currentMemberResolver;
        this.interestTagRepository = interestTagRepository;
        this.adminAuditLogRepository = adminAuditLogRepository;
    }

    public List<InterestTagView> list(HttpServletRequest request) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        return interestTagRepository.findAllForAdmin().stream().map(this::toView).toList();
    }

    @Transactional
    public InterestTagView create(HttpServletRequest request, InterestTagCommand command) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        String name = required(command.name(), "태그명을 입력해 주세요.");
        String slug = normalizeSlug(command.slug(), name);
        try {
            Long tagId = interestTagRepository.create(name, slug, command.displayOrder() == null ? 100 : command.displayOrder());
            adminAuditLogRepository.record(
                admin.memberId(),
                "CREATE_INTEREST_TAG",
                "INTEREST_TAG",
                tagId,
                null,
                "{\"name\":\"" + json(name) + "\",\"slug\":\"" + json(slug) + "\"}"
            );
            return interestTagRepository.findById(tagId).map(this::toView).orElseThrow(() -> new ApiException(ErrorCode.VALIDATION_ERROR));
        } catch (DuplicateKeyException exception) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "이미 사용 중인 태그 slug입니다.");
        }
    }

    @Transactional
    public InterestTagView update(HttpServletRequest request, Long tagId, InterestTagCommand command) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        InterestTagView before = interestTagRepository.findById(tagId).map(this::toView)
            .orElseThrow(() -> new ApiException(ErrorCode.VALIDATION_ERROR, "관심 태그를 찾을 수 없습니다."));
        String name = required(command.name(), "태그명을 입력해 주세요.");
        String slug = normalizeSlug(command.slug(), name);
        boolean active = command.active() == null || command.active();
        try {
            interestTagRepository.update(tagId, name, slug, command.displayOrder() == null ? before.displayOrder() : command.displayOrder(), active);
            adminAuditLogRepository.record(
                admin.memberId(),
                "UPDATE_INTEREST_TAG",
                "INTEREST_TAG",
                tagId,
                "{\"name\":\"" + json(before.name()) + "\",\"slug\":\"" + json(before.slug()) + "\",\"active\":" + before.active() + "}",
                "{\"name\":\"" + json(name) + "\",\"slug\":\"" + json(slug) + "\",\"active\":" + active + "}"
            );
            return interestTagRepository.findById(tagId).map(this::toView).orElseThrow(() -> new ApiException(ErrorCode.VALIDATION_ERROR));
        } catch (DuplicateKeyException exception) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "이미 사용 중인 태그 slug입니다.");
        }
    }

    @Transactional
    public void deactivate(HttpServletRequest request, Long tagId) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        InterestTagView before = interestTagRepository.findById(tagId).map(this::toView)
            .orElseThrow(() -> new ApiException(ErrorCode.VALIDATION_ERROR, "관심 태그를 찾을 수 없습니다."));
        interestTagRepository.deactivate(tagId);
        adminAuditLogRepository.record(
            admin.memberId(),
            "DEACTIVATE_INTEREST_TAG",
            "INTEREST_TAG",
            tagId,
            "{\"active\":" + before.active() + "}",
            "{\"active\":false}"
        );
    }

    private InterestTagView toView(InterestTagRepository.InterestTagRow row) {
        return new InterestTagView(row.id(), row.name(), row.slug(), row.displayOrder(), row.active());
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, message);
        }
        return value.trim();
    }

    private String normalizeSlug(String slug, String name) {
        String source = slug == null || slug.isBlank() ? name : slug;
        String normalized = Normalizer.normalize(source, Normalizer.Form.NFKD)
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
        if (normalized.isBlank()) {
            normalized = "tag-" + System.nanoTime();
        }
        return normalized.length() > 80 ? normalized.substring(0, 80) : normalized;
    }

    private String json(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record InterestTagCommand(String name, String slug, Integer displayOrder, Boolean active) {
    }

    public record InterestTagView(Long id, String name, String slug, int displayOrder, boolean active) {
    }
}
