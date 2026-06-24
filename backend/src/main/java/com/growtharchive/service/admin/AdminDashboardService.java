package com.growtharchive.service.admin;

import com.growtharchive.repository.AdminDashboardRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.CurrentMemberResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

    private final CurrentMemberResolver currentMemberResolver;
    private final AdminDashboardRepository adminDashboardRepository;

    public AdminDashboardService(
        CurrentMemberResolver currentMemberResolver,
        AdminDashboardRepository adminDashboardRepository
    ) {
        this.currentMemberResolver = currentMemberResolver;
        this.adminDashboardRepository = adminDashboardRepository;
    }

    public AdminDashboardView getDashboard(HttpServletRequest request) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        LocalDate targetMonth = LocalDate.now().withDayOfMonth(1);
        AdminDashboardRepository.AdminDashboardCounts counts = adminDashboardRepository.counts(targetMonth);
        long incompleteCount = Math.max(0, counts.participationTargetCount() - counts.participationCompletedCount());
        return new AdminDashboardView(
            targetMonth.toString().substring(0, 7),
            counts.activeMemberCount(),
            counts.activeReadingRecordCount(),
            counts.meetingCount(),
            counts.activeReviewCount(),
            counts.unverifiedBookCount(),
            counts.participationTargetCount(),
            counts.participationCompletedCount(),
            incompleteCount
        );
    }

    public record AdminDashboardView(
        String month,
        long activeMemberCount,
        long activeReadingRecordCount,
        long meetingCount,
        long activeReviewCount,
        long unverifiedBookCount,
        long participationTargetCount,
        long participationCompletedCount,
        long participationIncompleteCount
    ) {
    }
}
