package com.growtharchive.repository;

import com.growtharchive.service.profile.ActivitySummary;
import com.growtharchive.service.profile.GrowthStats;
import com.growtharchive.service.profile.MyDashboard;
import com.growtharchive.service.profile.MyProfile;
import com.growtharchive.service.profile.ProfileCard;
import com.growtharchive.service.profile.ProfileDetail;
import com.growtharchive.service.reading.ReadingRecordDetail;
import java.sql.Array;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProfileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProfileCard> findPeople(Long interestTagId, int limit, int offset) {
        String tagFilter = interestTagId == null ? "" : " AND EXISTS (SELECT 1 FROM member_interest_tags mit_filter WHERE mit_filter.member_id = m.id AND mit_filter.interest_tag_id = ?)";
        Object[] args = interestTagId == null ? new Object[] {limit, offset} : new Object[] {interestTagId, limit, offset};
        return jdbcTemplate.query(
            """
                SELECT m.id, m.display_type, m.real_name, m.nickname, m.one_line_intro,
                       coalesce(profile_image.public_url, m.kakao_profile_image_url) AS profile_image_url,
                       m.fifty_year_old_me,
                       coalesce(array_agg(it.name ORDER BY it.display_order) FILTER (WHERE it.id IS NOT NULL), '{}') AS interest_tags,
                       stats.reading_record_count, stats.action_plan_count, stats.monthly_reflection_count,
                       stats.meeting_review_count, stats.small_meeting_created_count,
                       latest.title AS latest_title, latest.href AS latest_href, latest.occurred_at AS latest_occurred_at
                FROM members m
                LEFT JOIN image_assets profile_image ON profile_image.id = m.profile_image_id
                LEFT JOIN member_interest_tags mit ON mit.member_id = m.id
                LEFT JOIN interest_tags it ON it.id = mit.interest_tag_id AND it.is_active
                LEFT JOIN LATERAL (
                    SELECT
                        (SELECT count(*) FROM reading_records rr WHERE rr.member_id = m.id AND rr.status = 'ACTIVE') AS reading_record_count,
                        (SELECT count(*) FROM monthly_action_plans map WHERE map.member_id = m.id AND map.status = 'ACTIVE') AS action_plan_count,
                        (SELECT count(*) FROM monthly_reflections mr WHERE mr.member_id = m.id AND mr.status = 'ACTIVE') AS monthly_reflection_count,
                        (SELECT count(*) FROM meeting_reviews mrv WHERE mrv.member_id = m.id AND mrv.status = 'ACTIVE') AS meeting_review_count,
                        (SELECT count(*) FROM meetings mt WHERE mt.host_member_id = m.id AND mt.meeting_type = 'SMALL' AND mt.status <> 'DELETED') AS small_meeting_created_count
                ) stats ON true
                LEFT JOIN LATERAL (
                    SELECT b.title, '/books/' || b.id AS href, rr.recorded_at AS occurred_at
                    FROM reading_records rr
                    JOIN books b ON b.id = rr.book_id
                    WHERE rr.member_id = m.id AND rr.status = 'ACTIVE'
                    ORDER BY rr.recorded_at DESC
                    LIMIT 1
                ) latest ON true
                WHERE m.onboarding_completed_at IS NOT NULL
                  AND m.deactivated_at IS NULL
                """ + tagFilter + """
                GROUP BY m.id, profile_image.public_url, stats.reading_record_count, stats.action_plan_count,
                         stats.monthly_reflection_count, stats.meeting_review_count, stats.small_meeting_created_count,
                         latest.title, latest.href, latest.occurred_at
                ORDER BY latest.occurred_at DESC NULLS LAST, m.id DESC
                LIMIT ? OFFSET ?
                """,
            (rs, rowNum) -> new ProfileCard(
                rs.getLong("id"),
                displayName(rs),
                rs.getString("profile_image_url"),
                rs.getString("one_line_intro"),
                readStringArray(rs, "interest_tags"),
                summarize(rs.getString("fifty_year_old_me"), 90),
                mapStats(rs),
                mapActivity(rs, "READING_RECORD", "latest_title", "latest_href", "latest_occurred_at")
            ),
            args
        );
    }

    public Optional<ProfileDetail> findProfileDetail(Long memberId, boolean includeMemberOnly) {
        return jdbcTemplate.query(
            """
                SELECT m.id, m.display_type, m.real_name, m.nickname, m.one_line_intro, m.job,
                       m.join_reason, m.current_concern, m.three_year_goal,
                       coalesce(profile_image.public_url, m.kakao_profile_image_url) AS profile_image_url,
                       m.fifty_year_old_me,
                       coalesce(array_agg(it.name ORDER BY it.display_order) FILTER (WHERE it.id IS NOT NULL), '{}') AS interest_tags,
                       stats.reading_record_count, stats.action_plan_count, stats.monthly_reflection_count,
                       stats.meeting_review_count, stats.small_meeting_created_count
                FROM members m
                LEFT JOIN image_assets profile_image ON profile_image.id = m.profile_image_id
                LEFT JOIN member_interest_tags mit ON mit.member_id = m.id
                LEFT JOIN interest_tags it ON it.id = mit.interest_tag_id AND it.is_active
                LEFT JOIN LATERAL (
                    SELECT
                        (SELECT count(*) FROM reading_records rr WHERE rr.member_id = m.id AND rr.status = 'ACTIVE') AS reading_record_count,
                        (SELECT count(*) FROM monthly_action_plans map WHERE map.member_id = m.id AND map.status = 'ACTIVE') AS action_plan_count,
                        (SELECT count(*) FROM monthly_reflections mr WHERE mr.member_id = m.id AND mr.status = 'ACTIVE') AS monthly_reflection_count,
                        (SELECT count(*) FROM meeting_reviews mrv WHERE mrv.member_id = m.id AND mrv.status = 'ACTIVE') AS meeting_review_count,
                        (SELECT count(*) FROM meetings mt WHERE mt.host_member_id = m.id AND mt.meeting_type = 'SMALL' AND mt.status <> 'DELETED') AS small_meeting_created_count
                ) stats ON true
                WHERE m.id = ? AND m.onboarding_completed_at IS NOT NULL AND m.deactivated_at IS NULL
                GROUP BY m.id, profile_image.public_url, stats.reading_record_count, stats.action_plan_count,
                         stats.monthly_reflection_count, stats.meeting_review_count, stats.small_meeting_created_count
                """,
            rs -> {
                if (!rs.next()) {
                    return Optional.empty();
                }
                ProfileDetail.MemberOnlyProfile memberOnly = includeMemberOnly
                    ? new ProfileDetail.MemberOnlyProfile(
                        rs.getString("job"),
                        rs.getString("join_reason"),
                        rs.getString("current_concern"),
                        rs.getString("three_year_goal"),
                        findActionPlans(memberId, 3),
                        findReflections(memberId, 3)
                    )
                    : null;
                return Optional.of(new ProfileDetail(
                    rs.getLong("id"),
                    displayName(rs),
                    rs.getString("profile_image_url"),
                    rs.getString("one_line_intro"),
                    readStringArray(rs, "interest_tags"),
                    rs.getString("fifty_year_old_me"),
                    mapStats(rs),
                    findRecentReadingRecords(memberId, 3),
                    findMeetingReviews(memberId, 3),
                    findPublicActivities(memberId, 5),
                    memberOnly
                ));
            },
            memberId
        );
    }

    public Optional<MyProfile> findMyProfile(Long memberId) {
        return jdbcTemplate.query(
            """
                SELECT m.id, m.nickname, m.real_name, m.display_type, m.one_line_intro, m.job, m.profile_image_id,
                       coalesce(profile_image.public_url, m.kakao_profile_image_url) AS profile_image_url,
                       m.fifty_year_old_me, m.join_reason, m.current_concern, m.three_year_goal,
                       coalesce(array_agg(it.id ORDER BY it.display_order) FILTER (WHERE it.id IS NOT NULL), '{}') AS interest_tag_ids
                FROM members m
                LEFT JOIN image_assets profile_image ON profile_image.id = m.profile_image_id
                LEFT JOIN member_interest_tags mit ON mit.member_id = m.id
                LEFT JOIN interest_tags it ON it.id = mit.interest_tag_id AND it.is_active
                WHERE m.id = ?
                GROUP BY m.id, profile_image.public_url
                """,
            rs -> rs.next() ? Optional.of(new MyProfile(
                rs.getLong("id"),
                rs.getString("nickname"),
                rs.getString("real_name"),
                rs.getString("display_type"),
                displayName(rs),
                rs.getString("profile_image_url"),
                readNullableLong(rs, "profile_image_id"),
                rs.getString("one_line_intro"),
                rs.getString("job"),
                readLongArray(rs, "interest_tag_ids"),
                rs.getString("fifty_year_old_me"),
                rs.getString("join_reason"),
                rs.getString("current_concern"),
                rs.getString("three_year_goal")
            )) : Optional.empty(),
            memberId
        );
    }

    public void updateMyProfile(
        Long memberId,
        String nickname,
        String oneLineIntro,
        String displayType,
        String realName,
        String job,
        String futureMeAt50,
        String joinReason,
        String currentConcern,
        String threeYearGoal,
        Long profileImageId
    ) {
        jdbcTemplate.update(
            """
                UPDATE members
                SET nickname = ?, one_line_intro = ?, display_type = ?, real_name = ?, job = ?,
                    fifty_year_old_me = ?, join_reason = ?, current_concern = ?, three_year_goal = ?,
                    profile_image_id = ?, updated_at = now()
                WHERE id = ?
                """,
            nickname,
            oneLineIntro,
            displayType,
            realName,
            job,
            futureMeAt50,
            joinReason,
            currentConcern,
            threeYearGoal,
            profileImageId,
            memberId
        );
    }

    public MyDashboard findDashboard(Long memberId, LocalDate month) {
        MyProfile profile = findMyProfile(memberId).orElseThrow();
        Long readingRecordCount = countReadingRecordsForMonth(memberId, month);
        Long actionPlanCount = countActionPlansForMonth(memberId, month);
        boolean calculationTarget = isParticipationTarget(memberId, month);
        boolean completed = calculationTarget && (readingRecordCount > 0 || actionPlanCount > 0);
        return new MyDashboard(
            new MyDashboard.DashboardProfile(profile.memberId(), profile.displayName(), profile.profileImageUrl(), profile.oneLineIntro()),
            new MyDashboard.DashboardParticipation(month.toString().substring(0, 7), readingRecordCount, actionPlanCount, completed, calculationTarget && !completed),
            growthStats(memberId),
            findPublicActivities(memberId, 5)
        );
    }

    private boolean isParticipationTarget(Long memberId, LocalDate month) {
        Boolean target = jdbcTemplate.queryForObject(
            "SELECT participation_start_month <= ? FROM members WHERE id = ? AND deactivated_at IS NULL",
            Boolean.class,
            Date.valueOf(month),
            memberId
        );
        return Boolean.TRUE.equals(target);
    }

    private Long countReadingRecordsForMonth(Long memberId, LocalDate month) {
        LocalDate nextMonth = month.plusMonths(1);
        Long count = jdbcTemplate.queryForObject(
            """
                SELECT count(*)
                FROM reading_records
                WHERE member_id = ? AND status = 'ACTIVE' AND recorded_at >= ? AND recorded_at < ?
                """,
            Long.class,
            memberId,
            Date.valueOf(month),
            Date.valueOf(nextMonth)
        );
        return count == null ? 0 : count;
    }

    private Long countActionPlansForMonth(Long memberId, LocalDate month) {
        Long count = jdbcTemplate.queryForObject(
            """
                SELECT count(*)
                FROM monthly_action_plans
                WHERE member_id = ? AND status = 'ACTIVE' AND target_month = ?
                """,
            Long.class,
            memberId,
            Date.valueOf(month)
        );
        return count == null ? 0 : count;
    }

    private GrowthStats growthStats(Long memberId) {
        return jdbcTemplate.queryForObject(
            """
                SELECT
                  (SELECT count(*) FROM reading_records WHERE member_id = ? AND status = 'ACTIVE') AS reading_record_count,
                  (SELECT count(*) FROM monthly_action_plans WHERE member_id = ? AND status = 'ACTIVE') AS action_plan_count,
                  (SELECT count(*) FROM monthly_reflections WHERE member_id = ? AND status = 'ACTIVE') AS monthly_reflection_count,
                  (SELECT count(*) FROM meeting_reviews WHERE member_id = ? AND status = 'ACTIVE') AS meeting_review_count,
                  (SELECT count(*) FROM meetings WHERE host_member_id = ? AND meeting_type = 'SMALL' AND status <> 'DELETED') AS small_meeting_created_count
                """,
            (rs, rowNum) -> mapStats(rs),
            memberId,
            memberId,
            memberId,
            memberId,
            memberId
        );
    }

    private List<ReadingRecordDetail> findRecentReadingRecords(Long memberId, int limit) {
        return jdbcTemplate.query(
            """
                SELECT rr.id, rr.member_id, rr.book_id, rr.rating, rr.one_line_review, rr.blog_url, rr.status,
                       rr.recorded_at, rr.created_at, rr.representative_image_id,
                       b.title AS book_title, b.authors_text, b.thumbnail_url AS book_thumbnail_url,
                       m.nickname, m.real_name, m.display_type, m.kakao_profile_image_url,
                       ia.public_url AS record_image_url
                FROM reading_records rr
                JOIN books b ON b.id = rr.book_id
                JOIN members m ON m.id = rr.member_id
                LEFT JOIN image_assets ia ON ia.id = rr.representative_image_id
                WHERE rr.member_id = ? AND rr.status = 'ACTIVE'
                ORDER BY rr.recorded_at DESC
                LIMIT ?
                """,
            (rs, rowNum) -> new ReadingRecordDetail(
                rs.getLong("id"),
                rs.getLong("member_id"),
                displayName(rs),
                rs.getString("kakao_profile_image_url"),
                rs.getLong("book_id"),
                rs.getString("book_title"),
                rs.getString("authors_text"),
                rs.getString("book_thumbnail_url"),
                readNullableInteger(rs, "rating"),
                rs.getString("one_line_review"),
                rs.getString("blog_url"),
                readNullableLong(rs, "representative_image_id"),
                rs.getString("record_image_url"),
                rs.getString("status"),
                rs.getObject("recorded_at", OffsetDateTime.class),
                rs.getObject("created_at", OffsetDateTime.class)
            ),
            memberId,
            limit
        );
    }

    private List<ActivitySummary> findMeetingReviews(Long memberId, int limit) {
        return jdbcTemplate.query(
            """
                SELECT id, title, created_at
                FROM meeting_reviews
                WHERE member_id = ? AND status = 'ACTIVE'
                ORDER BY created_at DESC
                LIMIT ?
                """,
            (rs, rowNum) -> new ActivitySummary("MEETING_REVIEW", rs.getString("title"), "/reviews/" + rs.getLong("id"), rs.getObject("created_at", OffsetDateTime.class)),
            memberId,
            limit
        );
    }

    private List<ActivitySummary> findActionPlans(Long memberId, int limit) {
        return jdbcTemplate.query(
            """
                SELECT id, coalesce(title, to_char(target_month, 'YYYY-MM') || ' 액션플랜') AS title, created_at
                FROM monthly_action_plans
                WHERE member_id = ? AND status = 'ACTIVE'
                ORDER BY target_month DESC
                LIMIT ?
                """,
            (rs, rowNum) -> new ActivitySummary("ACTION_PLAN", rs.getString("title"), "/mypage/action-plans", rs.getObject("created_at", OffsetDateTime.class)),
            memberId,
            limit
        );
    }

    private List<ActivitySummary> findReflections(Long memberId, int limit) {
        return jdbcTemplate.query(
            """
                SELECT id, to_char(target_month, 'YYYY-MM') || ' 회고' AS title, created_at
                FROM monthly_reflections
                WHERE member_id = ? AND status = 'ACTIVE'
                ORDER BY target_month DESC
                LIMIT ?
                """,
            (rs, rowNum) -> new ActivitySummary("MONTHLY_REFLECTION", rs.getString("title"), "/mypage/reflections", rs.getObject("created_at", OffsetDateTime.class)),
            memberId,
            limit
        );
    }

    private List<ActivitySummary> findPublicActivities(Long memberId, int limit) {
        return jdbcTemplate.query(
            """
                SELECT 'READING_RECORD' AS type, b.title AS title, '/books/' || b.id AS href, rr.recorded_at AS occurred_at
                FROM reading_records rr
                JOIN books b ON b.id = rr.book_id
                WHERE rr.member_id = ? AND rr.status = 'ACTIVE'
                UNION ALL
                SELECT 'MEETING_REVIEW' AS type, mrv.title AS title, '/reviews/' || mrv.id AS href, mrv.created_at AS occurred_at
                FROM meeting_reviews mrv
                WHERE mrv.member_id = ? AND mrv.status = 'ACTIVE'
                ORDER BY occurred_at DESC
                LIMIT ?
                """,
            (rs, rowNum) -> new ActivitySummary(
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("href"),
                rs.getObject("occurred_at", OffsetDateTime.class)
            ),
            memberId,
            memberId,
            limit
        );
    }

    private GrowthStats mapStats(ResultSet rs) throws SQLException {
        return new GrowthStats(
            readLong(rs, "reading_record_count"),
            readLong(rs, "action_plan_count"),
            readLong(rs, "monthly_reflection_count"),
            readLong(rs, "meeting_review_count"),
            readLong(rs, "small_meeting_created_count")
        );
    }

    private ActivitySummary mapActivity(ResultSet rs, String type, String titleColumn, String hrefColumn, String occurredAtColumn) throws SQLException {
        OffsetDateTime occurredAt = rs.getObject(occurredAtColumn, OffsetDateTime.class);
        if (occurredAt == null) {
            return null;
        }
        return new ActivitySummary(type, rs.getString(titleColumn), rs.getString(hrefColumn), occurredAt);
    }

    private String displayName(ResultSet rs) throws SQLException {
        String realName = rs.getString("real_name");
        return "REAL_NAME".equals(rs.getString("display_type")) && realName != null && !realName.isBlank()
            ? realName
            : rs.getString("nickname");
    }

    private String summarize(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private List<String> readStringArray(ResultSet rs, String column) throws SQLException {
        Array array = rs.getArray(column);
        if (array == null) {
            return List.of();
        }
        return Arrays.stream((Object[]) array.getArray()).map(String.class::cast).toList();
    }

    private List<Long> readLongArray(ResultSet rs, String column) throws SQLException {
        Array array = rs.getArray(column);
        if (array == null) {
            return List.of();
        }
        return Arrays.stream((Object[]) array.getArray()).map(value -> ((Number) value).longValue()).toList();
    }

    private Long readLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? 0 : value;
    }

    private Integer readNullableInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private Long readNullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
