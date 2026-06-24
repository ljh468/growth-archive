package com.growtharchive.service.participation;

import com.growtharchive.repository.ParticipationRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipationService {

    private final CurrentMemberResolver currentMemberResolver;
    private final ParticipationRepository participationRepository;

    public ParticipationService(CurrentMemberResolver currentMemberResolver, ParticipationRepository participationRepository) {
        this.currentMemberResolver = currentMemberResolver;
        this.participationRepository = participationRepository;
    }

    public ParticipationStatusView getMyStatus(HttpServletRequest request, LocalDate targetMonth) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        return participationRepository.getMemberStatus(member.memberId(), targetMonth.withDayOfMonth(1));
    }

    public AdminParticipationSummary getAdminStatus(HttpServletRequest request, LocalDate targetMonth) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        LocalDate normalizedMonth = targetMonth.withDayOfMonth(1);
        List<AdminParticipationMemberView> members = participationRepository.getAdminMembers(normalizedMonth);
        long targetCount = members.stream().filter(AdminParticipationMemberView::calculationTarget).count();
        long completedCount = members.stream().filter(AdminParticipationMemberView::completed).count();
        long incompleteCount = members.stream().filter(AdminParticipationMemberView::coffeeSupportTarget).count();
        double completionRate = targetCount == 0 ? 0.0 : (double) completedCount / targetCount;
        return new AdminParticipationSummary(
            normalizedMonth.toString().substring(0, 7),
            targetCount,
            completedCount,
            incompleteCount,
            completionRate,
            members
        );
    }

    public byte[] getAdminCsv(HttpServletRequest request, LocalDate targetMonth) {
        AdminParticipationSummary summary = getAdminStatus(request, targetMonth);
        StringBuilder csv = new StringBuilder("memberId,displayName,nickname,readingRecordCount,hasActionPlan,participationStatus,coffeeSupportItem,adminMemo\n");
        for (AdminParticipationMemberView member : summary.members()) {
            if (!member.coffeeSupportTarget()) {
                continue;
            }
            csv.append(member.memberId()).append(',')
                .append(csvEscape(member.displayName())).append(',')
                .append(csvEscape(member.nickname())).append(',')
                .append(member.readingRecordCount()).append(',')
                .append(member.hasActionPlan()).append(',')
                .append(member.completed() ? "COMPLETED" : "NEEDS_PARTICIPATION").append(',')
                .append(csvEscape(member.coffeeSupportItem())).append(',')
                .append(csvEscape(member.adminMemo()))
                .append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public void saveAdminNote(HttpServletRequest request, Long memberId, LocalDate targetMonth, String note) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        participationRepository.saveNote(admin.memberId(), memberId, targetMonth.withDayOfMonth(1), note == null ? "" : note.trim());
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
