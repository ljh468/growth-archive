package com.growtharchive.repository;

import java.sql.Date;
import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminDashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminDashboardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AdminDashboardCounts counts(LocalDate targetMonth) {
        LocalDate nextMonth = targetMonth.plusMonths(1);
        return jdbcTemplate.queryForObject(
            """
                SELECT
                    (SELECT count(*) FROM members WHERE onboarding_completed_at IS NOT NULL AND deactivated_at IS NULL) AS active_member_count,
                    (SELECT count(*) FROM reading_records WHERE status = 'ACTIVE') AS active_reading_record_count,
                    (SELECT count(*) FROM meetings WHERE status <> 'DELETED') AS meeting_count,
                    (SELECT count(*) FROM meeting_reviews WHERE status = 'ACTIVE') AS active_review_count,
                    (SELECT count(*) FROM books WHERE status = 'UNVERIFIED') AS unverified_book_count,
                    (SELECT count(*)
                     FROM members m
                     WHERE m.onboarding_completed_at IS NOT NULL
                       AND m.deactivated_at IS NULL
                       AND m.participation_start_month <= ?) AS participation_target_count,
                    (SELECT count(*)
                     FROM members m
                     WHERE m.onboarding_completed_at IS NOT NULL
                       AND m.deactivated_at IS NULL
                       AND m.participation_start_month <= ?
                       AND (
                         EXISTS (
                           SELECT 1 FROM reading_records rr
                           WHERE rr.member_id = m.id AND rr.status = 'ACTIVE'
                             AND rr.recorded_at >= ? AND rr.recorded_at < ?
                         )
                         OR EXISTS (
                           SELECT 1 FROM monthly_action_plans map
                           WHERE map.member_id = m.id AND map.status = 'ACTIVE'
                             AND map.target_month = ?
                         )
                       )) AS participation_completed_count
                """,
            (rs, rowNum) -> new AdminDashboardCounts(
                rs.getLong("active_member_count"),
                rs.getLong("active_reading_record_count"),
                rs.getLong("meeting_count"),
                rs.getLong("active_review_count"),
                rs.getLong("unverified_book_count"),
                rs.getLong("participation_target_count"),
                rs.getLong("participation_completed_count")
            ),
            Date.valueOf(targetMonth),
            Date.valueOf(targetMonth),
            Date.valueOf(targetMonth),
            Date.valueOf(nextMonth),
            Date.valueOf(targetMonth)
        );
    }

    public record AdminDashboardCounts(
        long activeMemberCount,
        long activeReadingRecordCount,
        long meetingCount,
        long activeReviewCount,
        long unverifiedBookCount,
        long participationTargetCount,
        long participationCompletedCount
    ) {
    }
}
