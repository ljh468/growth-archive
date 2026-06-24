package com.growtharchive.repository;

import com.growtharchive.service.participation.AdminParticipationMemberView;
import com.growtharchive.service.participation.ParticipationStatusView;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ParticipationRepository {

    public static final String COFFEE_SUPPORT_ITEM = "투썸 아메리카노 1잔";

    private final JdbcTemplate jdbcTemplate;

    public ParticipationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ParticipationStatusView getMemberStatus(Long memberId, LocalDate targetMonth) {
        LocalDate nextMonth = targetMonth.plusMonths(1);
        return jdbcTemplate.queryForObject(
            """
                SELECT m.participation_start_month <= ? AS calculation_target,
                       (SELECT count(*) FROM reading_records rr
                        WHERE rr.member_id = m.id AND rr.status = 'ACTIVE'
                          AND rr.recorded_at >= ? AND rr.recorded_at < ?) AS reading_record_count,
                       (SELECT count(*) FROM monthly_action_plans map
                        WHERE map.member_id = m.id AND map.status = 'ACTIVE'
                          AND map.target_month = ?) AS action_plan_count
                FROM members m
                WHERE m.id = ? AND m.deactivated_at IS NULL
                """,
            (rs, rowNum) -> mapStatus(targetMonth, rs),
            Date.valueOf(targetMonth),
            Date.valueOf(targetMonth),
            Date.valueOf(nextMonth),
            Date.valueOf(targetMonth),
            memberId
        );
    }

    public List<AdminParticipationMemberView> getAdminMembers(LocalDate targetMonth) {
        LocalDate nextMonth = targetMonth.plusMonths(1);
        return jdbcTemplate.query(
            """
                SELECT m.id, m.display_type, m.real_name, m.nickname,
                       coalesce(profile_image.public_url, m.kakao_profile_image_url) AS profile_image_url,
                       m.participation_start_month <= ? AS calculation_target,
                       (SELECT count(*) FROM reading_records rr
                        WHERE rr.member_id = m.id AND rr.status = 'ACTIVE'
                          AND rr.recorded_at >= ? AND rr.recorded_at < ?) AS reading_record_count,
                       EXISTS (
                         SELECT 1 FROM monthly_action_plans map
                         WHERE map.member_id = m.id AND map.status = 'ACTIVE' AND map.target_month = ?
                       ) AS has_action_plan,
                       pan.note AS admin_memo
                FROM members m
                LEFT JOIN image_assets profile_image ON profile_image.id = m.profile_image_id
                LEFT JOIN participation_admin_notes pan ON pan.member_id = m.id AND pan.target_month = ?
                WHERE m.onboarding_completed_at IS NOT NULL AND m.deactivated_at IS NULL
                ORDER BY m.participation_start_month ASC, m.id ASC
                """,
            (rs, rowNum) -> mapAdminMember(rs),
            Date.valueOf(targetMonth),
            Date.valueOf(targetMonth),
            Date.valueOf(nextMonth),
            Date.valueOf(targetMonth),
            Date.valueOf(targetMonth)
        );
    }

    public void saveNote(Long adminMemberId, Long memberId, LocalDate targetMonth, String note) {
        jdbcTemplate.update(
            """
                INSERT INTO participation_admin_notes (
                    member_id, target_month, note, created_by_member_id, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, now(), now())
                ON CONFLICT (member_id, target_month)
                DO UPDATE SET note = excluded.note,
                              created_by_member_id = excluded.created_by_member_id,
                              updated_at = now()
                """,
            memberId,
            Date.valueOf(targetMonth),
            note,
            adminMemberId
        );
    }

    private ParticipationStatusView mapStatus(LocalDate targetMonth, ResultSet rs) throws SQLException {
        long readingRecordCount = rs.getLong("reading_record_count");
        long actionPlanCount = rs.getLong("action_plan_count");
        boolean calculationTarget = rs.getBoolean("calculation_target");
        boolean completed = calculationTarget && (readingRecordCount > 0 || actionPlanCount > 0);
        return new ParticipationStatusView(
            targetMonth.toString().substring(0, 7),
            readingRecordCount,
            actionPlanCount,
            calculationTarget,
            completed,
            calculationTarget && !completed,
            COFFEE_SUPPORT_ITEM
        );
    }

    private AdminParticipationMemberView mapAdminMember(ResultSet rs) throws SQLException {
        long readingRecordCount = rs.getLong("reading_record_count");
        boolean hasActionPlan = rs.getBoolean("has_action_plan");
        boolean calculationTarget = rs.getBoolean("calculation_target");
        boolean completed = calculationTarget && (readingRecordCount > 0 || hasActionPlan);
        return new AdminParticipationMemberView(
            rs.getLong("id"),
            displayName(rs),
            rs.getString("nickname"),
            rs.getString("profile_image_url"),
            readingRecordCount,
            hasActionPlan,
            calculationTarget,
            completed,
            calculationTarget && !completed,
            COFFEE_SUPPORT_ITEM,
            rs.getString("admin_memo")
        );
    }

    private String displayName(ResultSet rs) throws SQLException {
        String realName = rs.getString("real_name");
        return "REAL_NAME".equals(rs.getString("display_type")) && realName != null && !realName.isBlank()
            ? realName
            : rs.getString("nickname");
    }
}
